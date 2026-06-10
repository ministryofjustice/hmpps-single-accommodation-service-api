#!/usr/bin/env sh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

MIGRATION_DIR="$SCRIPT_DIR/../infrastructure/src/main/resources/db/migration"

for file in $(find "$MIGRATION_DIR" -name "V*.sql"); do
  name=$(basename "$file")

  case "$name" in
    V[0-9][0-9][0-9][0-9]__*.sql)
      ;;
    *)
      echo "Invalid Flyway Migration filename: $name"
      exit 1
      ;;
  esac
done