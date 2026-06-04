#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export GRADLE_USER_HOME="$ROOT_DIR/.chatgpt/gradle-home"

mkdir -p "$GRADLE_USER_HOME"

GRADLE_ARGS=(--no-daemon --offline --max-workers=2)

if [[ "${CHATGPT_FULL_GRADLE_BUILD:-false}" == "true" ]]; then
  bash "$ROOT_DIR/gradlew" "${GRADLE_ARGS[@]}" clean build
else
  bash "$ROOT_DIR/gradlew" "${GRADLE_ARGS[@]}" testClasses
fi
