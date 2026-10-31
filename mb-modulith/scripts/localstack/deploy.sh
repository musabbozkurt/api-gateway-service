#!/usr/bin/env bash

set -euo pipefail
MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "$MYDIR/parameters.sh"
source "$MYDIR/utils.sh"
cd "$MYDIR/../.."

IDENTITY_SECRET="$(getIdentityClientSecret)"

#echo "Identity secret=$IDENTITY_SECRET"

yarn --cwd deploy install --prod --frozen-lockfile
yarn --cwd deploy cdk deploy --require-approval never --app ./cdk.out --context dockerTag="${DOCKER_TAG}" service-name
#  --parameters service-name:identityClientSecret=$IDENTITY_SECRET
