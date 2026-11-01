# Local Stack module

Module `com.mb.modulith.localstack` (migrated from the standalone `local-stack` project): integration samples for
DynamoDB (AWS SDK v1 and v2, via LocalStack), S3 (via MinIO), Feign + OAuth2 client credentials, and Reactor/RxJava
based schedulers.

## Prerequisites

- `Java 26`, `Maven` (or `./mvnw`)
- `Docker`
- `LocalStack` and `MinIO` are defined in [docker-compose.yaml](../../docker-compose.yaml). Spring Boot Docker Compose
  support starts them with `./mvnw spring-boot:run`, or start them manually: `docker compose up -d`
- MinIO data is stored in the `minio-data` Docker volume (the old `/mb/data` file sharing path is no longer required)

## Run tests or try it via Swagger

- `./mvnw clean test` (the integration tests start LocalStack through Testcontainers, Docker is required)
- Swagger UI (group `localstack-api`): http://localhost:8080/swagger-ui/index.html
- Actuator: http://localhost:8080/actuator and metrics: http://localhost:8080/actuator/metrics
- Endpoints: `/temp-tables/**` (via the API Gateway: `/modulith/temp-tables/**`)

## Configuration

Properties live in [application.yml](../../src/main/resources/application.yml):

| Property                                            | Env variable                                         | Default                                 |
|-----------------------------------------------------|------------------------------------------------------|-----------------------------------------|
| `amazon.dynamodb.endpoint`                          | `AMAZON_DYNAMODB_ENDPOINT`                           | `http://localhost:4566/`                |
| `amazon.aws.accessKey` / `secretKey`                | `AMAZON_AWS_ACCESS_KEY` / `AMAZON_AWS_SECRET_KEY`    | `test` / `test`                         |
| `minio.endpoint` / `minio.publicUrl`                | `MINIO_ENDPOINT` / `STORAGE_PUBLIC_URL`              | `http://127.0.0.1:9000`                 |
| `minio.accessKey` / `minio.secret` / `minio.bucket` | `MINIO_ACCESS_KEY` / `MINIO_SECRET` / `MINIO_BUCKET` | `admin` / `supersecret` / `test-bucket` |
| `spring.security.oauth2.client.*.m2m-internal`      | `IDENTITY_CLIENT_SECRET`                             | `test`                                  |
| `third-party-client-api.url`                        | -                                                    | `base-url`                              |

In the `docker` profile the endpoints default to `http://localstack:4566/` and `http://minio:9000`.

## Create a DynamoDB table with the AWS CLI

See [how_to_create_table_and_put_item_to_ddb](how_to_create_table_and_put_item_to_ddb) for the full list of commands.

```
aws dynamodb list-tables --endpoint-url http://localhost:4566

aws dynamodb create-table \
    --table-name Music \
    --attribute-definitions \
        AttributeName=Artist,AttributeType=S \
        AttributeName=SongTitle,AttributeType=S \
    --key-schema \
        AttributeName=Artist,KeyType=HASH \
        AttributeName=SongTitle,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --table-class STANDARD \
    --endpoint-url http://localhost:4566
```

## Compiler flags, linting & formatting

### CloudFormation

#### Linting

CloudFormation templates are validated using AWS CLI
and [cfn-lint](https://github.com/aws-cloudformation/cfn-python-lint).
You need to have both installed locally to be able to run the `build.sh` script. `cfn-lint` has also IntelliJ IDEA and
Visual Studio Code plugins. Follow the [official docs](https://github.com/aws-cloudformation/cfn-python-lint#install)
for the detailed installation instructions.

#### Formatting

We do not auto-format CloudFormation templates.

# Setup Intellij

## Install plugins

- [Project Lombok](https://projectlombok.org/setup/intellij)
- [Error Prone Compiler](https://plugins.jetbrains.com/plugin/7349-error-prone-compiler)
- [pmd-idea](https://plugins.jetbrains.com/plugin/15412-pmd-idea) => setup to use [pmd.xml](../pmd.xml)
- [Google Java Format](https://github.com/google/google-java-format#intellij-android-studio-and-other-jetbrains-ides)

## Other documents

- [Formatting.md](Formatting.md), [StaticCodeAnalysis.md](StaticCodeAnalysis.md): carried
  over from the original project (template documents).
- [`scripts/localstack/`](../../scripts/localstack): original deployment helper scripts (templates, they still contain
  placeholders such as `service-name` and `ECR_ACCOUNT_ID`).
