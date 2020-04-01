node.default['blueprint']['apps']['content-management-server']['application.properties']['sql.store.driver'] = 'com.mysql.cj.jdbc.Driver'
node.default['blueprint']['apps']['content-management-server']['application.properties']['sql.store.url'] = 'jdbc:mysql://localhost:3306/cm_management'
node.default['blueprint']['apps']['content-management-server']['application.properties']['sql.store.dbProperties'] = 'corem/mysql'

node.default['blueprint']['apps']['master-live-server']['application.properties']['sql.store.driver'] = 'com.mysql.cj.jdbc.Driver'
node.default['blueprint']['apps']['master-live-server']['application.properties']['sql.store.url'] = 'jdbc:mysql://localhost:3306/cm_master'
node.default['blueprint']['apps']['master-live-server']['application.properties']['sql.store.dbProperties'] = 'corem/mysql'

node.default['blueprint']['apps']['workflow-server']['application.properties']['sql.store.driver'] = node['blueprint']['webapps']['content-management-server']['application.properties']['sql.store.driver']
node.default['blueprint']['apps']['workflow-server']['application.properties']['sql.store.url'] = node['blueprint']['webapps']['content-management-server']['application.properties']['sql.store.url']
node.default['blueprint']['apps']['workflow-server']['application.properties']['sql.store.dbProperties'] = node['blueprint']['webapps']['content-management-server']['application.properties']['sql.store.dbProperties']

node.default['blueprint']['apps']['replication-live-server']['application.properties']['sql.store.driver'] = 'com.mysql.cj.jdbc.Driver'
node.default['blueprint']['apps']['replication-live-server']['application.properties']['sql.store.url'] = 'jdbc:mysql://localhost:3306/cm_replication'
node.default['blueprint']['apps']['replication-live-server']['application.properties']['sql.store.dbProperties'] = 'corem/mysql'

node.default['blueprint']['apps']['caefeeder-preview']['application.properties']['jdbc.driver'] = 'com.mysql.cj.jdbc.Driver'
node.default['blueprint']['apps']['caefeeder-preview']['application.properties']['jdbc.url'] = 'jdbc:mysql://localhost:3306/cm_mcaefeeder'

node.default['blueprint']['apps']['caefeeder-live']['application.properties']['jdbc.driver'] = 'com.mysql.cj.jdbc.Driver'
node.default['blueprint']['apps']['caefeeder-live']['application.properties']['jdbc.url'] = 'jdbc:mysql://localhost:3306/cm_caefeeder'

# REPO CONFIGURATION
node.default['blueprint']['maven_repository_url'] = 'http://your.maven.repo'
# set your nexus url and repo here, if you want to use the nexus rest api. If you always do so remove the branch logic below for the versions.
node.default['blueprint']['nexus_url'] = nil
# there should be a repo that groups your snapshot, your releases and a central mirror repo together, we call it public but you may use an arbitrary name
node.default['blueprint']['nexus_repo'] = 'public'
# VERSIONS
if node['blueprint']['nexus_url']
  # in case we are using the nexus rest api to determine versions, running on the RELEASE is a good pattern for
  # a staging environment
  node.default['blueprint']['apps']['content-management-server']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['master-live-server']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['workflow-server']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['replication-live-server']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['caefeeder-preview']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['caefeeder-live']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['content-feeder']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['elastic-worker']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['user-changes']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['studio-server']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['studio-client']['base_app_version'] = 'RELEASE'
  node.default['blueprint']['apps']['studio-client']['app_version'] = 'RELEASE'
  node.default['blueprint']['apps']['cae-preview']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['cae-live']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['headless-server-preview']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['headless-server-live']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['commerce-adapter-mock']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['commerce-adapter-hybris']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['commerce-adapter-sfcc']['version'] = 'RELEASE'
  node.default['blueprint']['apps']['commerce-adapter-wcs']['version'] = 'RELEASE'
  node.default['blueprint']['solr']['config_zip_version'] = 'RELEASE'
  node.default['blueprint']['tools']['caefeeder-preview']['version'] = 'RELEASE'
  node.default['blueprint']['tools']['caefeeder-live']['version'] = 'RELEASE'
  node.default['blueprint']['tools']['content-management-server']['version'] = 'RELEASE'
  node.default['blueprint']['tools']['master-live-server']['version'] = 'RELEASE'
  node.default['blueprint']['tools']['workflow-server']['version'] = 'RELEASE'
  node.default['blueprint']['tools']['theme-importer']['version'] = 'RELEASE'
  node.default['blueprint']['tools']['replication-live-server']['version'] = 'RELEASE'
else
  node.default['blueprint']['apps']['content-management-server']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['master-live-server']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['workflow-server']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['replication-live-server']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['caefeeder-preview']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['caefeeder-live']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['content-feeder']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['elastic-worker']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['user-changes']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['studio-server']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['studio-client']['base_app_version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['studio-client']['app_version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['cae-preview']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['cae-live']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['headless-server-preview']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['headless-server-live']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['commerce-adapter-mock']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['commerce-adapter-hybris']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['commerce-adapter-sfcc']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['apps']['commerce-adapter-wcs']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['solr']['config_zip_version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['tools']['caefeeder-preview']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['tools']['caefeeder-live']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['tools']['content-management-server']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['tools']['master-live-server']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['tools']['workflow-server']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['tools']['theme-importer']['version'] = 'ENTER CONCRETE VERSION HERE'
  node.default['blueprint']['tools']['replication-live-server']['version'] = 'ENTER CONCRETE VERSION HERE'
end
