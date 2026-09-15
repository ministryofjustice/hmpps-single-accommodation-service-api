#!/bin/bash

# so the script works when ran from any directory
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
source "$SCRIPT_DIR/pod-name-utils.sh"

PF_PID=

cleanup() {
  if [ -n "${PF_PID}" ] && kill -0 "${PF_PID}" 2>/dev/null; then
    kill "${PF_PID}" 2>/dev/null
  fi

  if [ -n "${PORT_FORWARD_CONTAINER_NAME}" ] && [ -n "${NAMESPACE}" ]; then
    echo "Cleaning up..."
    kubectl -n "$NAMESPACE" delete pod "$PORT_FORWARD_CONTAINER_NAME" --ignore-not-found >/dev/null 2>&1
  fi
}

trap cleanup EXIT

print_usage() {
  echo "Name:"
  echo "  remote_redis_tunnel - create a local tunnel to remote Redis"
  echo ""
  echo "Synopsis:"
  echo "  ./remote-redis-tunnel.sh [-d] [-c] [-s secret-name] [dev|test|preprod|prod]"
  echo ""
  echo "Options:"
  echo "  -c    Start redis-cli after port forwarding is ready"
  echo "  -d    Display Redis auth token for local CLI/IDE connection"
  echo "  -s    Redis secret name (default: sas-elasticache-redis)"
}

LOCAL_PORT=6379
REDIS_SECRET_NAME=
if ! PORT_FORWARD_CONTAINER_NAME=$(normalize_pod_name "sas-redis-port-forward-${USER:-unknown}")
then
  exit 1
fi
IMAGE="ministryofjustice/port-forward"
DISPLAY_CREDS=0
START_CLI=0

while getopts "dcs:" flag; do
  case "$flag" in
    d)
      DISPLAY_CREDS=1
      ;;
    c)
      START_CLI=1
      ;;
    s)
      REDIS_SECRET_NAME=${OPTARG}
      ;;
    \?)
      echo "ERROR: Invalid option -$OPTARG"
      exit 1
      ;;
  esac
done

shift $((OPTIND - 1))
ENV=$1

if [ -z "$ENV" ] || [[ ! "$ENV" =~ ^(dev|test|preprod|prod)$ ]]; then
  print_usage
  exit 1
fi

NAMESPACE="hmpps-community-accommodation-$ENV"
[ -z "${REDIS_SECRET_NAME}" ] && REDIS_SECRET_NAME='sas-elasticache-redis'

echo "---------------------------------------------------------------"
echo "* Namespace: $NAMESPACE"
echo "* Remote Port: 6379"
echo "* Local Port: $LOCAL_PORT"
echo "* Redis Secret Name: $REDIS_SECRET_NAME"
echo "* Pod Name: $PORT_FORWARD_CONTAINER_NAME"
echo "---------------------------------------------------------------"

# Get Redis connection details
secrets=$(kubectl get secrets "$REDIS_SECRET_NAME" -n "$NAMESPACE" -o json | jq ".data | map_values(@base64d)")

REDIS_HOST=$(echo "$secrets" | jq -r '.primary_endpoint_address')
REDIS_REPLICA_HOST=$(echo "$secrets" | jq -r '.reader_endpoint_address')
REDIS_AUTH_TOKEN=$(echo "$secrets" | jq -r '.auth_token')

if [ "$DISPLAY_CREDS" -eq 1 ]; then
  echo ""
  echo "* Host (via tunnel): localhost"
  echo "* Port: $LOCAL_PORT"
  echo "* Auth Token: $REDIS_AUTH_TOKEN"
  echo ""
fi

echo "Checking for existing pod..."
kubectl get pod "$PORT_FORWARD_CONTAINER_NAME" -n "$NAMESPACE" >/dev/null 2>&1

if [ $? -ne 0 ]; then
  echo "Creating port-forward pod..."

  kubectl -n "$NAMESPACE" run "$PORT_FORWARD_CONTAINER_NAME" \
    --image="$IMAGE" \
    --port="6379" \
    --env="REMOTE_HOST=$REDIS_HOST" \
    --env="REMOTE_PORT=6379" \
    --env="LOCAL_PORT=6379"

  kubectl wait --for=condition=ready pod/"$PORT_FORWARD_CONTAINER_NAME" -n "$NAMESPACE"
fi

echo "Starting port forward..."

if [ "$START_CLI" -eq 1 ]; then
  kubectl -n "$NAMESPACE" port-forward pod/"$PORT_FORWARD_CONTAINER_NAME" "$LOCAL_PORT:6379" &
  PF_PID=$!
  sleep 2

  echo "Launching redis-cli..."
  redis-cli -h 127.0.0.1 -p "$LOCAL_PORT" --tls -a "$REDIS_AUTH_TOKEN"

  if [ -n "${PF_PID}" ] && kill -0 "${PF_PID}" 2>/dev/null; then
    echo "Stopping background port-forward process $PF_PID"
    kill "${PF_PID}" 2>/dev/null
    wait "${PF_PID}" 2>/dev/null || true
    PF_PID=
  fi
else
  kubectl -n "$NAMESPACE" port-forward pod/"$PORT_FORWARD_CONTAINER_NAME" "$LOCAL_PORT:6379" &
  PF_PID=$!
  sleep 2

  echo
  echo "Connect with:"
  echo "redis-cli -h 127.0.0.1 -p $LOCAL_PORT --tls -a <REDIS_AUTH_TOKEN>"
  echo
fi

if [ -n "${PF_PID}" ]; then
  wait "$PF_PID"
fi
