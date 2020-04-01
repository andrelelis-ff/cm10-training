#!/usr/bin/env bash
echo "Creating MongoDB users..."
mongo admin --host localhost -u ${MONGO_INITDB_ROOT_USERNAME} -p ${MONGO_INITDB_ROOT_PASSWORD} --eval "db.createUser({user: 'coremedia', pwd: 'coremedia', roles: ['userAdminAnyDatabase', 'dbAdminAnyDatabase', 'readWriteAnyDatabase']});"
echo "MongoDB users created."
