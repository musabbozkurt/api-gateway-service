#!/usr/bin/env bash

set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

source $DIR/utils.sh
source $DIR/parameters.sh

eval "$(scloud account login --export account-name ReadOnlyAccess)"

export IDENTITY_CLIENT_SECRET="$(getClientSecretParameter)"

cd "$DIR"/../../

./gradlew build
