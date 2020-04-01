# if you use this environment with chef-solo make sure you set the following convenience attributes in you node.json
# * node['blueprint']['hostname'] if the default node['fqdn'] is not enough
# * node['blueprint']['default_version'] if you want to use this convenience attribute

# attributes to set here
node.default['blueprint']['dev']['db']['type'] = 'mysql'
node.default['blueprint']['spring-boot']['jmx_remote_authenticate'] = false

# REPO CONFIGURATION
node.default['blueprint']['maven_repository_url'] = 'http://your.maven.repo'
# set your nexus url and repo here, if you want to use the nexus rest api. If you always do so remove the branch logic below for the versions.
node.default['blueprint']['nexus_url'] = nil
# there should be a repo that groups your snapshot, your releases and a central mirror repo together, we call it public but you may use an arbitrary name.
node.default['blueprint']['nexus_repo'] = 'public'
# VERSIONS
if node['blueprint']['nexus_url']
  # in case we are using the nexus rest api to determine versions, running on the LATEST is a good pattern for
  # a staging environment
  node.default['blueprint']['apps']['content-management-server']['version'] = 'LATEST'
  node.default['blueprint']['apps']['master-live-server']['version'] = 'LATEST'
  node.default['blueprint']['apps']['workflow-server']['version'] = 'LATEST'
  node.default['blueprint']['apps']['replication-live-server']['version'] = 'LATEST'
  node.default['blueprint']['apps']['caefeeder-preview']['version'] = 'LATEST'
  node.default['blueprint']['apps']['caefeeder-live']['version'] = 'LATEST'
  node.default['blueprint']['apps']['content-feeder']['version'] = 'LATEST'
  node.default['blueprint']['apps']['elastic-worker']['version'] = 'LATEST'
  node.default['blueprint']['apps']['user-changes']['version'] = 'LATEST'
  node.default['blueprint']['apps']['studio-server']['version'] = 'LATEST'
  node.default['blueprint']['apps']['studio-client']['base_app_version'] = 'LATEST'
  node.default['blueprint']['apps']['studio-client']['app_version'] = 'LATEST'
  node.default['blueprint']['apps']['cae-preview']['version'] = 'LATEST'
  node.default['blueprint']['apps']['cae-live']['version'] = 'LATEST'
  node.default['blueprint']['apps']['headless-server-preview']['version'] = 'LATEST'
  node.default['blueprint']['apps']['headless-server-live']['version'] = 'LATEST'
  node.default['blueprint']['apps']['commerce-adapter-mock']['version'] = 'LATEST'
  node.default['blueprint']['apps']['commerce-adapter-hybris']['version'] = 'LATEST'
  node.default['blueprint']['apps']['commerce-adapter-sfcc']['version'] = 'LATEST'
  node.default['blueprint']['apps']['commerce-adapter-wcs']['version'] = 'LATEST'
  node.default['blueprint']['solr']['config_zip_version'] = 'LATEST'
  node.default['blueprint']['tools']['caefeeder-preview']['version'] = 'LATEST'
  node.default['blueprint']['tools']['caefeeder-live']['version'] = 'LATEST'
  node.default['blueprint']['tools']['content-management-server']['version'] = 'LATEST'
  node.default['blueprint']['tools']['master-live-server']['version'] = 'LATEST'
  node.default['blueprint']['tools']['workflow-server']['version'] = 'LATEST'
  node.default['blueprint']['tools']['theme-importer']['version'] = 'LATEST'
  node.default['blueprint']['tools']['replication-live-server']['version'] = 'LATEST'
end

# allow automatic schemamigration in this environment. If you allow it also in production or staging, you need to set
# these attributes in those environments too
node.default['blueprint']['apps']['content-management-server']['application.properties']['sql.schema.alterTable'] = true
node.default['blueprint']['apps']['master-live-server']['application.properties']['sql.schema.alterTable'] = true
node.default['blueprint']['apps']['replication-live-server']['application.properties']['sql.schema.alterTable'] = true
