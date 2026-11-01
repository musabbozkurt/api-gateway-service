#!/bin/bash

set -euo pipefail
MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$MYDIR/../.."

source "$MYDIR/parameters.sh"
aws ecr get-login-password | docker login -u AWS --password-stdin ${ECR_ACCOUNT_ID}.dkr.ecr.eu-west-1.amazonaws.com

source "$MYDIR/utils.sh"
eval "$(scloud account login --export account-name ReadOnlyAccess)"
export IDENTITY_CLIENT_SECRET="$(getClientSecretParameter)"

./gradlew build

aws ecr get-login-password | docker login -u AWS --password-stdin ${ECR_ACCOUNT_ID}.dkr.ecr.eu-west-1.amazonaws.com
docker build -t "service-name:${DOCKER_TAG}" .

yarn --cwd deploy install --frozen-lockfile
yarn --cwd deploy test:ci
