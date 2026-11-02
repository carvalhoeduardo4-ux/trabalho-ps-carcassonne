#!/usr/bin/env bash
set -euo pipefail

backend_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
mvn --batch-mode --no-transfer-progress -f "$backend_dir/pom.xml" verify "$@"
"$backend_dir/scripts/smoke-backend.sh"
