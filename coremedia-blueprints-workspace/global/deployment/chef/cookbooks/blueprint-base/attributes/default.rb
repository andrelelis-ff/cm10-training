#<> The basic blueprint user, i.e. for tool execution
default['blueprint']['user'] = 'coremedia'
#<> The basic blueprint group
default['blueprint']['group'] = 'coremedia'
#<> The base dir to install all apps to
default['blueprint']['base_dir'] = '/opt/coremedia'
#<> The directory to log files to
default['blueprint']['log_dir'] = '/var/log/coremedia'
#<> The directory for coremedia caches
default['blueprint']['cache_dir'] = '/var/cache/coremedia'
#<> The directory for coremedia temporary files
default['blueprint']['temp_dir'] = '/var/tmp/coremedia'
#<> The repository url from which the webapp artifacts are downloaded
default['blueprint']['maven_repository_url'] = 'file://localhost/maven-repo/'
#<> The nexus base url. Set this to use the nexus REST API to retrieve artifacts. Using it allows you to resolve versions like `RELEASE`, `LATEST` and `X-SNAPSHOT` correctly.
default['blueprint']['nexus_url'] = nil
#<> The nexus repo name to retrieve the artifacts via REST API.
default['blueprint']['nexus_repo'] = 'public'
#<> Convenience property to set the hostname used for public available urls in case of all components installed on the same host. Do not use or set this attribute in recipes, use the concrete attributes instead.
default['blueprint']['hostname'] = node['fqdn']
#<> The default local port range
default['blueprint']['local_port_range'] = '44000 65535'

#<> Convenience property to set the version of all coremedia artifacts. For individual versions you can still set a version attribute beside each artifact_id attribute. Do not use or set this attribute in recipes, use the concrete attributes instead.
default['blueprint']['default_version'] = '1-SNAPSHOT'

default['blueprint']['apps']['content-management-server']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['content-management-server']['artifact_id'] = 'content-server-app'
default['blueprint']['apps']['content-management-server']['version'] = node['blueprint']['default_version']
default['blueprint']['apps']['content-management-server']['config_group_id'] = 'com.coremedia.blueprint'
default['blueprint']['apps']['content-management-server']['config_artifact_id'] = 'content-server-blueprint-config'
default['blueprint']['apps']['content-management-server']['config_version'] = node['blueprint']['default_version']
default['blueprint']['apps']['content-management-server']['context'] = 'coremedia'

default['blueprint']['apps']['master-live-server']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['master-live-server']['artifact_id'] = 'content-server-app'
default['blueprint']['apps']['master-live-server']['version'] = node['blueprint']['default_version']
default['blueprint']['apps']['master-live-server']['config_group_id'] = 'com.coremedia.blueprint'
default['blueprint']['apps']['master-live-server']['config_artifact_id'] = 'content-server-blueprint-config'
default['blueprint']['apps']['master-live-server']['config_version'] = node['blueprint']['default_version']
default['blueprint']['apps']['master-live-server']['context'] = 'coremedia'

default['blueprint']['apps']['workflow-server']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['workflow-server']['artifact_id'] = 'workflow-server-app'
default['blueprint']['apps']['workflow-server']['version'] = node['blueprint']['default_version']
default['blueprint']['apps']['workflow-server']['config_group_id'] = 'com.coremedia.blueprint'
default['blueprint']['apps']['workflow-server']['config_artifact_id'] = 'workflowserver-blueprint-config'
default['blueprint']['apps']['workflow-server']['config_version'] = node['blueprint']['default_version']
default['blueprint']['apps']['workflow-server']['context'] = 'workflow'

default['blueprint']['apps']['replication-live-server']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['replication-live-server']['artifact_id'] = 'content-server-app'
default['blueprint']['apps']['replication-live-server']['version'] = node['blueprint']['default_version']
default['blueprint']['apps']['replication-live-server']['config_group_id'] = 'com.coremedia.blueprint'
default['blueprint']['apps']['replication-live-server']['config_artifact_id'] = 'content-server-blueprint-config'
default['blueprint']['apps']['replication-live-server']['config_version'] = node['blueprint']['default_version']
default['blueprint']['apps']['replication-live-server']['context'] = 'coremedia'

default['blueprint']['apps']['user-changes']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['user-changes']['artifact_id'] = 'user-changes-app'
default['blueprint']['apps']['user-changes']['version'] = node['blueprint']['default_version']

default['blueprint']['apps']['elastic-worker']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['elastic-worker']['artifact_id'] = 'elastic-worker-app'
default['blueprint']['apps']['elastic-worker']['version'] = node['blueprint']['default_version']

default['blueprint']['apps']['content-feeder']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['content-feeder']['artifact_id'] = 'content-feeder-app'
default['blueprint']['apps']['content-feeder']['version'] = node['blueprint']['default_version']

default['blueprint']['apps']['caefeeder-preview']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['caefeeder-preview']['artifact_id'] = 'cae-feeder-app'
default['blueprint']['apps']['caefeeder-preview']['version'] = node['blueprint']['default_version']

default['blueprint']['apps']['caefeeder-live']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['caefeeder-live']['artifact_id'] = 'cae-feeder-app'
default['blueprint']['apps']['caefeeder-live']['version'] = node['blueprint']['default_version']

default['blueprint']['apps']['cae-live']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['cae-live']['artifact_id'] = 'cae-live-app'
default['blueprint']['apps']['cae-live']['version'] = node['blueprint']['default_version']
default['blueprint']['apps']['cae-live']['context'] = 'blueprint'

