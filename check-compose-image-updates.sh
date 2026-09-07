#!/usr/bin/env bash
# =============================================================================
# Compose image version checker (Linux, macOS, Windows Git Bash / WSL)
# =============================================================================
# Reads image: lines from docker-compose.yml, asks each registry for tags, and
# prints whether the pin is OK, OUTDATED, FLOATING (latest/master), SKIPPED, or
# ERROR.
#
# How it works:
#   1. Parse unique image: values (including ${VAR:-default} defaults).
#   2. Docker Hub: query tags by major/minor prefix (e.g. 18.3, 18) — not the
#      full tag list, so large repos stay fast.
#   3. Quay / GHCR / Elastic: OCI tags/list with bearer auth + pagination.
#   4. Keep only "stable" tags: 1.2.3, v1.2.3, 3.0.0.Final — not alpine/rc.
#   5. Compare the compose pin to the highest stable tag found.
#
# Check only by default. Use --apply to bump OUTDATED pins in docker-compose.yml
# (creates a .bak backup). Use --dry-run to preview changes without writing.
# Automated upgrades run via .github/workflows/dependency-update.yml (with Maven).
#
# Needs: curl, plus jq or python3. Oracle Container Registry is skipped.
# Prints per-image progress on stderr; each registry call times out after 15s.
#
# Usage:
#   ./check-compose-image-updates.sh
#   ./check-compose-image-updates.sh --dry-run          # preview upgrades
#   ./check-compose-image-updates.sh --apply            # write OUTDATED bumps
#   ./check-compose-image-updates.sh --compose-file docker-compose.yml --fail-on-outdated
#   FAIL_ON_OUTDATED=true ./check-compose-image-updates.sh
# =============================================================================

set -euo pipefail

COMPOSE_FILE="docker-compose.yml"
FAIL_ON_OUTDATED="${FAIL_ON_OUTDATED:-false}"
APPLY_UPDATES=false
DRY_RUN=false
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --compose-file|-f)
      COMPOSE_FILE="$2"
      shift 2
      ;;
    --fail-on-outdated)
      FAIL_ON_OUTDATED=true
      shift
      ;;
    --apply)
      APPLY_UPDATES=true
      shift
      ;;
    --dry-run)
      DRY_RUN=true
      shift
      ;;
    -h|--help)
      sed -n '2,30p' "$0"
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

if [[ "$APPLY_UPDATES" == "true" && "$DRY_RUN" == "true" ]]; then
  echo "Use either --apply or --dry-run, not both." >&2
  exit 2
fi

if [[ ! -f "$COMPOSE_FILE" && -f "$SCRIPT_DIR/$COMPOSE_FILE" ]]; then
  COMPOSE_FILE="$SCRIPT_DIR/$COMPOSE_FILE"
fi
if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "Compose file not found: $COMPOSE_FILE" >&2
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required" >&2
  exit 1
fi
if [[ "$APPLY_UPDATES" == "true" || "$DRY_RUN" == "true" ]] && ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is required for --apply and --dry-run" >&2
  exit 1
fi

CURL_OPTS=(--max-time 15 --connect-timeout 5 -fsSL)

log_progress() {
  printf '  checking %s:%s ...\n' "$1" "$2" >&2
}

cache_key_for() {
  printf '%s' "$1" | tr '/:' '__'
}

json_query() {
  local expr="$1"
  if command -v jq >/dev/null 2>&1; then
    jq -r "$expr"
  elif command -v python3 >/dev/null 2>&1; then
    python3 -c "import json,sys
data=json.load(sys.stdin)
expr='''$expr'''
if expr == '.token // .access_token // empty':
    print(data.get('token') or data.get('access_token') or '')
elif expr == '.tags[]?':
    for t in data.get('tags') or []:
        print(t)
elif expr == '.results[].name':
    for r in data.get('results') or []:
        print(r.get('name',''))
elif expr == '.next // empty':
    print(data.get('next') or '')
else:
    sys.exit('unsupported json query without jq: '+expr)
"
  else
    echo "jq or python3 is required to parse registry JSON" >&2
    exit 1
  fi
}

