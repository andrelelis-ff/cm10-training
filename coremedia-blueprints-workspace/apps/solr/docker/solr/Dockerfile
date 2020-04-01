ARG SOLR_BASE_IMAGE_TAG
ARG SOLR_BASE_IMAGE_REPO

FROM ${SOLR_BASE_IMAGE_REPO}:${SOLR_BASE_IMAGE_TAG}
LABEL maintainer="CoreMedia AG <support@coremedia.com>"
ENV SOLR_MASTER_URL=http://solr:8983/solr
# set this to true for a solr master
ENV SOLR_MASTER=false
# set this to true for a solr slave
ENV SOLR_SLAVE=false
# set to true to automatically replicate all master cores on this slave
ENV SOLR_SLAVE_AUTOCREATE_CORES=false
# if you want to replicate only a specific set of cores set this to the list of cores (space separated)
# works only in conjuction with SOLR_SLAVE_AUTOCREATE_CORES=true
ENV SOLR_SLAVE_AUTOCREATE_CORES_LIST=""
# if SOLR_SLAVE_AUTOCREATE_CORES_LIST is empty this is a numerical threshold to wait for until starting to create all
# cores found
ENV SOLR_SLAVE_AUTOCREATE_THRESHOLD=3
ENV SOLR_HEAP=512m
# use this for fine tuning
# ENV SOLR_JAVA_MEM="-Xmx512m -Xms512m"
# kill the process on OOM, if you want to use a different OOM handling, set this to false and add it using SOLR_OPTS
# environmnt variable
ENV EXIT_ON_OOM=true
HEALTHCHECK --start-period=30s --interval=30s --timeout=3s CMD curl -Lf http://localhost:8983/solr || exit 1

# set to true to enable prometheus agent
ENV PROMETHEUS=false
ARG PROMETHEUS_AGENT_VERSION
ADD --chown=solr:solr https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${PROMETHEUS_AGENT_VERSION}/jmx_prometheus_javaagent-${PROMETHEUS_AGENT_VERSION}.jar /opt/solr/prometheus/jmx_prometheus_javaagent.jar

USER root
COPY --chown=solr:solr target/solr-home /opt/solr/server/solr
COPY --chown=solr:solr src/docker/bin /docker-entrypoint-initdb.d
COPY --chown=solr:solr src/docker/prometheus /opt/solr/prometheus
RUN apt-get update && \
    apt-get install -y curl jq && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
    # make solr.in.sh writable for the solr user, bin/config.sh needs to modify it when the container is started
    chown solr:solr /etc/default/solr.in.sh && \
    # Remove default SOLR_HOME files, which we definitely don't need
    rm -rf /opt/solr/server/solr/configsets/sample_techproducts_configs && \
    # Windows compatibility step
    find -H /opt/solr -type d -exec chmod 770 {} \; && chown -R solr. /opt/solr*
USER solr
EXPOSE 8199 8983
