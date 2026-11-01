export AWS_DEFAULT_REGION="eu-west-1"

export SERVICE="service-name"

export VERTICAL=template-vertical
export SEGMENT=group-name
export USECASE=template-usecase
export TEAM=team-name

export VERSION="${VERSION:-latest}"
export BRANCH_NAME="${BRANCH_NAME:-$(git rev-parse --abbrev-ref HEAD)}"
export DOCKER_TAG="${BRANCH_NAME//[^a-zA-Z0-9]/-}-${VERSION}"

export ECR_ACCOUNT_ID=ECR_ACCOUNT_ID

export CLIENT_SECRET="parameter-store-parameter-name"
