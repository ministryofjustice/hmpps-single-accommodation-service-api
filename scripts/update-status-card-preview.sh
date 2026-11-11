#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

print_usage() {
  echo "Name:"
  echo "  update-status-card-preview - update the local status card preview"
  echo ""
  echo "Synopsis:"
  echo "  $0 [ui-branch]"
  echo ""
  echo "Description:"
  echo "  Downloads the preview from the latest successful UI pipeline run on the branch (default: main)."
  echo ""
  echo "Examples:"
  echo "  $0"
  echo "  $0 my-branch"
}

if [ $# -gt 1 ]; then
  print_usage
  exit 1
fi

if [ "${1:-}" = "-h" ] || [ "${1:-}" = "--help" ]; then
  print_usage
  exit 0
fi

BRANCH="${1:-main}"
REPO="ministryofjustice/hmpps-single-accommodation-service-ui"
PREVIEW="query/src/main/resources/eligibility/status-card-preview.html"
GRAPH="docs/eligibility-rules-graph.html"
MARKDOWN="docs/eligibility-rules-graph.md"
HELPER="scripts/status-card-preview.cjs"

cd "$ROOT_DIR"

RUN_ID=$(gh api --method GET "repos/$REPO/actions/workflows/pipeline.yml/runs" \
  -f branch="$BRANCH" -f status=success -f per_page=1 \
  --jq '.workflow_runs[0].id // empty')
if [ -z "$RUN_ID" ]; then
  echo "Error: No successful UI pipeline run found for branch: $BRANCH" >&2
  exit 1
fi

SHA=$(gh api "repos/$REPO/actions/runs/$RUN_ID" | node "$HELPER" verify-run "$BRANCH")

TEMP_DIR=$(mktemp -d)
cleanup() {
  rm -f -- "$TEMP_DIR/status-card-preview.html"
  if ! rmdir -- "$TEMP_DIR"; then
    echo "Error: Could not remove temporary artifact directory: $TEMP_DIR" >&2
  fi
}
trap cleanup EXIT

echo "Downloading preview from UI branch '$BRANCH', run $RUN_ID ($SHA)."
if ! gh run download "$RUN_ID" --repo "$REPO" --name status-card-preview --dir "$TEMP_DIR"; then
  echo "Error: Could not download the status-card-preview artifact from run $RUN_ID; it may be missing or expired." >&2
  exit 1
fi

SOURCE="$TEMP_DIR/status-card-preview.html"
if [ ! -f "$SOURCE" ]; then
  echo "Error: Artifact does not contain status-card-preview.html." >&2
  exit 1
fi
CHANGED=$(node "$HELPER" preview-changed "$SOURCE" "$PREVIEW" "$SHA")
if [ "$CHANGED" = "false" ]; then
  echo "Status card preview is unchanged."
  exit 0
fi

if [ -n "$(git status --porcelain --untracked-files=all -- "$PREVIEW" "$GRAPH" "$MARKDOWN")" ]; then
  echo "Error: Preview or graph files have local changes. Preserve them before running this command." >&2
  exit 1
fi

cp -- "$SOURCE" "$PREVIEW"
if ! ./gradlew :query:generateEligibilityRulesGraphs --no-daemon; then
  echo "Error: Graph generation failed; the preview has been updated and graph files may be partially updated." >&2
  exit 1
fi
echo "Updated the local preview and eligibility rules graphs. Review the changes before committing."