default['blueprint']['apps']['cae-preview']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['cae-preview']['artifact_id'] = 'cae-preview-app'
default['blueprint']['apps']['cae-preview']['version'] = node['blueprint']['default_version']
default['blueprint']['apps']['cae-preview']['context'] = 'blueprint'

default['blueprint']['apps']['studio-server']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['studio-server']['artifact_id'] = 'studio-server-app'
default['blueprint']['apps']['studio-server']['version'] = node['blueprint']['default_version']

default['blueprint']['apps']['studio-client']['base_app_group_id'] = 'com.coremedia.blueprint'
default['blueprint']['apps']['studio-client']['base_app_artifact_id'] = 'studio-base-app'
default['blueprint']['apps']['studio-client']['base_app_version'] = node['blueprint']['default_version']

default['blueprint']['apps']['studio-client']['app_group_id'] = 'com.coremedia.blueprint'
default['blueprint']['apps']['studio-client']['app_artifact_id'] = 'studio-app'
default['blueprint']['apps']['studio-client']['app_version'] = node['blueprint']['default_version']

default['blueprint']['apps']['headless-server-preview']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['headless-server-preview']['artifact_id'] = 'headless-server-app'
default['blueprint']['apps']['headless-server-preview']['version'] = node['blueprint']['default_version']

default['blueprint']['apps']['headless-server-live']['group_id'] = 'com.coremedia.blueprint.boot'
default['blueprint']['apps']['headless-server-live']['artifact_id'] = 'headless-server-app'
default['blueprint']['apps']['headless-server-live']['version'] = node['blueprint']['default_version']

default['blueprint']['apps']['commerce-adapter-mock']['group_id'] = 'com.coremedia.commerce.adapter.base'
default['blueprint']['apps']['commerce-adapter-mock']['artifact_id'] = 'adapter-mock-app'
default['blueprint']['apps']['commerce-adapter-mock']['version'] = '1.2.5'

default['blueprint']['apps']['commerce-adapter-hybris']['group_id'] = 'com.coremedia.commerce.adapter.hybris'
default['blueprint']['apps']['commerce-adapter-hybris']['artifact_id'] = 'adapter-hybris-app'
default['blueprint']['apps']['commerce-adapter-hybris']['version'] = '1.1.7'

default['blueprint']['apps']['commerce-adapter-sfcc']['group_id'] = 'com.coremedia.commerce.adapter.sfcc'
default['blueprint']['apps']['commerce-adapter-sfcc']['artifact_id'] = 'adapter-sfcc-app'
default['blueprint']['apps']['commerce-adapter-sfcc']['version'] = '1.1.7'

default['blueprint']['apps']['commerce-adapter-wcs']['group_id'] = 'com.coremedia.commerce.adapter.wcs'
default['blueprint']['apps']['commerce-adapter-wcs']['artifact_id'] = 'adapter-wcs-app'
default['blueprint']['apps']['commerce-adapter-wcs']['version'] = '1.2.5'

default['blueprint']['solr']['config_zip_version'] = node['blueprint']['default_version']

#### Tools
default['blueprint']['tools']['content-management-server']['group_id'] = 'com.coremedia.blueprint'
default['blueprint']['tools']['content-management-server']['artifact_id'] = 'cms-tools-application'
default['blueprint']['tools']['content-management-server']['version'] = node['blueprint']['default_version']

default['blueprint']['tools']['workflow-server']['group_id'] = 'com.coremedia.blueprint'
default['blueprint']['tools']['workflow-server']['artifact_id'] = 'wfs-tools-application'
default['blueprint']['tools']['workflow-server']['version'] = node['blueprint']['default_version']

default['blueprint']['tools']['caefeeder-preview']['group_id'] = 'com.coremedia.blueprint'
default['blueprint']['tools']['caefeeder-preview']['artifact_id'] = 'caefeeder-tools-application'
default['blueprint']['tools']['caefeeder-preview']['version'] = node['blueprint']['default_version']

default['blueprint']['tools']['master-live-server']['group_id'] = 'com.coremedia.blueprint'
default['blueprint']['tools']['master-live-server']['artifact_id'] = 'mls-tools-application'
default['blueprint']['tools']['master-live-server']['version'] = node['blueprint']['default_version']

default['blueprint']['tools']['caefeeder-live']['group_id'] = 'com.coremedia.blueprint'
default['blueprint']['tools']['caefeeder-live']['artifact_id'] = 'caefeeder-tools-application'
default['blueprint']['tools']['caefeeder-live']['version'] = node['blueprint']['default_version']

default['blueprint']['tools']['replication-live-server']['group_id'] = 'com.coremedia.blueprint'
default['blueprint']['tools']['replication-live-server']['artifact_id'] = 'rls-tools-application'
default['blueprint']['tools']['replication-live-server']['version'] = node['blueprint']['default_version']

default['blueprint']['tools']['theme-importer']['group_id'] = 'com.coremedia.blueprint'
default['blueprint']['tools']['theme-importer']['artifact_id'] = 'theme-importer-application'
default['blueprint']['tools']['theme-importer']['version'] = node['blueprint']['default_version']

##################
## JAAS CONFIG  ##
##################
#LDAP
default['blueprint']['jaas']['ldap']['enabled'] = false
default['blueprint']['jaas']['ldap']['host'] = 'my.ldap.host'
default['blueprint']['jaas']['ldap']['port'] = 1212
default['blueprint']['jaas']['ldap']['domain'] = 'mydomain'
#CAS
default['blueprint']['jaas']['cas']['enabled'] = false
default['blueprint']['jaas']['cas']['validator_url'] = 'http://'
default['blueprint']['jaas']['cas']['cap_service_url'] = 'http://'
