#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/admin-job-common.sh"

print_usage() {
  echo "Name:"
  echo "  bulk-load-cases - bulk load cases for one or more team codes"
  echo ""
  echo "Synopsis:"
  echo "  $0 <dev|test|preprod|prod> <teamCode1> [teamCode2 ...] [--dry-run=true|false]"
  echo ""
  echo "Description:"
  echo "  Submits a bulk case load request for the specified team codes."
  echo ""
  echo "Examples:"
  echo "  $0 dev A1234 B5678"
  echo "  $0 test A1234 B5678 --dry-run=false"
}

if [ $# -lt 2 ]; then
  print_usage
  exit 1
fi

ENVIRONMENT="$1"
shift 1

TEAM_CODES=()
DRY_RUN=true

while [ $# -gt 0 ]; do
  case "$1" in
    --dry-run=true|--dry-run)
      DRY_RUN=true
      shift
      ;;
    --dry-run=false)
      DRY_RUN=false
      shift
      ;;
    --live-run)
      DRY_RUN=false
      shift
      ;;
    --help|-h)
      print_usage
      exit 0
      ;;
    *)
      TEAM_CODES+=("$1")
      shift
      ;;
  esac
done

if [ ${#TEAM_CODES[@]} -eq 0 ]; then
  echo "Error: At least one team code is required" >&2
  exit 1
fi

validate_environment "$ENVIRONMENT"

ENV_NAME="$(get_service_url_suffix "$ENVIRONMENT")"
AUTH_TOKEN="$(get_admin_auth_token "$ENVIRONMENT")"

echo "For service call using environment: $ENV_NAME"
BASE_URL="https://single-accommodation-service-api${ENV_NAME}.hmpps.service.justice.gov.uk/admin/bulk-load-cases"

TEAM_CODES_JSON=$(printf '%s\n' "${TEAM_CODES[@]}" | jq -R . | jq -s .)
REQUEST_BODY=$(jq -n --argjson teamCodes "$TEAM_CODES_JSON" --argjson dryRun "$DRY_RUN" '{teamCodes: $teamCodes, dryRun: $dryRun}')

echo "Submitting bulk load request for teams:"
printf '  %s\n' "${TEAM_CODES[@]}"
echo "dryRun=$DRY_RUN"

curl --fail --show-error \
  -X POST \
  "$BASE_URL" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$REQUEST_BODY"

echo
echo "Bulk load request submitted successfully"
