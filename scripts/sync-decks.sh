#!/usr/bin/env bash
# Copies vocabulary decks from the sibling swedish-study repo into app assets.
# Source of truth: ../swedish-study/vocab/ — never edit CSVs under app/.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$REPO_ROOT/../swedish-study/vocab"
DST="$REPO_ROOT/app/src/main/assets/vocab"

if [[ ! -d "$SRC" ]]; then
  echo "error: source deck dir not found: $SRC" >&2
  echo "expected swedish-study checked out next to this repo" >&2
  exit 1
fi

mkdir -p "$DST"

if ! command -v rsync >/dev/null 2>&1; then
  echo "error: rsync not found (Fedora: sudo dnf install rsync)" >&2
  exit 1
fi

# Only *.csv, keep folder structure, delete stale files, skip README etc.
rsync -av --delete \
  --include='*/' --include='*.csv' --exclude='*' \
  "$SRC/" "$DST/"

echo "decks synced to $DST"