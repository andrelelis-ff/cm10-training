# Description

This is the base cookbook for your chef deployment. Within this cookbook you can do basic stuff like:
- setting MOTD
- create users and set them up


Use this cookbook with care and only put things in here if they don't fit anywhere else. Don't let it become a "junk drawer"



# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* ulimit (~> 1.0.0)
* java_se (~> 8.0) (Suggested but not required)

# Attributes

* `node['blueprint']['user']` - The basic blueprint user, i.e. for tool execution. Defaults to `coremedia`.
* `node['blueprint']['group']` - The basic blueprint group. Defaults to `coremedia`.
* `node['blueprint']['base_dir']` - The base dir to install all apps to. Defaults to `/opt/coremedia`.
* `node['blueprint']['log_dir']` - The directory to log files to. Defaults to `/var/log/coremedia`.
* `node['blueprint']['cache_dir']` - The directory for coremedia caches. Defaults to `/var/cache/coremedia`.
* `node['blueprint']['temp_dir']` - The directory for coremedia temporary files. Defaults to `/var/tmp/coremedia`.
* `node['blueprint']['maven_repository_url']` - The repository url from which the webapp artifacts are downloaded. Defaults to `file://localhost/maven-repo/`.
* `node['blueprint']['nexus_url']` - The nexus base url. Set this to use the nexus REST API to retrieve artifacts. Using it allows you to resolve versions like `RELEASE`, `LATEST` and `X-SNAPSHOT` correctly. Defaults to `nil`.
* `node['blueprint']['nexus_repo']` - The nexus repo name to retrieve the artifacts via REST API. Defaults to `public`.
* `node['blueprint']['hostname']` - Convenience property to set the hostname used for public available urls in case of all components installed on the same host. Do not use or set this attribute in recipes, use the concrete attributes instead. Defaults to `node['fqdn']`.
* `node['blueprint']['local_port_range']` - The default local port range. Defaults to `44000 65535`.
* `node['blueprint']['default_version']` - Convenience property to set the version of all coremedia artifacts. For individual versions you can still set a version attribute beside each artifact_id attribute. Do not use or set this attribute in recipes, use the concrete attributes instead. Defaults to `1-SNAPSHOT`.
* `node['blueprint']['apps']['content-management-server']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['content-management-server']['artifact_id']` -  Defaults to `content-server-app`.
* `node['blueprint']['apps']['content-management-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['content-management-server']['config_group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['apps']['content-management-server']['config_artifact_id']` -  Defaults to `content-server-blueprint-config`.
* `node['blueprint']['apps']['content-management-server']['config_version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['content-management-server']['context']` -  Defaults to `coremedia`.
* `node['blueprint']['apps']['master-live-server']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['master-live-server']['artifact_id']` -  Defaults to `content-server-app`.
* `node['blueprint']['apps']['master-live-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['master-live-server']['config_group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['apps']['master-live-server']['config_artifact_id']` -  Defaults to `content-server-blueprint-config`.
* `node['blueprint']['apps']['master-live-server']['config_version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['master-live-server']['context']` -  Defaults to `coremedia`.
* `node['blueprint']['apps']['workflow-server']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['workflow-server']['artifact_id']` -  Defaults to `workflow-server-app`.
* `node['blueprint']['apps']['workflow-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['workflow-server']['config_group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['apps']['workflow-server']['config_artifact_id']` -  Defaults to `workflowserver-blueprint-config`.
* `node['blueprint']['apps']['workflow-server']['config_version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['workflow-server']['context']` -  Defaults to `workflow`.
* `node['blueprint']['apps']['replication-live-server']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['replication-live-server']['artifact_id']` -  Defaults to `content-server-app`.
* `node['blueprint']['apps']['replication-live-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['replication-live-server']['config_group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['apps']['replication-live-server']['config_artifact_id']` -  Defaults to `content-server-blueprint-config`.
* `node['blueprint']['apps']['replication-live-server']['config_version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['replication-live-server']['context']` -  Defaults to `coremedia`.
* `node['blueprint']['apps']['user-changes']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['user-changes']['artifact_id']` -  Defaults to `user-changes-app`.
* `node['blueprint']['apps']['user-changes']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['elastic-worker']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['elastic-worker']['artifact_id']` -  Defaults to `elastic-worker-app`.
* `node['blueprint']['apps']['elastic-worker']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['content-feeder']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['content-feeder']['artifact_id']` -  Defaults to `content-feeder-app`.
* `node['blueprint']['apps']['content-feeder']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['caefeeder-preview']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['caefeeder-preview']['artifact_id']` -  Defaults to `cae-feeder-app`.
* `node['blueprint']['apps']['caefeeder-preview']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['caefeeder-live']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['caefeeder-live']['artifact_id']` -  Defaults to `cae-feeder-app`.
* `node['blueprint']['apps']['caefeeder-live']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['cae-live']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['cae-live']['artifact_id']` -  Defaults to `cae-live-app`.
* `node['blueprint']['apps']['cae-live']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['cae-live']['context']` -  Defaults to `blueprint`.
* `node['blueprint']['apps']['cae-preview']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['cae-preview']['artifact_id']` -  Defaults to `cae-preview-app`.
* `node['blueprint']['apps']['cae-preview']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['cae-preview']['context']` -  Defaults to `blueprint`.
* `node['blueprint']['apps']['studio-server']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['studio-server']['artifact_id']` -  Defaults to `studio-server-app`.
* `node['blueprint']['apps']['studio-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['studio-client']['base_app_group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['apps']['studio-client']['base_app_artifact_id']` -  Defaults to `studio-base-app`.
* `node['blueprint']['apps']['studio-client']['base_app_version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['studio-client']['app_group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['apps']['studio-client']['app_artifact_id']` -  Defaults to `studio-app`.
* `node['blueprint']['apps']['studio-client']['app_version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['headless-server-preview']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['headless-server-preview']['artifact_id']` -  Defaults to `headless-server-app`.
* `node['blueprint']['apps']['headless-server-preview']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['headless-server-live']['group_id']` -  Defaults to `com.coremedia.blueprint.boot`.
* `node['blueprint']['apps']['headless-server-live']['artifact_id']` -  Defaults to `headless-server-app`.
* `node['blueprint']['apps']['headless-server-live']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['apps']['commerce-adapter-mock']['group_id']` -  Defaults to `com.coremedia.commerce.adapter.base`.
* `node['blueprint']['apps']['commerce-adapter-mock']['artifact_id']` -  Defaults to `adapter-mock-app`.
* `node['blueprint']['apps']['commerce-adapter-mock']['version']` -  Defaults to `1.2.5`.
* `node['blueprint']['apps']['commerce-adapter-hybris']['group_id']` -  Defaults to `com.coremedia.commerce.adapter.hybris`.
* `node['blueprint']['apps']['commerce-adapter-hybris']['artifact_id']` -  Defaults to `adapter-hybris-app`.
* `node['blueprint']['apps']['commerce-adapter-hybris']['version']` -  Defaults to `1.1.7`.
* `node['blueprint']['apps']['commerce-adapter-sfcc']['group_id']` -  Defaults to `com.coremedia.commerce.adapter.sfcc`.
* `node['blueprint']['apps']['commerce-adapter-sfcc']['artifact_id']` -  Defaults to `adapter-sfcc-app`.
* `node['blueprint']['apps']['commerce-adapter-sfcc']['version']` -  Defaults to `1.1.7`.
* `node['blueprint']['apps']['commerce-adapter-wcs']['group_id']` -  Defaults to `com.coremedia.commerce.adapter.wcs`.
* `node['blueprint']['apps']['commerce-adapter-wcs']['artifact_id']` -  Defaults to `adapter-wcs-app`.
* `node['blueprint']['apps']['commerce-adapter-wcs']['version']` -  Defaults to `1.2.5`.
* `node['blueprint']['solr']['config_zip_version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['tools']['content-management-server']['group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['tools']['content-management-server']['artifact_id']` -  Defaults to `cms-tools-application`.
* `node['blueprint']['tools']['content-management-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['tools']['workflow-server']['group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['tools']['workflow-server']['artifact_id']` -  Defaults to `wfs-tools-application`.
* `node['blueprint']['tools']['workflow-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['tools']['caefeeder-preview']['group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['tools']['caefeeder-preview']['artifact_id']` -  Defaults to `caefeeder-tools-application`.
* `node['blueprint']['tools']['caefeeder-preview']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['tools']['master-live-server']['group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['tools']['master-live-server']['artifact_id']` -  Defaults to `mls-tools-application`.
* `node['blueprint']['tools']['master-live-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['tools']['caefeeder-live']['group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['tools']['caefeeder-live']['artifact_id']` -  Defaults to `caefeeder-tools-application`.
* `node['blueprint']['tools']['caefeeder-live']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['tools']['replication-live-server']['group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['tools']['replication-live-server']['artifact_id']` -  Defaults to `rls-tools-application`.
* `node['blueprint']['tools']['replication-live-server']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['tools']['theme-importer']['group_id']` -  Defaults to `com.coremedia.blueprint`.
* `node['blueprint']['tools']['theme-importer']['artifact_id']` -  Defaults to `theme-importer-application`.
* `node['blueprint']['tools']['theme-importer']['version']` -  Defaults to `node['blueprint']['default_version']`.
* `node['blueprint']['jaas']['ldap']['enabled']` -  Defaults to `false`.
* `node['blueprint']['jaas']['ldap']['host']` -  Defaults to `my.ldap.host`.
* `node['blueprint']['jaas']['ldap']['port']` -  Defaults to `1212`.
* `node['blueprint']['jaas']['ldap']['domain']` -  Defaults to `mydomain`.
* `node['blueprint']['jaas']['cas']['enabled']` -  Defaults to `false`.
* `node['blueprint']['jaas']['cas']['validator_url']` -  Defaults to `http://`.
* `node['blueprint']['jaas']['cas']['cap_service_url']` -  Defaults to `http://`.

# Recipes

* [blueprint-base::default](#blueprint-basedefault) - This recipe creates the blueprint user and sets its home directory and process ulimits.
* blueprint-base::reporting

## blueprint-base::default

This recipe creates the blueprint user and sets its home directory and process ulimits. This user should be used to for login
and tools.

# Author

Author:: Your Name (<your_name@domain.com>)
