#!/usr/bin/env bash
SCRIPT=$(readlink -f $0)
SCRIPTPATH=`dirname $SCRIPT`

if [ "$#" -ne 1 ]
then
  echo "Usage: ./set-blueprint-groupId.sh <NEW_GROUP_ID>"
  exit 1
fi

set -eu
BASE_PATH=${SCRIPTPATH}/../..
export OLD_GROUP_ID=${BLUEPRINT_DEFAULT_GROUP_ID:-com.coremedia.blueprint}
export NEW_GROUP_ID=$1

# \x27 is the UTF-8 encoding for escaped single quote
echo "replace groupId $OLD_GROUP_ID with $NEW_GROUP_ID in:"
find ${BASE_PATH} -type f \( -name pom.xml -or -name "*.rb" -or -name "*.md" -or -name "*.json" \) \
 -and -not \( -path "*/.git/*" -or -path "*/node_modules/*" \) \
 -exec sh -c '
 grep -l "<groupId>${OLD_GROUP_ID}\|\"${OLD_GROUP_ID}\"\|\x27${OLD_GROUP_ID}\x27" "$0"
 sed -i -e "s#<groupId>${OLD_GROUP_ID}</groupId>#<groupId>${NEW_GROUP_ID}</groupId>#g" -e "s#\"${OLD_GROUP_ID}\"#\"${NEW_GROUP_ID}\"#g" -e "s#\x27${OLD_GROUP_ID}\x27#\x27${NEW_GROUP_ID}\x27#g" "$0"
' {} ';' -prune
