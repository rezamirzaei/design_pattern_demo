#!/usr/bin/env bash
#
# Sets up Git hooks for local development.
# Run once after cloning: ./scripts/setup-hooks.sh
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
HOOKS_DIR="$PROJECT_ROOT/.git/hooks"

echo "📂 Project root: $PROJECT_ROOT"

if [ ! -d "$HOOKS_DIR" ]; then
    echo "❌ Not a git repository. Run 'git init' first."; exit 1
fi

# Install pre-commit hook
if [ -f "$HOOKS_DIR/pre-commit" ]; then
    echo "⚠️  Existing pre-commit hook found — backing up to pre-commit.bak"
    cp "$HOOKS_DIR/pre-commit" "$HOOKS_DIR/pre-commit.bak"
fi

cp "$SCRIPT_DIR/pre-commit" "$HOOKS_DIR/pre-commit"
chmod +x "$HOOKS_DIR/pre-commit"
chmod +x "$SCRIPT_DIR/pre-commit"
echo "✅ Pre-commit hook installed at $HOOKS_DIR/pre-commit"

echo ""
echo "🎉 Git hooks setup complete!"
echo "   The pre-commit hook runs: conflict check → debug check → compile → tests"
echo "   To bypass (emergency only): git commit --no-verify"
echo ""
