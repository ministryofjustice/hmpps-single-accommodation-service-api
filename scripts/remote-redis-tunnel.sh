#!/bin/bash

trap "pkill -f 'port-forward'" EXIT

LOCAL_PORT=6380
REDIS_SECRET_NAME=
PORT_FORWARD_CONTAINER_NAME="redis-port-forward-${USER//.}"
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
  echo "You must specify the environment: ./remote-redis-tunnel.sh [-d] [-c] [dev|test|preprod|prod]"
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
else
  kubectl -n "$NAMESPACE" port-forward pod/"$PORT_FORWARD_CONTAINER_NAME" "$LOCAL_PORT:6379"
  PF_PID=$!

  echo
  echo "Connect with:"
  echo "redis-cli -h 127.0.0.1 -p $LOCAL_PORT --tls -a <REDIS_AUTH_TOKEN>"
  echo
fi

wait $PF_PID

echo "Cleaning up..."
kubectl -n "$NAMESPACE" delete pod "$PORT_FORWARD_CONTAINER_NAME"
