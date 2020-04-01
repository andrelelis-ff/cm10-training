ARG TOOLS_BASE_IMAGE_TAG
ARG TOOLS_BASE_IMAGE_REPO
FROM ${TOOLS_BASE_IMAGE_REPO}:${TOOLS_BASE_IMAGE_TAG}
ENV JAVA_HEAP=384m TOOLS_USER=admin TOOLS_PASSWORD=admin SKIP_CONTENT=false \
    FORCE_REIMPORT_THEMES=false FORCE_REIMPORT_CONTENT=false BLOB_STORAGE_URL="" CONTENT_ARCHIVE_URL="" THEME_ARCHIVE_URL="" \
    ### Development environment variables activate them by setting
    ### CONFD_PREFIX to dev/management, dev/master or dev/replication
    # content-management-server
    DEV_MANAGEMENT_CAP_CLIENT_SERVER_IOR_URL=http://content-management-server:8080/ior \
    DEV_MANAGEMENT_SQL_STORE_DRIVER=com.mysql.cj.jdbc.Driver \
    DEV_MANAGEMENT_SQL_STORE_URL=jdbc:mysql://mysql:3306/cm_management \
    DEV_MANAGEMENT_SQL_STORE_USER=cm_management \
    DEV_MANAGEMENT_SQL_STORE_PASSWORD=cm_management \
    # cae-feeder-preview
    DEV_MANAGEMENT_JDBC_DRIVER=com.mysql.cj.jdbc.Driver \
    DEV_MANAGEMENT_JDBC_URL=jdbc:mysql://mysql:3306/cm_mcaefeeder \
    DEV_MANAGEMENT_JDBC_USER=cm_mcaefeeder \
    DEV_MANAGEMENT_JDBC_PASSWORD=cm_mcaefeeder \
    # master-live-server
    DEV_MASTER_CAP_CLIENT_SERVER_IOR_URL=http://master-live-server:8080/ior \
    DEV_MASTER_SQL_STORE_DRIVER=com.mysql.cj.jdbc.Driver \
    DEV_MASTER_SQL_STORE_URL=jdbc:mysql://mysql:3306/cm_master \
    DEV_MASTER_SQL_STORE_USER=cm_master \
    DEV_MASTER_SQL_STORE_PASSWORD=cm_master \
    # cae-feeder-live
    DEV_MASTER_JDBC_DRIVER=com.mysql.cj.jdbc.Driver \
    DEV_MASTER_JDBC_URL=jdbc:mysql://mysql:3306/cm_caefeeder \
    DEV_MASTER_JDBC_USER=cm_caefeeder \
    DEV_MASTER_JDBC_PASSWORD=cm_caefeeder \
    # replication-live-server
    DEV_REPLICATION_CAP_CLIENT_SERVER_IOR_URL=http://replication-live-server:8080/ior \
    DEV_REPLICATION_SQL_STORE_DRIVER=com.mysql.cj.jdbc.Driver \
    DEV_REPLICATION_SQL_STORE_URL=jdbc:mysql://mysql:3306/cm_replication \
    DEV_REPLICATION_SQL_STORE_USER=cm_replication \
    DEV_REPLICATION_SQL_STORE_PASSWORD=cm_replication
COPY --chown=coremedia:coremedia target/tools /coremedia/tools
COPY --chown=coremedia:coremedia src/docker /coremedia
COPY --chown=coremedia:coremedia src/confd /etc/confd
RUN mkdir /coremedia/import && chown coremedia . /coremedia/import

# add message of the day
USER root
RUN echo '[ ! -z "$TERM" -a -r /etc/motd ] && cat /etc/motd \n\
function tools-context() { \n\
  CONFD_PREFIX=$1 /coremedia/confd\n\
}\n\' >> /etc/bash.bashrc \
; echo "\
=================================================================\n\
To reconfigure the tools in the development setup against either:\n\
\n\
 * content-management-server and cae-feeder-preview\n\
 * master-live-server and cae-feeder-live\n\
 * replication-live-server\n\
\n\
use one of the following bash aliases:\n\
\n\
 * tools-context dev/management \n\
 * tools-context dev/master \n\
 * tools-context dev/replication \n\
 \n\
 You can find the tools below /coremedia/tools, i.e. run: \n\
\n\
 /coremedia/tools/bin/cm dump u -admin -p admin 1\n\
 =================================================================\n\
\n"\
    > /etc/motd
USER coremedia

# Windows compatibility step
RUN find /coremedia -type d -exec chmod 770 {} \; && \
    chmod +x tools/bin/cm entrypoint.sh import-themes use-remote-content-archive import-user import-content import-default-workflows \
             import-custom-workflow publish-content export-content

CMD ["../bin/bash"]
ENTRYPOINT ["./entrypoint.sh"]
