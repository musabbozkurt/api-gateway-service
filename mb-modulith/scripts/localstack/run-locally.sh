#!/usr/bin/env bash

set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

source $DIR/utils.sh

eval "$(scloud account login --export account-name PowerUserAccess)"

export IDENTITY_CLIENT_SECRET="$(getIdentityClientSecret)"

# Here we want to switch back to read only access in order to be on the safe side
eval "$(scloud account login --export account-name ReadOnlyAccess)"

cd "$DIR"/../../

./gradlew bootRun
