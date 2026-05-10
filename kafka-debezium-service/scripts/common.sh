#!/bin/bash

# Logging
function me {
    basename $0
}

function timestamp {
  date +"%F %T"
}

function fail {
    echo -e "\033[0;31m[$(me) - $(timestamp)][FAIL] $*\033[0m"
    exit 1
}

function logInfo {
    echo -e "\033[0;32m[$(me) - $(timestamp)][INFO] $*\033[0m"
}

function logWarn {
    echo -e "\033[1;33m[$(me) - $(timestamp)][WARN] $*\033[0m"
}

function randomPort {
    echo -n $(( ( RANDOM % 60000 ) + 1024 ))
}

# Wait for Oracle to be ready, but this is not used currently
function wait_for_oracle_19() {
    logInfo "Waiting for Oracle to be ready..."
    until docker exec oracle-db-19 sqlplus -S sys/top_secret@//localhost:1521/ORCLCDB as sysdba <<< "SELECT 1 FROM dual;" > /dev/null 2>&1; do
        logWarn "Oracle not ready yet..."
        sleep 10
    done
    logInfo "Oracle is ready!"
}

function createPostgresConnector {
    postgres_connector=$(curl -X POST 'http://localhost:8087/connectors' \
                                -H 'Content-Type: application/json' \
                                -d '{
                                       "name": "inventory-connector",
                                       "config": {
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
                                       }
                                     }')
        echo "$postgres_connector"
}

function createOracleConnector {
    oracle_connector=$(curl --location 'http://localhost:8087/connectors' \
                       --header 'Content-Type: application/json' \
                       --data '{
                           "name": "oracle-connector",
                           "config": {
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
                           }
                       }')
    echo "$oracle_connector"
}
