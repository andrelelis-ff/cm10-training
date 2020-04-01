#!/bin/sh
set -e
if [ "${DEBUG_ENTRYPOINT}" = "true" ]; then
  echo "[DOCKER ENTRYPOINT] - DEBUG_ENTRYPOINT detected, all commands will be printed"
  set -x
fi

./confd ../bin/true

if [ "${SKIP_CONTENT}" = "true" ]; then
  echo "[DOCKER ENTRYPOINT] - skipping entrypoint chain $@"
else
  echo "[DOCKER ENTRYPOINT] - starting entrypoint chain $@"
  export EXPORT_CONTENT_DIR=/coremedia/export
  export IMPORT_CONTENT_DIR=/coremedia/import/content
  export IMPORT_USERS_DIR=/coremedia/import/users
  # create dirs
  mkdir -p ${IMPORT_CONTENT_DIR} ${IMPORT_USERS_DIR} ${EXPORT_CONTENT_DIR}
  exec ./${@}
fi
