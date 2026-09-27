#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="${ROOT_DIR}/cloudformation/build"
JAVA_DIR="${ROOT_DIR}/serverless/java"
NODE_DIR="${ROOT_DIR}/serverless/node"

run_maven() {
  if command -v java >/dev/null 2>&1 && java -version 2>&1 | grep -q 'version "21'; then
    (cd "${JAVA_DIR}" && ./mvnw clean package)
  elif command -v mise >/dev/null 2>&1; then
    (cd "${JAVA_DIR}" && mise exec java@21 -- ./mvnw clean package)
  elif [[ -x /opt/homebrew/bin/mise ]]; then
    (cd "${JAVA_DIR}" && /opt/homebrew/bin/mise exec java@21 -- ./mvnw clean package)
  else
    echo "Java 21 is required. Install it or install mise and run: mise install java@21" >&2
    exit 1
  fi
}

for command in npm zip; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}/node"

run_maven
cp "${JAVA_DIR}/target/users-java.jar" "${BUILD_DIR}/users-java.jar"

cp "${NODE_DIR}/package.json" "${NODE_DIR}/package-lock.json" "${BUILD_DIR}/node/"
cp "${NODE_DIR}/handler.js" "${NODE_DIR}/email-handler.js" "${BUILD_DIR}/node/"
npm ci --omit=dev --prefix "${BUILD_DIR}/node"
(cd "${BUILD_DIR}/node" && zip -q -r "${BUILD_DIR}/users-node.zip" .)

echo "Artifacts created:"
echo "${BUILD_DIR}/users-java.jar"
echo "${BUILD_DIR}/users-node.zip"
