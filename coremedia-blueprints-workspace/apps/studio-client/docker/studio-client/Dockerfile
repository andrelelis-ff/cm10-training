# builder stage to extract all necessary artifacts
ARG ALPINE_BASE_IMAGE_TAG
ARG ALPINE_BASE_IMAGE_REPO
ARG NGINX_BASE_IMAGE_TAG
ARG NGINX_BASE_IMAGE_REPO
FROM ${ALPINE_BASE_IMAGE_REPO}:${ALPINE_BASE_IMAGE_TAG} as builder
RUN apk add --no-cache unzip && rm -rf /var/cache/apk/*
COPY target/studio-resources.jar /tmp
RUN mkdir /tmp/studio-resources-exploded && \
    unzip -qq -n /tmp/studio-resources.jar -d /tmp/studio-resources-exploded/ && \
    mv /tmp/studio-resources-exploded/META-INF/resources /tmp/merged && \
    chmod -R 644 /tmp/merged && \
    find /tmp/merged/ -type d -exec chmod -R 755 {} \;

# copy static studio client files from builder stage
FROM ${NGINX_BASE_IMAGE_REPO}:${NGINX_BASE_IMAGE_TAG}
LABEL maintainer="CoreMedia AG <support@coremedia.com>"
RUN rm -rf /usr/share/nginx/html
COPY conf.d /etc/nginx/conf.d
COPY --from=builder /tmp/merged /usr/share/nginx/html