is_stable_tag() {
  local tag="$1"
  [[ "$tag" =~ ^[vV]?[0-9]+([.][0-9]+)*([.-][Ff]inal)?$ ]]
}

normalize_tag() {
  local tag="$1"
  tag="${tag#v}"
  tag="${tag#V}"
  tag="${tag%.Final}"
  tag="${tag%.final}"
  tag="${tag%-Final}"
  printf '%s' "$tag"
}

tag_sort_key() {
  normalize_tag "$1" | awk -F. '{
    for (i=1; i<=4; i++) {
      printf "%05d", ($i == "" ? 0 : $i)
    }
  }'
}

pick_latest_stable() {
  local latest="" latest_key="" key tag
  while IFS= read -r tag; do
    [[ -z "$tag" ]] && continue
    is_stable_tag "$tag" || continue
    key="$(tag_sort_key "$tag")"
    if [[ -z "$latest_key" || "$key" > "$latest_key" ]]; then
      latest="$tag"
      latest_key="$key"
    fi
  done <<< "$1"
  printf '%s' "$latest"
}

split_image() {
  local image="$1"
  IMAGE_NAME=""
  IMAGE_TAG="latest"
  image="${image%%@*}"
  local last="${image##*/}"
  if [[ "$last" == *:* ]]; then
    IMAGE_TAG="${last##*:}"
    IMAGE_NAME="${image%:$IMAGE_TAG}"
  else
    IMAGE_NAME="$image"
  fi
}

registry_host() {
  local name="$1"
  local first="${name%%/*}"
  if [[ "$name" == *"/"* ]] && [[ "$first" == *.* || "$first" == *:* || "$first" == localhost ]]; then
    printf '%s' "$first"
  else
    printf 'docker.io'
  fi
}

repository_path() {
  local name="$1"
  local host
  host="$(registry_host "$name")"
  if [[ "$host" == "docker.io" ]]; then
    if [[ "$name" != *"/"* ]]; then
      printf 'library/%s' "$name"
    else
      printf '%s' "$name"
    fi
  else
    printf '%s' "${name#"$host"/}"
  fi
}

auth_host() {
  case "$1" in
    docker.io) printf 'registry-1.docker.io' ;;
    *) printf '%s' "$1" ;;
  esac
}

