FROM nginx:1.15.12-alpine
ENV CMS_IOR WFS_IOR
EXPOSE 8080
COPY entrypoint.sh /entrypoint.sh
COPY conf.d /etc/nginx/conf.d
RUN apk add --no-cache zip && rm -rf /var/cache/apk/* && /bin/chmod 775 /entrypoint.sh
COPY target/site-manager /tmp/site-manager
ENTRYPOINT ["/entrypoint.sh"]
