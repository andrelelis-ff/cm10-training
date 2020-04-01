#!/usr/bin/env bash
SCRIPT=$(readlink -f $0)
SCRIPTPATH=`dirname $SCRIPT`

if [ "$#" -ne 1 ]
then
  echo "Usage: ./set-blueprint-version.sh <NEW VERSION>"
  exit 1
fi

set -eu
BASE_PATH=${SCRIPTPATH}/../..
export OLD_VERSION=${BLUEPRINT_DEFAULT_VERSION:-1-SNAPSHOT}
export NEW_VERSION=$1

# grep -l "${OLD_VERSION}" "$0"
echo "replacing $OLD_VERSION with $NEW_VERSION in:"
find ${BASE_PATH} -type f \( -name pom.xml -or -name "*.rb" -or -name "*.md" -or -name "*.json" \) \
 -and -not \( -path "*/.git/*" -or -path "*/node_modules/*" \) \
 -exec sh -c '
 grep -l "${OLD_VERSION}" "$0" && sed -i "s#${OLD_VERSION}#${NEW_VERSION}#g" "$0"
' {} ';' -prune