parse_www_authenticate() {
  local header="$1"
  AUTH_REALM=""
  AUTH_SERVICE=""
  AUTH_SCOPE=""
  [[ -z "$header" ]] && return 0
  AUTH_REALM="$(printf '%s' "$header" | sed -n 's/.*realm="\([^"]*\)".*/\1/p')"
  AUTH_SERVICE="$(printf '%s' "$header" | sed -n 's/.*service="\([^"]*\)".*/\1/p')"
  AUTH_SCOPE="$(printf '%s' "$header" | sed -n 's/.*scope="\([^"]*\)".*/\1/p')"
}

fetch_bearer_token() {
  local realm="$1" service="$2" scope="$3"
  local url="$realm"
  local sep="?"
  [[ "$url" == *"?"* ]] && sep="&"
  [[ -n "$service" ]] && url="${url}${sep}service=${service}" && sep="&"
  [[ -n "$scope" ]] && url="${url}${sep}scope=${scope}"
  curl "${CURL_OPTS[@]}" "$url" 2>/dev/null | json_query '.token // .access_token // empty' || true
}

next_link_from_headers() {
  local headers_file="$1" host="$2"
  local link path
  link="$(grep -i '^Link:' "$headers_file" | tr -d '\r' | tr ',' '\n' | grep -i 'rel="next"' | tail -n1 || true)"
  [[ -z "$link" ]] && return 0
  path="$(printf '%s' "$link" | sed -n 's/.*<\([^>]*\)>.*/\1/p')"
  [[ -z "$path" ]] && return 0
  case "$path" in
    http*) printf '%s' "$path" ;;
    *) printf 'https://%s%s' "$host" "$path" ;;
  esac
}

tag_search_prefixes() {
  local tag="$1"
  local base major rest minor
  base="$(normalize_tag "$tag")"
  major="${base%%.*}"
  [[ "$major" =~ ^[0-9]+$ ]] || return 0
  rest="${base#*.}"
  minor="${rest%%.*}"
  if [[ -n "$rest" && "$minor" =~ ^[0-9]+$ ]]; then
    printf '%s\n' "${major}.${minor}"
    printf 'v%s\n' "${major}.${minor}"
  fi
  printf '%s\n' "$major"
  printf 'v%s\n' "$major"
}

list_tags_dockerhub() {
  local repo="$1" current_tag="$2"
  local prefix url body pages seen_file
  seen_file="$(mktemp)"
  while IFS= read -r prefix; do
    [[ -z "$prefix" ]] && continue
    url="https://hub.docker.com/v2/repositories/${repo}/tags?page_size=100&name=${prefix}"
    pages=0
    while [[ -n "$url" && $pages -lt 2 ]]; do
      pages=$((pages + 1))
      body="$(curl "${CURL_OPTS[@]}" "$url" 2>/dev/null || true)"
      [[ -z "$body" ]] && break
      while IFS= read -r tag_name; do
        [[ -z "$tag_name" ]] && continue
        grep -qxF "$tag_name" "$seen_file" 2>/dev/null && continue
        printf '%s\n' "$tag_name" >> "$seen_file"
        printf '%s\n' "$tag_name"
      done < <(printf '%s' "$body" | json_query '.results[].name')
      url="$(printf '%s' "$body" | json_query '.next // empty')"
    done
  done < <(tag_search_prefixes "$current_tag")
  rm -f "$seen_file"
}

list_tags_registry_paginated() {
  local name="$1"
  local host repo url tmp headers code body token pages www
  host="$(auth_host "$(registry_host "$name")")"
  repo="$(repository_path "$name")"
  url="https://${host}/v2/${repo}/tags/list?n=100"
  tmp="$(mktemp)"
  headers="$(mktemp)"
  token=""
  pages=0
  auth_attempts=0
  while [[ -n "$url" && $pages -lt 5 ]]; do
    pages=$((pages + 1))
    if [[ -n "$token" ]]; then
      code="$(curl -sS "${CURL_OPTS[@]}" -D "$headers" -o "$tmp" -w '%{http_code}' -H "Authorization: Bearer $token" "$url" 2>/dev/null || true)"
    else
      code="$(curl -sS "${CURL_OPTS[@]}" -D "$headers" -o "$tmp" -w '%{http_code}' "$url" 2>/dev/null || true)"
    fi
    body="$(cat "$tmp")"
    if [[ "$code" == "401" || "$code" == "403" ]]; then
      auth_attempts=$((auth_attempts + 1))
      [[ $auth_attempts -gt 2 ]] && break
      www="$(grep -i '^Www-Authenticate:' "$headers" | tail -n1 | tr -d '\r' | sed -E 's/^[^:]+:[[:space:]]*//')"
      parse_www_authenticate "$www"
      if [[ -n "$AUTH_REALM" ]]; then
        [[ -z "$AUTH_SCOPE" ]] && AUTH_SCOPE="repository:${repo}:pull"
        token="$(fetch_bearer_token "$AUTH_REALM" "$AUTH_SERVICE" "$AUTH_SCOPE")"
        [[ -z "$token" ]] && break
        continue
      fi
      break
    fi
    [[ "$code" != "200" ]] && break
    printf '%s' "$body" | json_query '.tags[]?'
    url="$(next_link_from_headers "$headers" "$host")"
  done
  rm -f "$tmp" "$headers"
}

list_tags_for_image() {
  local name="$1" current_tag="$2"
  local host
  host="$(registry_host "$name")"
  if [[ "$host" == "docker.io" ]]; then
    list_tags_dockerhub "$(repository_path "$name")" "$current_tag"
  else
    list_tags_registry_paginated "$name"
  fi
}

extract_images() {
  # Parse image: lines (including ${VAR:-default} defaults). Portable grep/sed (Linux, macOS, Git Bash).
  grep -E '^[[:space:]]*image:' "$COMPOSE_FILE" \
    | grep -v '^[[:space:]]*#' \
    | sed -E 's/^[[:space:]]*image:[[:space:]]*//' \
    | sed -E 's/^["'\'']//; s/["'\'']$//' \
    | sed -E 's/\$[{][^}]*:-([^}]+)[}]/\1/' \
    | sed -E 's/[[:space:]]+$//' \
    | grep -v '^\$[{]' \
    | grep -v '^$' \
    | sort -u
}

STATUS_OUTDATED=0
STATUS_FLOATING=0
STATUS_ERROR=0
STATUS_SKIPPED=0
STATUS_OK=0
RESULTS_FILE="$(mktemp)"
TAG_CACHE_DIR="$(mktemp -d)"
trap 'rm -f "$RESULTS_FILE"; rm -rf "$TAG_CACHE_DIR"' EXIT

append_row() {
  local status="$1" image="$2" current="$3" latest="$4" note="$5"
  printf '%s\n' "$status|$image|$current|$latest|$note" >> "$RESULTS_FILE"
  case "$status" in
    OK) STATUS_OK=$((STATUS_OK + 1)) ;;
    OUTDATED) STATUS_OUTDATED=$((STATUS_OUTDATED + 1)) ;;
    FLOATING) STATUS_FLOATING=$((STATUS_FLOATING + 1)) ;;
    ERROR) STATUS_ERROR=$((STATUS_ERROR + 1)) ;;
    SKIPPED) STATUS_SKIPPED=$((STATUS_SKIPPED + 1)) ;;
  esac
  if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
    case "$status" in
      OUTDATED) echo "::warning::${image}:${current} -> ${latest}" ;;
      FLOATING) echo "::warning::${image}:${current} uses a floating tag" ;;
      ERROR) echo "::warning::Failed to check ${image}:${current} (${note})" ;;
    esac
  fi
}

