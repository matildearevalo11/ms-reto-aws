#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="${ROOT_DIR}/cloudformation/build"
TEMPLATE="${ROOT_DIR}/cloudformation/template.yaml"

AWS_REGION="${AWS_REGION:-us-east-1}"
STAGE="${STAGE:-dev}"
STACK_NAME="${STACK_NAME:-ms-reto-aws-users-cloudformation}"
NOTIFICATION_EMAIL="${NOTIFICATION_EMAIL:-}"

for command in aws shasum; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

"${ROOT_DIR}/cloudformation/build.sh"

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
ARTIFACT_BUCKET="${ARTIFACT_BUCKET:-ms-reto-aws-cfn-artifacts-${ACCOUNT_ID}-${AWS_REGION}}"
JAVA_HASH="$(shasum -a 256 "${BUILD_DIR}/users-java.jar" | cut -d ' ' -f 1)"
NODE_HASH="$(shasum -a 256 "${BUILD_DIR}/users-node.zip" | cut -d ' ' -f 1)"
JAVA_KEY="cloudformation/${STAGE}/users-java-${JAVA_HASH}.jar"
NODE_KEY="cloudformation/${STAGE}/users-node-${NODE_HASH}.zip"

if ! aws s3api head-bucket --bucket "${ARTIFACT_BUCKET}" 2>/dev/null; then
  if [[ "${AWS_REGION}" == "us-east-1" ]]; then
    aws s3api create-bucket --bucket "${ARTIFACT_BUCKET}" --region "${AWS_REGION}"
  else
    aws s3api create-bucket \
      --bucket "${ARTIFACT_BUCKET}" \
      --region "${AWS_REGION}" \
      --create-bucket-configuration "LocationConstraint=${AWS_REGION}"
  fi
  aws s3api put-public-access-block \
    --bucket "${ARTIFACT_BUCKET}" \
    --public-access-block-configuration \
      BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
  aws s3api put-bucket-encryption \
    --bucket "${ARTIFACT_BUCKET}" \
    --server-side-encryption-configuration \
      '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
  aws s3api put-bucket-versioning \
    --bucket "${ARTIFACT_BUCKET}" \
    --versioning-configuration Status=Enabled
fi

aws s3 cp "${BUILD_DIR}/users-java.jar" "s3://${ARTIFACT_BUCKET}/${JAVA_KEY}" --only-show-errors
aws s3 cp "${BUILD_DIR}/users-node.zip" "s3://${ARTIFACT_BUCKET}/${NODE_KEY}" --only-show-errors

aws cloudformation deploy \
  --template-file "${TEMPLATE}" \
  --stack-name "${STACK_NAME}" \
  --capabilities CAPABILITY_NAMED_IAM \
  --no-fail-on-empty-changeset \
  --region "${AWS_REGION}" \
  --parameter-overrides \
    "Stage=${STAGE}" \
    "ArtifactBucket=${ARTIFACT_BUCKET}" \
    "JavaArtifactKey=${JAVA_KEY}" \
    "NodeArtifactKey=${NODE_KEY}" \
    "NotificationEmail=${NOTIFICATION_EMAIL}"

aws cloudformation describe-stacks \
  --stack-name "${STACK_NAME}" \
  --region "${AWS_REGION}" \
  --query 'Stacks[0].Outputs' \
  --output table
