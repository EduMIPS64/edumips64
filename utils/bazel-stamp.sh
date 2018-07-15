#!/bin/bash
#
# Tool to build key/value pairs that will be injected as variables in Bazel
# build files if the tool is invoked through --workspace_status_command.

# Volatile variable with the current build time.
echo "CURRENT_TIME $(date)"

# Stable variable with the most recent Git commit.
echo "STABLE_GIT_COMMIT $(git rev-parse HEAD)"