apply_outdated_updates() {
  local mode="$1"
  python3 - "$COMPOSE_FILE" "$mode" "$RESULTS_FILE" <<'PY'
import re
import shutil
import sys

compose_file, mode, results_file = sys.argv[1], sys.argv[2], sys.argv[3]
dry_run = mode == "dry-run"

updates = []
with open(results_file, encoding="utf-8") as handle:
    for line in handle:
        parts = line.strip().split("|", 4)
        if len(parts) < 4:
            continue
        status, image, current, latest = parts[0], parts[1], parts[2], parts[3]
        if status != "OUTDATED" or not latest or latest == "-":
            continue
        updates.append((image, current, latest))

if not updates:
    print("No OUTDATED images to upgrade.")
    sys.exit(0)

with open(compose_file, encoding="utf-8") as handle:
    lines = handle.readlines()

new_lines = []
change_count = 0
for line in lines:
    new_line = line
    match = re.match(r"^(\s*image:\s*)(.+?)\s*$", line)
    if match:
        prefix, value = match.group(1), match.group(2).strip()
        inner = value
        quote = ""
        if (value.startswith("'") and value.endswith("'")) or (
            value.startswith('"') and value.endswith('"')
        ):
            quote = value[0]
            inner = value[1:-1]
        elif value.startswith("${"):
            inner = None
        if inner is not None:
            for image, current, latest in updates:
                if inner == f"{image}:{current}":
                    new_line = f"{prefix}{quote}{image}:{latest}{quote}\n"
                    print(f"  upgrade {image}:{current} -> {latest}")
                    change_count += 1
                    break
    new_lines.append(new_line)

if change_count == 0:
    print("No matching image: lines updated.")
    sys.exit(0)

if dry_run:
    print(f"\nDry run: {change_count} image line(s) would be updated in {compose_file}")
    sys.exit(0)

backup = f"{compose_file}.bak"
shutil.copy2(compose_file, backup)
with open(compose_file, "w", encoding="utf-8") as handle:
    handle.writelines(new_lines)
print(f"\nApplied {change_count} upgrade(s) to {compose_file} (backup: {backup})")
PY
}

