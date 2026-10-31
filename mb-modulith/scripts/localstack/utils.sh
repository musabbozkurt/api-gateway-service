#!/bin/bash

export SERVICE_NAME=kafka-service
export JAVA_OPTS="-Xmx8192m -Xms8192m -XX:MaxMetaspaceSize=8192m -Dfile.encoding=utf-8"

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

function getSecretStringValue {
  aws secretsmanager get-secret-value --region $AWS_DEFAULT_REGION --secret-id "$1" --query 'SecretString' |  jq "fromjson | .secret" -r
}

function getDummySecret {
  echo "a-dummy-secret-for-testing"
}

function getIdentityClientSecret {
  getDummySecret secretsmanager-url
}

function getClientSecretParameter {
   aws ssm get-parameter --region $AWS_DEFAULT_REGION --name $CLIENT_SECRET --with-decryption --query Parameter.Value --output text
}

function docker_login(){
  aws ecr get-login-password \
    --region $AWS_DEFAULT_REGION \
  | docker login \
    --username AWS \
    --password-stdin "${1:-$REGISTRY_ID}.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com"
}

function getSsmParameter {
    aws --region "$AWS_DEFAULT_REGION" ssm get-parameter --name "$1" --with-decryption --query Parameter.Value --output text
}
