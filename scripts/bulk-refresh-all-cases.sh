#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/admin-job-common.sh"

print_usage() {
  echo "Name:"
  echo "  bulk-refresh-all-cases"
  echo ""
  echo "Synopsis:"
  echo "  $0 <dev|test|preprod|prod>"
  echo ""
  echo "Description:"
  echo "  Refreshes all entries in the sas_case table"
  echo ""
  echo "Examples:"
  echo "  $0 dev"
  echo "  $0 test"
}

if [ $# -lt 1 ]; then
  print_usage
  exit 1
fi

ENVIRONMENT="$1"

validate_environment "$ENVIRONMENT"

ENV_NAME="$(get_service_url_suffix "$ENVIRONMENT")"
AUTH_TOKEN="$(get_admin_auth_token "$ENVIRONMENT")"

echo "For service call using environment: $ENVIRONMENT"
BASE_URL="https://single-accommodation-service-api${ENV_NAME}.hmpps.service.justice.gov.uk/admin/bulk-refresh-all-cases"

echo "Refreshing all cases using $BASE_URL..."

curl --silent --fail --show-error \
  -X POST \
  "$BASE_URL" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "dryRun": false }'

echo "Bulk refresh request submitted successfully"
