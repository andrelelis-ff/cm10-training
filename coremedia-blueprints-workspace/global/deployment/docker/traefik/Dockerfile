FROM traefik:1.7.14-alpine
COPY docker.localhost.crt /etc/traefik/traefik.crt
COPY docker.localhost.key /etc/traefik/traefik.key
ENV ENVIRONMENT_FQDN=docker.localhost
CMD  ["--loglevel=INFO", \
     "--checknewversion=false", \
     "--docker=true", \
     "--web=true", \
     "--docker.domain=${ENVIRONMENT_FQDN}", \
     "--docker.exposedbydefault=false", \
     "--defaultentrypoints=http,https", \
     "--entryPoints=Name:http Address::80", \
     "--entryPoints=Name:https Address::443 TLS:/etc/traefik/traefik.crt,/etc/traefik/traefik.key TLS.DefaultCertificate.Cert:/etc/traefik/traefik.crt TLS.DefaultCertificate.Key:/etc/traefik/traefik.key", \
     "--respondingtimeouts.idletimeout=360s", \
     "--web.metrics.prometheus", \
     "--web.metrics.prometheus.buckets=0.1,0.3,1.2,5.0"]