echo "Checking image tags in $COMPOSE_FILE"
echo

while IFS= read -r image; do
  [[ -z "$image" ]] && continue
  split_image "$image"
  host="$(registry_host "$IMAGE_NAME")"

  if [[ "$host" == "container-registry.oracle.com" ]]; then
    log_progress "$IMAGE_NAME" "$IMAGE_TAG"
    append_row "SKIPPED" "$IMAGE_NAME" "$IMAGE_TAG" "-" "private Oracle registry (login required)"
    continue
  fi

  if [[ "$IMAGE_TAG" == "latest" || "$IMAGE_TAG" == "master" || "$IMAGE_TAG" == "main" ]]; then
    log_progress "$IMAGE_NAME" "$IMAGE_TAG"
    append_row "FLOATING" "$IMAGE_NAME" "$IMAGE_TAG" "-" "pin a release tag"
    continue
  fi

  log_progress "$IMAGE_NAME" "$IMAGE_TAG"
  cache_file="$TAG_CACHE_DIR/$(cache_key_for "$IMAGE_NAME")"
  if [[ -f "$cache_file" ]]; then
    tags="$(cat "$cache_file")"
  else
    tags="$(list_tags_for_image "$IMAGE_NAME" "$IMAGE_TAG" || true)"
    printf '%s' "$tags" > "$cache_file"
  fi
  if [[ -z "$tags" ]]; then
    append_row "ERROR" "$IMAGE_NAME" "$IMAGE_TAG" "-" "could not list tags"
    continue
  fi

  latest="$(pick_latest_stable "$tags")"
  if [[ -z "$latest" ]]; then
    append_row "ERROR" "$IMAGE_NAME" "$IMAGE_TAG" "-" "no stable tags found"
    continue
  fi

  current_key="$(tag_sort_key "$IMAGE_TAG")"
  latest_key="$(tag_sort_key "$latest")"
  if [[ "$current_key" == "$latest_key" ]]; then
    append_row "OK" "$IMAGE_NAME" "$IMAGE_TAG" "$latest" ""
  elif [[ "$current_key" < "$latest_key" ]]; then
    append_row "OUTDATED" "$IMAGE_NAME" "$IMAGE_TAG" "$latest" "newer stable tag available"
  else
    append_row "OK" "$IMAGE_NAME" "$IMAGE_TAG" "$latest" "compose tag is newer than detected latest stable"
  fi
done < <(extract_images)

printf '%-10s %-72s %-16s %-16s %s\n' "STATUS" "IMAGE" "CURRENT" "LATEST" "NOTE"
printf '%s\n' "------------------------------------------------------------------------------------------------------------------------------"
while IFS='|' read -r status image current latest note; do
  printf '%-10s %-72s %-16s %-16s %s\n' "$status" "$image" "$current" "$latest" "$note"
done < "$RESULTS_FILE"
echo
echo "OK/newer: $STATUS_OK  outdated: $STATUS_OUTDATED  floating: $STATUS_FLOATING  skipped: $STATUS_SKIPPED  errors: $STATUS_ERROR"

if [[ "$DRY_RUN" == "true" ]]; then
  echo
  echo "Upgrade preview:"
  apply_outdated_updates "dry-run"
elif [[ "$APPLY_UPDATES" == "true" ]]; then
  echo
  echo "Applying upgrades:"
  apply_outdated_updates "apply"
fi

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "## Docker Compose image versions"
    echo
    echo "| Status | Image | Current | Latest stable | Note |"
    echo "| --- | --- | --- | --- | --- |"
    while IFS='|' read -r status image current latest note; do
      echo "| $status | \`$image\` | \`$current\` | \`$latest\` | $note |"
    done < "$RESULTS_FILE"
  } >> "$GITHUB_STEP_SUMMARY"
fi

if [[ "$FAIL_ON_OUTDATED" == "true" ]] && (( STATUS_OUTDATED + STATUS_FLOATING + STATUS_ERROR > 0 )); then
  exit 1
fi
exit 0
