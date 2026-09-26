#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NAMESPACE="ms-reto-aws"

for command in minikube kubectl; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    echo "Required command not found: ${command}" >&2
    exit 1
  fi
done

if ! minikube status >/dev/null 2>&1; then
  minikube start --driver=docker --cpus=2 --memory=3000
fi

minikube addons enable ingress
minikube image build --tag ms-reto-aws:hu-11 "${ROOT_DIR}"

kubectl apply -f "${ROOT_DIR}/kubernetes/namespace.yaml"

read -r -s -p "Local PostgreSQL password: " database_password
echo
if [[ -z "${database_password}" ]]; then
  echo "The database password cannot be empty" >&2
  exit 1
fi

kubectl create secret generic ms-reto-aws-db \
  --namespace "${NAMESPACE}" \
  --from-literal=DB_USERNAME=retoaws_app \
  --from-literal=DB_PASSWORD="${database_password}" \
  --dry-run=client \
  --output=yaml | kubectl apply -f -
unset database_password

kubectl apply --kustomize "${ROOT_DIR}/kubernetes"
kubectl rollout status statefulset/postgres --namespace "${NAMESPACE}" --timeout=180s
kubectl rollout status deployment/ms-reto-aws --namespace "${NAMESPACE}" --timeout=300s

echo
echo "Kubernetes resources are ready."
echo "Run the following command in another terminal:"
echo "kubectl port-forward -n ingress-nginx service/ingress-nginx-controller 8081:80"
echo "Then verify the Ingress with:"
echo "curl -H 'Host: ms-reto-aws.local' http://127.0.0.1:8081/api/v1/actuator/health"
