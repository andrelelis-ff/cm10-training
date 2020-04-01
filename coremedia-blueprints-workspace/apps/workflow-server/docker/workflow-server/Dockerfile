ARG JAVA_APPLICATION_BASE_IMAGE_TAG
ARG JAVA_APPLICATION_BASE_IMAGE_REPO
FROM ${JAVA_APPLICATION_BASE_IMAGE_REPO}:${JAVA_APPLICATION_BASE_IMAGE_TAG}
ENV JAVA_HEAP=384m
ENV JAVA_PARALLEL_GC=true
EXPOSE 8080 8081 8083 8199
HEALTHCHECK --start-period=60s --interval=30s --timeout=3s CMD curl -Lf http://localhost:8080/ior || exit 1
COPY --chown=coremedia:coremedia src/docker /coremedia
COPY --chown=coremedia:coremedia src/confd /etc/confd
COPY --chown=coremedia:coremedia target/corem-home /coremedia/corem-home
COPY --chown=coremedia:coremedia target/workflow-server-app.jar /coremedia/application.jar
COPY --chown=coremedia:coremedia target/tools /coremedia/tools
# Windows compatibility step
RUN find /coremedia -type d -exec chmod 770 {} \; && \
    chmod +x tools/bin/cm set-corem-home workflowconverter
CMD ["application"]
ENTRYPOINT ["./entrypoint.sh"]
