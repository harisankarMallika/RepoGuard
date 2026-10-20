#!/usr/bin/env bash
set -euo pipefail
repo="$(cd "${1:?Usage: install-hook.sh /path/to/repository}" && pwd)"
root="$(cd "$(dirname "$0")/.." && pwd)"
jar="$root/target/repoguard-1.0.0.jar"
if [[ ! -f "$jar" ]]; then (cd "$root" && mvn -DskipTests package); fi
hook_dir="$(git -C "$repo" rev-parse --git-path hooks)"
mkdir -p "$repo/$hook_dir"
cat > "$repo/$hook_dir/pre-commit" <<EOF
#!/usr/bin/env bash
set -e
java -jar "$jar" --scan "\$(git -C "$repo" rev-parse --show-toplevel)"
EOF
chmod +x "$repo/$hook_dir/pre-commit"
echo "RepoGuard pre-commit hook installed in $repo"
