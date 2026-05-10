### Prerequisites

- Docker should be installed
- Java 25 should be installed --> `export JAVA_HOME=$(/usr/libexec/java_home -v 25)`
- Oracle Container Registry (OPTIONAL)
    - Create account: https://container-registry.oracle.com/
    - Generate `Auth Token`, copy the token, and update `ORACLE_AUTH_TOKEN` and update `ORACLE_USERNAME_OR_EMAIL`
      in [.env](../.env) file

-----

### `docker-compose` contains the followings

> **Note:** All Debezium infrastructure services are defined in the root
> [docker-compose.yml](../docker-compose.yml) and start automatically with
> `docker-compose --profile start_application up -d --build`. Oracle 19 Enterprise uses
> the additional `debezium-oracle-19` profile.

- `PostgreSQL (debezium-postgres)` DB connection details
    - `User: postgres`
    - `Password: postgres`
    - `Database: postgres`
    - `Port: 5434`
- `Oracle 23 PDB (Pluggable Database)` DB connection details
    - `Host: localhost`
    - `Service: freepdb1`
    - `Port: 1522`
    - Login:
        - Authentication: `SYSDBA`
            - `User: SYS`
            - `Password: oracle_password`
        - Authentication: `User & Password`
            - `User: MB_ORACLE_USER`
            - `Password: oracle_password`
- `Oracle 19` DB connection details
    - `Host: localhost`
    - `Service: ORCLPDB1` or `Service: ORCLCDB`
    - `Port: 1521`
    - Login:
        - Authentication: `SYSDBA`
            - `User: SYS`
            - `Password: oracle_19_password`
        - Authentication: `User & Password`
            - `User: c##dbzuser` for `oracle-db-19-connector` to connect to `ORCLPDB1`
            - `Password: dbz`
        - Authentication: `User & Password`
            - `User: debezium` for `DML operations` to connect to `ORCLPDB1`
            - `Password: dbz`

- `Kafka Nodes`: http://localhost:9999/ui/clusters/debezium-local/brokers
- `kafka-ui`: http://localhost:9999 (both `local` and `debezium-local` clusters)
- `debezium-connectors`: http://localhost:8087/connectors
- Check the logs of `oracle-db-19` container, if `DATABASE IS READY TO USE!` message is printed, then run
  the [./scripts/create-oracle-19-connector.sh](scripts/create-oracle-19-connector.sh) command to set up
  `debezium-oracle-db-19` user and other required settings via `Terminal`

-----

### How to start the application

- From the project root directory, run all services including kafka-debezium-service:
  ```sh
  docker-compose --profile start_application up -d --build
  ```
  The `debezium-init` container will automatically register the PostgreSQL and Oracle connectors once the Debezium
  Connect REST API is ready.
- To also include Oracle 19 Enterprise:
  ```sh
  docker-compose --profile start_application --profile debezium-oracle-19 up -d --build
  ```
- Or run [./scripts/run.sh](scripts/run.sh) for the legacy standalone startup flow

-----

### Check and Test REST APIs via Swagger and Actuator

- Swagger Url: http://localhost:8001/swagger-ui/index.html
- Actuator Url: http://localhost:8001/actuator

-----

### How to test Debezium

- Connect to `PostgreSQL` with the provided connection details above
- Run the following query to make sure there are records

  ```sql
  
  select *
  from inventory.customers;
  
  ```

- Run the following script to update a record

  ```sql

  UPDATE inventory.customers
  SET email = 'sally.thomas.updated@acme.com'
  WHERE id = 1001;

  ```

-

Check [inventory-db-server.inventory.customers topic](http://localhost:9999/ui/clusters/debezium-local/all-topics?perPage=25)
to make sure message is published

-----

### Reference Documentation

For further reference, please consider the following sections:

- [Debezium Source Connectors](https://debezium.io/documentation/reference/stable/connectors/index.html)
- [Setting Up a Kafka Cluster Using Docker Compose(Kraft Mode): A Step-by-Step Guide](https://medium.com/@darshak.kachchhi/setting-up-a-kafka-cluster-using-docker-compose-a-step-by-step-guide-a1ee5972b122)
- [Posting Request Body with Curl [Curl/Bash Code]](https://reqbin.com/req/curl/c-d2nzjn3z/curl-post-body)
- [Capture Oracle database events with Debezium - Preparing the database (Part 1)](https://www.youtube.com/watch?v=mzho5QS6CSk)
    - [Capture Oracle database events in Apache Kafka with Debezium](https://developers.redhat.com/blog/2021/04/19/capture-oracle-database-events-in-apache-kafka-with-debezium)
    - https://github.com/debezium/oracle-vagrant-box

-----
