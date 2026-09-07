# =============================================================================
# Compose image version checker (Windows PowerShell / Windows Terminal)
# =============================================================================
# Same job as check-compose-image-updates.sh: compare docker-compose.yml image
# pins to the newest stable registry tag. Use this on native Windows; Git Bash
# users can run the .sh script instead. check-compose-image-updates.cmd just
# calls this file.
#
# Flow: parse image: lines → Docker Hub prefix search or OCI tags/list → keep
# numeric/v-prefixed/Final tags → print OK / OUTDATED / FLOATING / SKIPPED /
# ERROR. Per-image progress is printed while querying registries (15s timeout).
# Check only by default. Use -Apply to bump OUTDATED pins (creates .bak backup).
# Use -DryRun to preview changes without writing.
#
# Usage:
#   .\docs\scripts\check-compose-image-updates.ps1
#   .\docs\scripts\check-compose-image-updates.ps1 -DryRun
#   .\docs\scripts\check-compose-image-updates.ps1 -Apply
#   .\docs\scripts\check-compose-image-updates.ps1 -ComposeFile docker-compose.yml -FailOnOutdated
# =============================================================================

[CmdletBinding()]
param(
    [string]$ComposeFile = "docker-compose.yml",
    [switch]$FailOnOutdated,
    [switch]$Apply,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
if ($env:FAIL_ON_OUTDATED -eq "true") { $FailOnOutdated = $true }
if ($Apply -and $DryRun) {
    Write-Error "Use either -Apply or -DryRun, not both."
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = (Resolve-Path (Join-Path $scriptDir "../..")).Path
if (-not (Test-Path $ComposeFile) -and (Test-Path (Join-Path $repoRoot $ComposeFile))) {
    $ComposeFile = Join-Path $repoRoot $ComposeFile
} elseif (-not (Test-Path $ComposeFile) -and (Test-Path (Join-Path $scriptDir $ComposeFile))) {
    $ComposeFile = Join-Path $scriptDir $ComposeFile
}
if (-not (Test-Path $ComposeFile)) {
    Write-Error "Compose file not found: $ComposeFile"
}

$HttpTimeoutSec = 15
$TagCache = @{}

function Write-ProgressCheck([string]$image, [string]$tag) {
    Write-Host "  checking ${image}:${tag} ..."
}

function Test-StableTag([string]$tag) {
    return $tag -match '^[vV]?[0-9]+(\.[0-9]+)*([.-][Ff]inal)?$'
}

function Get-TagSortKey([string]$tag) {
    $normalized = $tag -replace '^[vV]', '' -replace '[.-][Ff]inal$', ''
    $parts = $normalized.Split('.')
    $key = ""
    for ($i = 0; $i -lt 4; $i++) {
        $n = 0
        if ($i -lt $parts.Length -and $parts[$i] -match '^\d+$') { $n = [int]$parts[$i] }
        $key += $n.ToString("00000")
    }
    return $key
}

function Get-LatestStable([string[]]$tags) {
    $latest = $null
    $latestKey = ""
    foreach ($tag in $tags) {
        if (-not (Test-StableTag $tag)) { continue }
        $key = Get-TagSortKey $tag
        if ([string]::IsNullOrEmpty($latestKey) -or $key -gt $latestKey) {
            $latest = $tag
            $latestKey = $key
        }
    }
    return $latest
}

function Split-Image([string]$image) {
    $image = $image.Split("@")[0]
    $last = ($image -split "/")[-1]
    if ($last.Contains(":")) {
        $tag = $last.Substring($last.LastIndexOf(":") + 1)
        return @{ Name = $image.Substring(0, $image.Length - $tag.Length - 1); Tag = $tag }
    }
    return @{ Name = $image; Tag = "latest" }
}

function Get-RegistryHost([string]$name) {
    if ($name.Contains("/")) {
        $first = $name.Split("/")[0]
        if ($first.Contains(".") -or $first.Contains(":") -or $first -eq "localhost") {
            return $first
        }
    }
    return "docker.io"
}

function Get-RepositoryPath([string]$name) {
    $hostName = Get-RegistryHost $name
    if ($hostName -eq "docker.io") {
        if (-not $name.Contains("/")) { return "library/$name" }
        return $name
    }
    return $name.Substring($hostName.Length + 1)
}

function Get-AuthHost([string]$hostName) {
    if ($hostName -eq "docker.io") { return "registry-1.docker.io" }
    return $hostName
}

function Get-WwwAuthenticate([string]$headerValue) {
    $result = @{ Realm = ""; Service = ""; Scope = "" }
    if ([string]::IsNullOrEmpty($headerValue)) { return $result }
    if ($headerValue -match 'realm="([^"]*)"') { $result.Realm = $Matches[1] }
    if ($headerValue -match 'service="([^"]*)"') { $result.Service = $Matches[1] }
    if ($headerValue -match 'scope="([^"]*)"') { $result.Scope = $Matches[1] }
    return $result
}

function Get-BearerToken($auth) {
    $url = $auth.Realm
    $sep = if ($url.Contains("?")) { "&" } else { "?" }
    if ($auth.Service) { $url += "${sep}service=$($auth.Service)"; $sep = "&" }
    if ($auth.Scope) { $url += "${sep}scope=$($auth.Scope)" }
    try {
        $json = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec $HttpTimeoutSec
        if ($json.token) { return $json.token }
        if ($json.access_token) { return $json.access_token }
    } catch {
        return $null
    }
    return $null
}

function Get-TagSearchPrefixes([string]$tag) {
    $base = $tag -replace '^[vV]', '' -replace '[.-][Ff]inal$', ''
    $parts = $base.Split('.')
    $major = $parts[0]
    if ($major -notmatch '^\d+$') { return @() }
    $prefixes = New-Object System.Collections.Generic.List[string]
    if ($parts.Length -gt 1 -and $parts[1] -match '^\d+$') {
        $prefixes.Add("$major.$($parts[1])") | Out-Null
        $prefixes.Add("v$major.$($parts[1])") | Out-Null
    }
    $prefixes.Add($major) | Out-Null
    $prefixes.Add("v$major") | Out-Null
    return $prefixes
}

function Get-DockerHubTags([string]$repo, [string]$currentTag) {
    $tags = New-Object System.Collections.Generic.List[string]
    $seen = @{}
    foreach ($prefix in (Get-TagSearchPrefixes $currentTag)) {
        $url = "https://hub.docker.com/v2/repositories/$repo/tags?page_size=100&name=$prefix"
        $pages = 0
        while ($url -and $pages -lt 2) {
            $pages++
            try {
                $json = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec $HttpTimeoutSec
            } catch {
                break
            }
            foreach ($item in @($json.results)) {
                if ($item.name -and -not $seen.ContainsKey($item.name)) {
                    $seen[$item.name] = $true
                    $tags.Add($item.name) | Out-Null
                }
            }
            $url = $json.next
        }
    }
    return $tags
}

function Get-RegistryTags([string]$name, [string]$currentTag) {
    if ($TagCache.ContainsKey($name)) {
        return $TagCache[$name]
    }

    $result = @()
    if ((Get-RegistryHost $name) -eq "docker.io") {
        $result = @(Get-DockerHubTags (Get-RepositoryPath $name) $currentTag)
        $TagCache[$name] = $result
        return $result
    }

    $hostName = Get-AuthHost (Get-RegistryHost $name)
    $repo = Get-RepositoryPath $name
    $url = "https://$hostName/v2/$repo/tags/list?n=100"
    $token = $null
    $tags = New-Object System.Collections.Generic.List[string]
    $pages = 0
    $authAttempts = 0
    while ($url -and $pages -lt 5) {
        $pages++
        try {
            $headers = @{}
            if ($token) { $headers["Authorization"] = "Bearer $token" }
            $json = Invoke-RestMethod -Uri $url -Method Get -Headers $headers -TimeoutSec $HttpTimeoutSec
            foreach ($tag in @($json.tags)) { if ($tag) { $tags.Add($tag) } }
            $url = $null
        } catch {
            $resp = $_.Exception.Response
            if ($null -eq $resp) { break }
            $authAttempts++
            if ($authAttempts -gt 2) { break }
            $www = $null
            try { $www = $resp.Headers["Www-Authenticate"] } catch { $www = $null }
            $auth = Get-WwwAuthenticate "$www"
            if (-not $auth.Scope) { $auth.Scope = "repository:${repo}:pull" }
            $token = Get-BearerToken $auth
            if (-not $token) { break }
        }
    }
    $result = @($tags)
    $TagCache[$name] = $result
    return $result
}

function Get-ComposeImages([string]$path) {
    $images = New-Object System.Collections.Generic.List[string]
    foreach ($line in Get-Content -Path $path) {
        if ($line -match '^\s*#' ) { continue }
        if ($line -notmatch '^\s*image:\s*(.+)$') { continue }
        $value = $Matches[1].Trim().Trim("'`"")
        if ($value -match '\$\{[^}]*:-([^}]+)\}') { $value = $Matches[1] }
        if ($value -match '^\$\{') { continue }
        if (-not [string]::IsNullOrWhiteSpace($value) -and -not $images.Contains($value)) {
            $images.Add($value)
        }
    }
    return $images
}

$rows = New-Object System.Collections.Generic.List[object]
$counts = @{ OK = 0; OUTDATED = 0; FLOATING = 0; SKIPPED = 0; ERROR = 0 }

function Add-Row($status, $image, $current, $latest, $note) {
    $rows.Add([pscustomobject]@{ Status = $status; Image = $image; Current = $current; Latest = $latest; Note = $note })
    $counts[$status]++
    if ($env:GITHUB_ACTIONS) {
        if ($status -eq "OUTDATED") { Write-Host "::warning::$image`:$current -> $latest" }
        elseif ($status -eq "FLOATING") { Write-Host "::warning::$image`:$current uses a floating tag" }
        elseif ($status -eq "ERROR") { Write-Host "::warning::Failed to check ${image}:$current ($note)" }
    }
}

function Apply-OutdatedUpdates([string]$composePath, [switch]$PreviewOnly) {
    $updates = @($rows | Where-Object { $_.Status -eq "OUTDATED" -and $_.Latest -and $_.Latest -ne "-" })
    if ($updates.Count -eq 0) {
        Write-Host "No OUTDATED images to upgrade."
        return
    }

    $lines = Get-Content -Path $composePath
    $changeCount = 0
    $newLines = foreach ($line in $lines) {
        $newLine = $line
        if ($line -match '^\s*image:\s*(.+?)\s*$') {
            $value = $Matches[1].Trim()
            $inner = $value
            $quote = ""
            if (($value.StartsWith("'") -and $value.EndsWith("'")) -or ($value.StartsWith('"') -and $value.EndsWith('"'))) {
                $quote = $value.Substring(0, 1)
                $inner = $value.Substring(1, $value.Length - 2)
            } elseif ($value.StartsWith('${')) {
                $inner = $null
            }
            if ($null -ne $inner) {
                foreach ($update in $updates) {
                    $expected = "$($update.Image):$($update.Current)"
                    if ($inner -eq $expected) {
                        if ($line -match '^(\s*image:\s*)') {
                            $prefix = $Matches[1]
                        } else {
                            $prefix = "    image: "
                        }
                        $replacement = "$($update.Image):$($update.Latest)"
                        $newLine = "${prefix}${quote}${replacement}${quote}"
                        Write-Host "  upgrade $($update.Image):$($update.Current) -> $($update.Latest)"
                        $changeCount++
                        break
                    }
                }
            }
        }
        $newLine
    }

    if ($changeCount -eq 0) {
        Write-Host "No matching image: lines updated."
        return
    }
    if ($PreviewOnly) {
        Write-Host ""
        Write-Host "Dry run: $changeCount image line(s) would be updated in $composePath"
        return
    }

    $backup = "$composePath.bak"
    Copy-Item -Path $composePath -Destination $backup -Force
    $newLines | Set-Content -Path $composePath -Encoding UTF8
    Write-Host ""
    Write-Host "Applied $changeCount upgrade(s) to $composePath (backup: $backup)"
}

Write-Host "Checking image tags in $ComposeFile"
Write-Host ""

foreach ($image in (Get-ComposeImages $ComposeFile)) {
    $parts = Split-Image $image
    $hostName = Get-RegistryHost $parts.Name

    if ($hostName -eq "container-registry.oracle.com") {
        Write-ProgressCheck $parts.Name $parts.Tag
        Add-Row "SKIPPED" $parts.Name $parts.Tag "-" "private Oracle registry (login required)"
        continue
    }
    if ($parts.Tag -in @("latest", "master", "main")) {
        Write-ProgressCheck $parts.Name $parts.Tag
        Add-Row "FLOATING" $parts.Name $parts.Tag "-" "pin a release tag"
        continue
    }

    Write-ProgressCheck $parts.Name $parts.Tag
    $tags = @(Get-RegistryTags $parts.Name $parts.Tag)
    if ($tags.Count -eq 0) {
        Add-Row "ERROR" $parts.Name $parts.Tag "-" "could not list tags"
        continue
    }

    $latest = Get-LatestStable $tags
    if (-not $latest) {
        Add-Row "ERROR" $parts.Name $parts.Tag "-" "no stable tags found"
        continue
    }

    $currentKey = Get-TagSortKey $parts.Tag
    $latestKey = Get-TagSortKey $latest
    if ($currentKey -eq $latestKey) {
        Add-Row "OK" $parts.Name $parts.Tag $latest ""
    } elseif ($currentKey -lt $latestKey) {
        Add-Row "OUTDATED" $parts.Name $parts.Tag $latest "newer stable tag available"
    } else {
        Add-Row "OK" $parts.Name $parts.Tag $latest "compose tag is newer than detected latest stable"
    }
}

$rows | Format-Table -AutoSize Status, Image, Current, Latest, Note
Write-Host ("OK: {0}  outdated: {1}  floating: {2}  skipped: {3}  errors: {4}" -f $counts.OK, $counts.OUTDATED, $counts.FLOATING, $counts.SKIPPED, $counts.ERROR)

if ($DryRun) {
    Write-Host ""
    Write-Host "Upgrade preview:"
    Apply-OutdatedUpdates -composePath $ComposeFile -PreviewOnly
} elseif ($Apply) {
    Write-Host ""
    Write-Host "Applying upgrades:"
    Apply-OutdatedUpdates -composePath $ComposeFile
}

if ($env:GITHUB_STEP_SUMMARY) {
    $md = @("## Docker Compose image versions", "", "| Status | Image | Current | Latest stable | Note |", "| --- | --- | --- | --- | --- |")
    foreach ($row in $rows) {
        $md += "| $($row.Status) | ``$($row.Image)`` | ``$($row.Current)`` | ``$($row.Latest)`` | $($row.Note) |"
    }
    Add-Content -Path $env:GITHUB_STEP_SUMMARY -Value ($md -join "`n")
}

if ($FailOnOutdated -and (($counts.OUTDATED + $counts.FLOATING + $counts.ERROR) -gt 0)) {
    exit 1
}
exit 0
