#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/admin-job-common.sh"

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

if [ $# -lt 3 ]; then
  print_usage
  exit 1
fi

ENVIRONMENT="$1"
USERNAME="$2"
shift 2

REPLAY_ARGS=("$@")

validate_environment "$ENVIRONMENT"

ENV_NAME="$(get_service_url_suffix "$ENVIRONMENT")"
AUTH_TOKEN="$(get_admin_auth_token "$ENVIRONMENT" "$USERNAME")"

echo "For service call using environment: $ENV_NAME"
BASE_URL="https://single-accommodation-service-api${ENV_NAME}.hmpps.service.justice.gov.uk/admin/replay-failed-inbox-events"

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