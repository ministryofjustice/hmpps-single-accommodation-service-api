#!/bin/bash

normalize_pod_name() {
  local raw_name="$1"
  local normalized

  normalized=$(printf '%s' "$raw_name" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9-]+/-/g; s/^-+//; s/-+$//; s/-+/-/g')
  normalized=${normalized:0:63}
  normalized=${normalized%-}

  if [ -z "${normalized}" ]; then
    echo "Unable to derive a valid port-forward pod name from input: '$raw_name'" >&2
    return 1
  fi

  printf '%s\n' "$normalized"
}

