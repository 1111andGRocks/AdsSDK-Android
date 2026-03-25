#!/usr/bin/env bash
# Loads central-publishing.properties into ORG_GRADLE_PROJECT_* so Vanniktech's
# providers.gradleProperty("mavenCentralUsername") etc. resolve (Gradle does not
# read that file by itself; mutating startParameter in settings.gradle.kts is ignored).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROPS="$ROOT/central-publishing.properties"
if [[ ! -f "$PROPS" ]]; then
  echo "Missing $PROPS — copy central-publishing.properties.example and fill secrets." >&2
  exit 1
fi
while IFS= read -r line || [[ -n "$line" ]]; do
  [[ "$line" =~ ^[[:space:]]*# ]] && continue
  [[ "$line" =~ ^[[:space:]]*$ ]] && continue
  if [[ "$line" =~ ^([A-Za-z0-9_.-]+)[[:space:]]*=[[:space:]]*(.*)$ ]]; then
    key="${BASH_REMATCH[1]}"
    value="${BASH_REMATCH[2]%%$'\r'}"
    # Gradle: project prop "a.b" -> ORG_GRADLE_PROJECT_a_b
    env_key="${key//./_}"
    export "ORG_GRADLE_PROJECT_${env_key}=${value}"
  fi
done < "$PROPS"

# macOS: Terminal often has no JAVA_HOME; Gradle needs a JDK (17+ for this project).
ensure_java_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    return 0
  fi
  if [[ "$(uname -s)" == "Darwin" ]] && [[ -x /usr/libexec/java_home ]]; then
    local jh
    jh="$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home 2>/dev/null || true)"
    if [[ -n "$jh" && -x "$jh/bin/java" ]]; then
      export JAVA_HOME="$jh"
      return 0
    fi
  fi
  local c
  for c in \
    /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
    /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
    /usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
    /usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home; do
    if [[ -x "$c/bin/java" ]]; then
      export JAVA_HOME="$c"
      return 0
    fi
  done
  return 1
}

if ! ensure_java_home; then
  echo "No JDK found. Set JAVA_HOME or install JDK 17, e.g.: brew install openjdk@17" >&2
  echo "Then: export JAVA_HOME=\$(/usr/libexec/java_home -v 17)" >&2
  exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"

# gpg из Gradle без TTY → «Inappropriate ioctl for device». Демон Gradle тоже отрезает терминал.
if [[ -t 1 ]]; then
  GPG_TTY="$(tty)"
  export GPG_TTY
fi

gradle_args=()
has_no_daemon=false
for a in "$@"; do
  [[ "$a" == "--no-daemon" ]] && has_no_daemon=true
  gradle_args+=("$a")
done
if ! $has_no_daemon; then
  gradle_args=("--no-daemon" "${gradle_args[@]}")
fi

exec "$ROOT/gradlew" "${gradle_args[@]}"
