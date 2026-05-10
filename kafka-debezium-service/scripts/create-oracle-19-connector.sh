#!/bin/bash

docker exec oracle-db-19 bash -c "
  cd /opt/oracle/oradata/ && mkdir -p recovery_area &&
  cd /opt/oracle/scripts/ && chmod +x 01-setup-debezium.sh && ./01-setup-debezium.sh &&
  sqlplus debezium/dbz@//localhost:1521/ORCLPDB1 @/opt/oracle/scripts/02-init.sql
"

echo "Checking available connector plugins..."
curl -s -X GET -H 'Content-Type: application/json' http://localhost:8087/connector-plugins | jq '.'

# Delete existing connector if it exists
echo "Deleting existing oracle-db-19-connector if it exists..."
curl --location --request DELETE 'http://localhost:8087/connectors/oracle-db-19-connector' --header 'Content-Type: application/json'

# Wait a moment for cleanup
sleep 3

# Check if connector already exists and is running
CONNECTOR_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8087/connectors/oracle-db-19-connector/status)

if [ "$CONNECTOR_STATUS" -eq 200 ]; then
    echo "Connector oracle-db-19-connector already exists and is running"
    curl -s http://localhost:8087/connectors/oracle-db-19-connector/status | jq '.'
else
    echo "Creating oracle-db-19-connector..."
    oracle_19_connector=$(curl --location 'http://localhost:8087/connectors' \
                          --header 'Content-Type: application/json' \
                          --data '{
                              "name": "oracle-db-19-connector",
                              "config": {
                                  "connector.class": "io.debezium.connector.oracle.OracleConnector",
                                  "tasks.max": "1",
                                  "database.server.name": "oracle-db-19-server",
                                  "database.user": "c##dbzuser",
                                  "database.password": "dbz",
                                  "database.hostname": "oracle-db-19",
                                  "database.port": "1521",
                                  "database.dbname": "ORCLCDB",
                                  "database.pdb.name": "ORCLPDB1",
                                  "database.connection.adapter": "logminer",
                                  "schema.history.internal.kafka.bootstrap.servers": "debezium-kafka1:29092,debezium-kafka2:29092,debezium-kafka3:29092",
                                  "schema.history.internal.kafka.topic": "oracle-19-schema-history",
                                  "topic.prefix": "oracle-db-19-server",
                                  "table.include.list": "DEBEZIUM.CUSTOMERS,DEBEZIUM.ORDERS,DEBEZIUM.PRODUCTS,DEBEZIUM.PRODUCTS_ON_HAND",
                                  "snapshot.mode": "always",
                                  "log.mining.strategy": "online_catalog",
                                  "log.mining.continuous.mine": "true"
                              }
                          }')
    echo "create Oracle 19 Connector: $oracle_19_connector"
fi
