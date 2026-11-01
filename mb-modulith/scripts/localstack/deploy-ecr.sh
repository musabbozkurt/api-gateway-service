#!/usr/bin/env bash

set -euo pipefail
MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "$MYDIR/parameters.sh"
cd "$MYDIR/../.."

yarn --cwd deploy install --frozen-lockfile
yarn --cwd deploy cdk deploy --strict --require-approval never --exclusively service-name-ecr
