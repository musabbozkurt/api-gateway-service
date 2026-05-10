#!/bin/bash

source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/common.sh

BASE_SCRIPTS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export BASE_SCRIPTS_DIR
echo "BASE_SCRIPTS_DIR: $BASE_SCRIPTS_DIR"

# Project root is two levels up from scripts/
PROJECT_ROOT="$( cd "$BASE_SCRIPTS_DIR/../.." && pwd )"
echo "PROJECT_ROOT: $PROJECT_ROOT"

echo "Current JAVA_HOME: $JAVA_HOME"

JAVA_HOME=$(/usr/libexec/java_home -v 25)
export JAVA_HOME
echo "Current JAVA_HOME after export: $JAVA_HOME"

# Load environment variables from .env file if it exists
if [ -f "$PROJECT_ROOT/.env" ]; then
    echo "Loading environment variables from .env file..."
    set -a  # automatically export all variables
    source "$PROJECT_ROOT/.env"
    set +a  # stop automatically exporting
else
    echo "No .env file found, using existing environment variables..."
fi

# Make Oracle scripts executable
echo "Making Oracle scripts executable..."
chmod +x "$BASE_SCRIPTS_DIR"/oracle-19/*.sh
chmod +x "$BASE_SCRIPTS_DIR"/oracle/*.sh 2>/dev/null || true  # ignore error if directory doesn't exist

docker-compose -f "$PROJECT_ROOT/docker-compose.yml" --profile start_application down

# Oracle login (optional - continue if fails)
if [[ -n "$ORACLE_USERNAME_OR_EMAIL" || -n "$ORACLE_AUTH_TOKEN" ]]; then
    echo "Oracle credentials found, attempting login..."
    docker logout container-registry.oracle.com
    if printf '%s\n' "$ORACLE_AUTH_TOKEN" | docker login container-registry.oracle.com -u "$ORACLE_USERNAME_OR_EMAIL" --password-stdin; then
        echo "Oracle login successful, pulling Enterprise image..."
        docker pull "$ORACLE_CONTAINER_REGISTRY_URL"
        docker-compose -f "$PROJECT_ROOT/docker-compose.yml" --profile start_application --profile debezium-oracle-19 up -d --build
    else
        echo "Oracle login failed, continuing without Oracle Enterprise image..."
        docker-compose -f "$PROJECT_ROOT/docker-compose.yml" --profile start_application up -d --build
    fi
else
    echo "Oracle credentials not set, skipping Oracle Enterprise setup..."
    docker-compose -f "$PROJECT_ROOT/docker-compose.yml" --profile start_application up -d --build
fi

echo "Waiting for services to be ready..."
sleep 20

echo "Checking available connector plugins..."
curl -s -X GET -H 'Content-Type: application/json' http://localhost:8087/connector-plugins | jq '.'

postgres_connector=$(createPostgresConnector)
echo "create Postgres Connector: $postgres_connector"

oracle_connector=$(createOracleConnector)
echo "create Oracle Connector: $oracle_connector"

KAFKA_DEBEZIUM_DIR="$( cd "$BASE_SCRIPTS_DIR/.." && pwd )"

cd "$KAFKA_DEBEZIUM_DIR"
./mvnw clean install
echo "mvn is run..."

./mvnw spring-boot:run
