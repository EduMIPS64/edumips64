#!/bin/bash
#
# Tool to build key/value pairs that will be injected as variables in Bazel
# build files if the tool is invoked through --workspace_status_command.

# Volatile variable with the current build time.
echo "CURRENT_TIME $(date)"

# Stable variable with the most recent Git commit.
repo=$(basename $(git rev-parse --show-toplevel 2>/dev/null))
branch=$(git symbolic-ref HEAD 2>/dev/null)
branch=${branch##refs/heads/}
rev=$(git rev-parse --short HEAD 2>/dev/null)

echo "STABLE_GIT_COMMIT ${repo}:${branch}:${rev}"
