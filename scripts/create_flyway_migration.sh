#!/usr/bin/env sh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

MIGRATION_DIR="$SCRIPT_DIR/../infrastructure/src/main/resources/db/migration"

NAME="${1:-<placeholder_filename>}"

# Add .sql if missing
case "$NAME" in
  *.sql) ;;
  *) NAME="${NAME}.sql" ;;
esac

LAST=$(
find "$MIGRATION_DIR" -name "V*.sql" |
xargs -n1 basename |
sed -n 's/^V\([0-9]\{4\}\).*/\1/p' |
sort -n |
tail -1
)

LAST=${LAST:-0}

NEXT=$(printf "%04d" "$((10#$LAST + 1))")

FILE="V${NEXT}__${NAME}"

touch "$MIGRATION_DIR/$FILE"

echo "Created: $MIGRATION_DIR/$FILE"