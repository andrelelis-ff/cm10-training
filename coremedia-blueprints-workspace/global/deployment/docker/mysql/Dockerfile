FROM mysql:5.7.28
COPY initdb.d/* /docker-entrypoint-initdb.d/
COPY conf.d/* /etc/mysql/conf.d/
ENV MYSQL_PASSWORD=secret
ENV MYSQL_ROOT_PASSWORD=secret
