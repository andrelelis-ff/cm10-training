FROM nginx:1.15.12-alpine
ENV COMMERCE_MOCK_ENABLED=false \
    COMMERCE_SFCC_ENABLED=false \
    COMMERCE_SFCC_HOST="" \
    COMMERCE_HYBRIS_ENABLED=false \
    COMMERCE_HYBRIS_HOST="" \
    COMMERCE_WCS_ENABLED=false \
    COMMERCE_WCS_HOST="" \
    COMMERCE_WCS_VERSION="8.0" \
    STUDIO_PACKAGES_PROXY_ENABLED=false
COPY entrypoint.sh /entrypoint.sh
RUN apk add --update \
      curl \
      bash && \
    rm -rf /var/cache/apk/* && \
    curl -sL "https://github.com/kelseyhightower/confd/releases/download/v0.16.0/confd-0.16.0-linux-amd64" -o /usr/bin/confd && \
    chmod +x /usr/bin/confd && /bin/chmod 775 /entrypoint.sh
COPY confd /etc/confd
COPY index.html /usr/share/nginx/html/
COPY assets /usr/share/nginx/html/assets
COPY conf.d /etc/nginx/conf.d

ENTRYPOINT ["/entrypoint.sh"]
