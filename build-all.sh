#!/bin/bash

# Run mvn clean install in all Maven project directories
# Uses Java 25 — auto-detects via SDKMAN, JAVA_HOME, or system defaults

set_java_25() {
  # 1. Try SDKMAN
  if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk use java 25.0.2-open 2>/dev/null && return
  fi

  # 2. If JAVA_HOME is already set to Java 25, use it
  if [ -n "$JAVA_HOME" ] && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"25'; then
    return
  fi

  # 3. macOS: use java_home utility
  if command -v /usr/libexec/java_home &>/dev/null; then
    local jh=$(/usr/libexec/java_home -v 25 2>/dev/null)
    if [ -n "$jh" ] && [ -d "$jh" ]; then
      export JAVA_HOME="$jh"
      return
    fi
  fi

  # 4. Linux / Windows Git Bash: search common paths
  for candidate in \
    "/usr/lib/jvm/java-25"* \
    "/usr/lib/jvm/jdk-25"* \
    "/c/Program Files/Java/jdk-25"* \
    "/c/Program Files/Eclipse Adoptium/jdk-25"*; do
    if [ -d "$candidate" ]; then
      export JAVA_HOME="$candidate"
      return
    fi
  done

  # 5. Fall back to whatever 'java' is on PATH
  if java -version 2>&1 | grep -q '"25'; then
    return
  fi

  echo "❌ Java 25 not found. Please install it or set JAVA_HOME."
  exit 1
}

set_java_25

echo "Using JAVA_HOME: $JAVA_HOME"
echo "Java version:"
java -version
echo ""

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

MAVEN_DIRS=(
  "api-gateway"
  "brokerage-provider"
  "gitlab-service"
  "inventory-management-service"
  "kafka-debezium-service"
  "notification-service"
  "openai-service"
  "payment-service"
  "service-registry"
  "spring-config-server"
  "stock-exchange-service"
  "student-service"
  "swagger-application"
)

FAILED_MODULES=()

for dir in "${MAVEN_DIRS[@]}"; do
  if [ ! -d "$BASE_DIR/$dir" ]; then
    echo "⚠️  Skipping $dir (directory not found)"
    continue
  fi
  echo ""
  echo "=========================================="
  echo "Building: $dir"
  echo "=========================================="
  cd "$BASE_DIR/$dir" && ./mvnw clean install
  if [ $? -ne 0 ]; then
    echo "❌ FAILED: $dir"
    FAILED_MODULES+=("$dir")
  else
    echo "✅ SUCCESS: $dir"
  fi
done

echo ""
echo "=========================================="
echo "Build Summary"
echo "=========================================="
if [ ${#FAILED_MODULES[@]} -eq 0 ]; then
  echo "✅ All modules built successfully."
else
  echo "❌ The following modules failed:"
  for mod in "${FAILED_MODULES[@]}"; do
    echo "   - $mod"
  done
  exit 1
fi
