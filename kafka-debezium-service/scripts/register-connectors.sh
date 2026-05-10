#!/bin/sh

# This script is used by the debezium-init docker-compose service to register
# Debezium connectors after the Debezium Connect REST API is ready.

DEBEZIUM_URL="http://debezium-connector:8083"
ORACLE_HOST="oracle-db"
ORACLE_PORT="1521"
MAX_ORACLE_WAIT=120  # max seconds to wait for Oracle

echo "Waiting for Debezium Connect REST API to be ready..."
until curl -s -o /dev/null -w "%{http_code}" "$DEBEZIUM_URL/connector-plugins" | grep -q "200"; do
    echo "Debezium Connect not ready yet, retrying in 5s..."
    sleep 5
done
echo "Debezium Connect REST API is ready!"

echo ""
echo "Available connector plugins:"
curl -s "$DEBEZIUM_URL/connector-plugins"

echo ""
echo "Registering PostgreSQL connector (create or update)..."
PG_RESULT=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$DEBEZIUM_URL/connectors/inventory-connector/config" \
    -H 'Content-Type: application/json' \
    -d '{
        "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
        "database.hostname": "debezium-postgres",
        "database.port": "5432",
        "database.user": "postgres",
        "database.password": "postgres",
        "database.dbname": "postgres",
        "database.server.name": "inventory-db-server",
        "table.include.list": "inventory.customers, inventory.products",
        "topic.prefix": "inventory-db-server",
        "plugin.name": "pgoutput",
        "slot.name": "debezium_slot"
    }')
echo "PostgreSQL connector registration HTTP status: $PG_RESULT"

echo ""
echo "Waiting for Oracle DB ($ORACLE_HOST:$ORACLE_PORT) to be ready..."
ORACLE_WAITED=0
while ! nc -z "$ORACLE_HOST" "$ORACLE_PORT" 2>/dev/null; do
    if [ "$ORACLE_WAITED" -ge "$MAX_ORACLE_WAIT" ]; then
        echo "Oracle DB not reachable after ${MAX_ORACLE_WAIT}s — skipping Oracle connector registration."
        echo ""
        echo "Listing registered connectors:"
        curl -s "$DEBEZIUM_URL/connectors"
        echo ""
        echo "Connector registration completed (Oracle skipped)."
        exit 0
    fi
    echo "Oracle DB not ready yet, retrying in 5s... (${ORACLE_WAITED}s/${MAX_ORACLE_WAIT}s)"
    sleep 5
    ORACLE_WAITED=$((ORACLE_WAITED + 5))
done
echo "Oracle DB is reachable!"

# Oracle listener may be up but DB not fully initialized — retry connector registration
echo "Registering Oracle connector (create or update, with retries)..."
ORACLE_ATTEMPTS=0
MAX_ORACLE_ATTEMPTS=12
ORACLE_CONFIG='{
    "connector.class": "io.debezium.connector.oracle.OracleConnector",
    "database.hostname": "oracle-db",
    "database.port": "1521",
    "database.user": "MB_ORACLE_USER",
    "database.password": "oracle_password",
    "database.dbname": "FREEPDB1",
    "database.pdb.name": "FREEPDB1",
    "database.server.name": "oracle-db-server",
    "database.connection.adapter": "logminer",
    "table.include.list": "MB_ORACLE_USER.CUSTOMERS",
    "topic.prefix": "oracle-db-server",
    "schema.history.internal.kafka.bootstrap.servers": "debezium-kafka1:29092,debezium-kafka2:29092,debezium-kafka3:29092",
    "schema.history.internal.kafka.topic": "oracle-schema-history",
    "log.mining.strategy": "online_catalog",
    "log.mining.continuous.mine": "true",
    "snapshot.mode": "initial",
    "include.schema.changes": "true",
    "database.tablename.case.insensitive": "false",
    "log.mining.session.max.ms": "10000",
    "log.mining.sleep.time.default.ms": "1000"
}'

while [ "$ORACLE_ATTEMPTS" -lt "$MAX_ORACLE_ATTEMPTS" ]; do
    ORACLE_HTTP_CODE=$(curl -s -o /tmp/oracle_result.json -w "%{http_code}" \
        -X PUT "$DEBEZIUM_URL/connectors/oracle-connector/config" \
        -H 'Content-Type: application/json' \
        -d "$ORACLE_CONFIG")
    ORACLE_RESULT=$(cat /tmp/oracle_result.json 2>/dev/null)

    if [ "$ORACLE_HTTP_CODE" = "200" ] || [ "$ORACLE_HTTP_CODE" = "201" ]; then
        echo "Oracle connector registered successfully! (HTTP $ORACLE_HTTP_CODE)"
        echo "$ORACLE_RESULT"
        break
    else
        ORACLE_ATTEMPTS=$((ORACLE_ATTEMPTS + 1))
        echo "Oracle connector registration failed (attempt ${ORACLE_ATTEMPTS}/${MAX_ORACLE_ATTEMPTS}, HTTP $ORACLE_HTTP_CODE), retrying in 10s..."
        echo "Error: $ORACLE_RESULT"
        sleep 10
    fi
done

if [ "$ORACLE_ATTEMPTS" -ge "$MAX_ORACLE_ATTEMPTS" ]; then
    echo "Oracle connector registration failed after ${MAX_ORACLE_ATTEMPTS} attempts — skipping."
fi

echo ""
echo "Listing registered connectors:"
curl -s "$DEBEZIUM_URL/connectors"
echo ""
echo "Connector registration completed!"
