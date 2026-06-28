#!/usr/bin/env bash
# Fix stale dex / duplicate-class errors after m4sync (e.g. androidx.activity.compose.R).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "→ Stopping Gradle daemons"
./gradlew --stop >/dev/null 2>&1 || true

echo "→ Removing local build outputs"
rm -rf app/build build .kotlin

echo "→ Clean + assembleDebug"
./gradlew clean :app:assembleDebug "$@"

echo "Done."
