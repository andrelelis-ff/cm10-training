#!/bin/bash
set -e

function solr_config_append() {
  key=$1
  value="$2"
  grep "^${key}=\"${value}\"" /etc/default/solr.in.sh || echo "${key}=\"${value}\"" >> /etc/default/solr.in.sh
  echo "[DOCKER ENTRYPOINT] - set ${key}=${value} in solr config"
}

mkdir -p /var/solr/data/preview /var/solr/data/live /var/solr/data/studio

# The base image has "ENV SOLR_HOME=/var/solr/data" which does not really fit, because SOLR_HOME/lib contains JAR files.
# Let's override SOLR_HOME to <solr>/server/solr (the Solr default) and only keep Solr cores in /var/solr/data.
solr_config_append SOLR_OPTS "\$SOLR_OPTS -DcoreRootDirectory=/var/solr/data"
solr_config_append SOLR_HOME "server/solr"

# Delete unused files from the base image, which uses /var/solr/data as SOLR_HOME.
# In this CoreMedia Solr docker image, /var/solr/data is just the coreRootDirectory, while SOLR_HOME is in
# /opt/solr/server/solr and contains the effective solr.xml/zoo.cfg configuration files
rm -f /var/solr/data/zoo.cfg
rm -f /var/solr/data/solr.xml

if [[ "${PROMETHEUS}" = "true" ]]; then
  solr_config_append SOLR_OPTS "\$SOLR_OPTS -javaagent:/opt/solr/prometheus/jmx_prometheus_javaagent.jar=8199:/opt/solr/prometheus/jmx_prometheus.yml"
fi

if [ "${EXIT_ON_OOM}" = "true" ]; then
  solr_config_append SOLR_OPTS "\$SOLR_OPTS -XX:+ExitOnOutOfMemoryError"
fi

# check if solr is master or slave
if [ "${SOLR_MASTER}" = "true" ]; then
  if [ "${SOLR_SLAVE}" = "true" ]; then
    echo "ERROR - Misconfigured solr master/slave setup, you have set both SOLR_MASTER and SOLR_SLAVE to true"
    sleep 40
    exit 1
  fi
  solr_config_append SOLR_OPTS "\$SOLR_OPTS -Dsolr.master=true"
fi
if [ "${SOLR_SLAVE}" = "true" ]; then
  if [ "${SOLR_MASTER}" = "true" ]; then
    echo "ERROR - Misconfigured solr master/slave setup, you have set both SOLR_MASTER and SOLR_SLAVE to true"
    sleep 40
    exit 1
  fi
  if [ -z "${SOLR_MASTER_URL}" ]; then
    echo "ERROR - Misconfigured solr master/slave setup, you have to set SOLR_MASTER_URL"
    sleep 40
    exit 1
  fi

  # wait for solr master to be available
  is_ready() {
    eval "curl -sLf ${SOLR_MASTER_URL} > /dev/null"
  }
  i=0
  while ! is_ready ; do
    i=`expr $i + 1`
    if [ $i -ge 30 ]; then
        echo "[DOCKER ENTRYPOINT] - solr master still not ready, giving up"
        exit 1
    fi
    echo "[DOCKER ENTRYPOINT] - waiting for solr master to be ready"
    sleep 10
  done

  solr_config_append SOLR_OPTS "\$SOLR_OPTS -Dsolr.master=false -Dsolr.slave=true -Dsolr.master.url=${SOLR_MASTER_URL}"

  # automatically replicate all cores of the master
  if [ "${SOLR_SLAVE_AUTOCREATE_CORES}" = "true" ]; then
    MASTER_CORES_READY=false
    CORES_URL="${SOLR_MASTER_URL}/admin/cores?action=STATUS&wt=json"
    SOLR_SLAVE_AUTOCREATE_THRESHOLD=3

    while [ "${MASTER_CORES_READY}" = "false" ] ; do
      rm -f /tmp/cores.json
      curl -so /tmp/cores.json "${CORES_URL}"
      CORES_FOUND=$(jq --raw-output '.status | keys | .[]' /tmp/cores.json)

      if [ -n "${SOLR_SLAVE_AUTOCREATE_CORES_LIST}" ]; then
        # if there is a list to autocreate, wait for them to be available and then create them
        CORES_NOT_READY=""
        for CORE in ${SOLR_SLAVE_AUTOCREATE_CORES_LIST}; do
          jq -e '.status | .'${CORE}' | .uptime' /tmp/cores.json > /dev/null || CORES_NOT_READY=$(echo "${CORE} ${CORES_NOT_READY}")
        done
        if [ -z "${CORES_NOT_READY}" ]; then
          echo "[DOCKER ENTRYPOINT] - solr cores \"${SOLR_SLAVE_AUTOCREATE_CORES_LIST}\" ready on master"
          MASTER_CORES_READY=true
        else
          echo "[DOCKER ENTRYPOINT] - solr cores \"${CORES_NOT_READY}\" not found"
        fi
      else
        # if no list to autocreate is given, check if there are at least 3 cores available and then create all found ones
        # make sure we wait for at least studio, live and preview core, therefore the default threshold is 3
        test $(echo "${CORES_FOUND}" | wc -w) -ge ${SOLR_SLAVE_AUTOCREATE_THRESHOLD} && MASTER_CORES_READY=true
      fi

      i=`expr $i + 1`
      if [ $i -ge 30 ]; then
        echo "[DOCKER ENTRYPOINT] - still no solr cores found in the master, giving up"
        exit 1
      fi
      if [ "${MASTER_CORES_READY}" != "true" ]; then
        echo "[DOCKER ENTRYPOINT] - waiting for cores to be created on solr master"
        sleep 30
      fi
    done

    if [ -n "${SOLR_SLAVE_AUTOCREATE_CORES_LIST}" ]; then
      # use defined list
      CORES_TO_REPLICATE="${SOLR_SLAVE_AUTOCREATE_CORES_LIST}"
    else
      # try to create all found ones
      CORES_TO_REPLICATE=${CORES_FOUND}
    fi

    for CORE in ${CORES_TO_REPLICATE}; do
      CORE_DIR=/var/solr/data/${CORE}
      if [ ! -f ${CORE_DIR}/core.json ]; then
        INSTANCE_DIR=$(jq --raw-output ".status.\"${CORE}\".instanceDir" /tmp/cores.json)
        CONFIG_SET=${INSTANCE_DIR##*/}
        echo "[DOCKER ENTRYPOINT] - creating core ${CORE} with configSet ${CONFIG_SET}"
        mkdir -p ${CORE_DIR}
        cat << EOF > ${CORE_DIR}/core.properties
# generated by entrypoint script
name=${CORE}
dataDir=data
configSet=${CONFIG_SET}
EOF
      fi
    done
  fi
fi
