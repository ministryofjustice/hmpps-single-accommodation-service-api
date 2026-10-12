#!/bin/bash

set -euo pipefail

print_usage() {
  echo "Name:"
  echo "  replay-failed-inbox-events - replay failed inbox events"
  echo ""
  echo "Synopsis:"
  echo "  $0 <dev|test|preprod|prod> <username> <all|uuid1 [uuid2 ...]>"
  echo ""
  echo "Description:"
  echo "  Replays failed inbox events in the specified environment. You can either replay all failed"
  echo "  events or specify individual event UUIDs."
  echo ""
  echo "Examples:"
  echo "  $0 dev john.smith all"
  echo "  $0 test john.smith 8ccddd53-df2d-4ead-bd8c-b72211c20371"
  echo "  $0 preprod john.smith uuid1 uuid2 uuid3"
}

#
# Validate arguments
#
if [ $# -lt 3 ]; then
  print_usage
  exit 1
fi

ENVIRONMENT="$1"
USERNAME="$2"
shift 2

REPLAY_ARGS=("$@")

#
# Validate environment
#
case "$ENVIRONMENT" in
  dev|test|preprod|prod)
    ;;
  *)
    echo "Error: Environment must be one of dev, test, preprod or prod"
    exit 1
    ;;
esac

#
# Environment-specific values
#
NAMESPACE="hmpps-community-accommodation-$ENVIRONMENT"
SECRET="hmpps-single-accommodation-service-api"

# For the auth token request, test environment uses -dev
# But the service API endpoint uses the actual environment name
if [ "$ENVIRONMENT" = "prod" ]; then
  AUTH_ENV_NAME=""
  ENV_NAME=""
elif [ "$ENVIRONMENT" = "test" ]; then
  AUTH_ENV_NAME="-dev"
  ENV_NAME="-test"
else
  AUTH_ENV_NAME="-$ENVIRONMENT"
  ENV_NAME="-$ENVIRONMENT"
fi

echo "Using environment: $ENVIRONMENT"
echo "Using username: $USERNAME"

#
# Get client credentials from Kubernetes secret
#
AUTH_STRING=$(kubectl -n "$NAMESPACE" get secrets "$SECRET" -o json | jq -r '.data | map_values(@base64d) | "\(.ADMIN_CLIENT_SINGLE_ACCOMMODATION_SERVICE_ID):\(.ADMIN_CLIENT_SINGLE_ACCOMMODATION_SERVICE_SECRET)"')

CLIENT_AUTH=$(echo -n "$AUTH_STRING" | base64 -b 0)

#
# Get a running pod
#
POD=$(kubectl -n "$NAMESPACE" get pods --field-selector=status.phase=Running -o jsonpath="{.items[0].metadata.name}")

if [ -z "$POD" ]; then
  echo "Error: No running pod found in namespace $NAMESPACE"
  exit 1
fi

echo "Using pod: $POD"

#
# Obtain JWT token
#
echo "Obtaining auth token..."

AUTH_TOKEN=$(kubectl -n "$NAMESPACE" exec "$POD" -- wget -q -O - --post-data "" "https://sign-in$AUTH_ENV_NAME.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=client_credentials&username=$USERNAME&auth_source=delius" --header "Authorization: Basic $CLIENT_AUTH" | jq -r .access_token)

if [ -z "$AUTH_TOKEN" ] || [ "$AUTH_TOKEN" = "null" ]; then
  echo "Error: Failed to obtain auth token"
  exit 1
fi

echo "Successfully obtained auth token"

BASE_URL="https://single-accommodation-service-api$ENV_NAME.hmpps.service.justice.gov.uk/admin/replay-failed-inbox-events"

#
# Replay all events
#
if [ "${#REPLAY_ARGS[@]}" -eq 1 ] && [ "${REPLAY_ARGS[0]}" = "all" ]; then
  echo "Replaying ALL failed inbox events..."

  curl --fail --silent --show-error \
    -X PUT \
    "$BASE_URL?replayAll=true" \
    -H "Authorization: Bearer $AUTH_TOKEN"

  echo
  echo "Replay request submitted successfully"
  exit 0
fi

#
# Replay specific UUIDs
#
UUID_JSON=$(printf '%s\n' "${REPLAY_ARGS[@]}" | jq -R . | jq -s .)

echo "Replaying specific failed inbox events:"
printf '  %s\n' "${REPLAY_ARGS[@]}"

curl --fail --silent --show-error \
  -X PUT \
  "$BASE_URL?replayAll=false" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$UUID_JSON"

echo
echo "Replay request submitted successfully"