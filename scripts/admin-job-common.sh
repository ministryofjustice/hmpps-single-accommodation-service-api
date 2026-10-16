#!/bin/bash

validate_environment() {
  local environment="${1:-}"

  case "$environment" in
    dev|test|preprod|prod)
      ;;
    *)
      echo "Error: Environment must be one of dev, test, preprod or prod" >&2
      exit 1
      ;;
  esac
}

get_service_url_suffix() {
  local environment="$1"

  case "$environment" in
    prod)
      echo ""
      ;;
    *)
      echo "-$environment"
      ;;
  esac
}

get_admin_auth_token() {
  local environment="$1"
  local username="$2"
  local namespace="hmpps-community-accommodation-$environment"
  local secret="hmpps-single-accommodation-service-api"

  # For the auth token request, test environment uses -dev
  # But the service API endpoint uses the actual environment name
  local auth_env_name
  if [ "$environment" = "prod" ]; then
    auth_env_name=""
  elif [ "$environment" = "test" ]; then
    auth_env_name="-dev"
  else
    auth_env_name="-$environment"
  fi

  echo "For auth token using environment: $environment" >&2
  echo "For auth token using username: $username" >&2

  #
  # Get client credentials from Kubernetes secret
  #
  local auth_string
  auth_string=$(kubectl -n "$namespace" get secrets "$secret" -o json | jq -r '.data | map_values(@base64d) | "\(.ADMIN_CLIENT_SINGLE_ACCOMMODATION_SERVICE_ID):\(.ADMIN_CLIENT_SINGLE_ACCOMMODATION_SERVICE_SECRET)"')

  local client_auth
  client_auth=$(printf '%s' "$auth_string" | base64 | tr -d '\n')

  #
  # Get a running hmpps-single-accommodation-service-ui pod. The UI pods have wget installed.
  #
  local pod
  pod=$(kubectl -n "$namespace" get pods -l app=hmpps-single-accommodation-service-ui --field-selector=status.phase=Running -o jsonpath="{.items[0].metadata.name}")

  if [ -z "$pod" ]; then
    echo "Error: No running hmpps-single-accommodation-service-ui pod found in namespace $namespace" >&2
    exit 1
  fi

  echo "Using pod for auth request: $pod" >&2
  echo "Obtaining auth token..." >&2

  local auth_token
  auth_token=$(kubectl -n "$namespace" exec "$pod" -- wget -q -O - --post-data "" "https://sign-in${auth_env_name}.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=client_credentials&username=$username&auth_source=delius" --header "Authorization: Basic $client_auth" | jq -r .access_token)

  if [ -z "$auth_token" ] || [ "$auth_token" = "null" ]; then
    echo "Error: Failed to obtain auth token" >&2
    exit 1
  fi

  echo "Successfully obtained auth token" >&2
  echo "$auth_token"
}
