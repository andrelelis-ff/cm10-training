#!/usr/bin/env bash

set -e

# ======================================================================
#
# This script copies all Run Configurations from this project to your
# .idea/runConfigurations folder
#
# ======================================================================

GIT=$(which git)
if [[ -z "${GIT}" ]]; then
  echo "[ERROR] Could not detect git. Please set Git command explicitly."
  exit 1
fi

PROJECT_ROOT="$("${GIT}" rev-parse --show-toplevel)"
echo "Detected project root: ${PROJECT_ROOT}"

RUN_CONFIG_FOLDER="${PROJECT_ROOT}/.idea/runConfigurations"

echo "Create folder ${RUN_CONFIG_FOLDER}"
mkdir -p "${RUN_CONFIG_FOLDER}"

echo "Search for run configurations"
RUN_CONFIGS_ARRAY=($(find ${PROJECT_ROOT} -path "*/ideaRunConfigurations/*" -type f -name "*.xml"))

#RUN_CONFIGS_ARRAY=(${RUN_CONFIGS})
echo "Copy ${#RUN_CONFIGS_ARRAY[@]} run configurations to ${RUN_CONFIG_FOLDER}"
cp "${RUN_CONFIGS_ARRAY[@]}" "${RUN_CONFIG_FOLDER}"

echo "Please re-open your project to make the run configurations available."

exit 0
