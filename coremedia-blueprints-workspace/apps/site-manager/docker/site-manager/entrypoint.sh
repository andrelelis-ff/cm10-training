#!/bin/sh
cd /tmp/site-manager
cat << EOF > properties/corem/capclient.properties
cap.client.server.ior.url=${CMS_IOR:-http://${ENVIRONMENT_FQDN}:40180/ior}
# Timezone for CAP Clients
cap.client.timezone.default=Europe/Berlin
EOF
cat << EOF > properties/corem/workflowclient.properties
workflow.client.server.ior.url=${WFS_IOR:-http://${ENVIRONMENT_FQDN}:40380/ior}
EOF
zip -r -0 /usr/share/nginx/html/site-manager.zip .
nginx -g "daemon off;"
