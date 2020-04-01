default['blueprint']['tools']['java_home'] = '/usr/lib/jvm/java'

#Tools
default['blueprint']['tools']['content-management-server']['dir'] = "#{node['blueprint']['base_dir']}/content-management-server-tools"
default['blueprint']['tools']['content-management-server']['property_files']['capclient.properties']['cap.client.server.ior.url'] = 'http://localhost:40180/ior'
default['blueprint']['tools']['content-management-server']['property_files']['capclient.properties']['cap.client.timezone.default'] = 'Europe/Berlin'

default['blueprint']['tools']['theme-importer']['dir'] = "#{node['blueprint']['base_dir']}/theme-importer-tools"
default['blueprint']['tools']['theme-importer']['property_files']['theme-importer.properties']['import.multiResultGeneratorFactory.property.inbox'] = "#{node['blueprint']['base_dir']}/theme-importer-inbox"
default['blueprint']['tools']['theme-importer']['property_files']['theme-importer.properties']['import.transformer.10.property.sourcepath'] = "#{node['blueprint']['base_dir']}/theme-importer-inbox"
default['blueprint']['tools']['theme-importer']['property_files']['theme-importer.properties']['import.transformer.10.property.targetpath'] = '/Themes'
default['blueprint']['tools']['theme-importer']['property_files']['capclient.properties'] = node['blueprint']['tools']['content-management-server']['property_files']['capclient.properties']

default['blueprint']['tools']['master-live-server']['dir'] = "#{node['blueprint']['base_dir']}/master-live-server-tools"
default['blueprint']['tools']['master-live-server']['property_files']['capclient.properties']['cap.client.server.ior.url'] = 'http://localhost:40280/ior'
default['blueprint']['tools']['master-live-server']['property_files']['capclient.properties']['cap.client.timezone.default'] = 'Europe/Berlin'

default['blueprint']['tools']['replication-live-server']['dir'] = "#{node['blueprint']['base_dir']}/replication-live-server-tools"
default['blueprint']['tools']['replication-live-server']['property_files']['capclient.properties']['cap.client.server.ior.url'] = 'http://localhost:42080/ior'
default['blueprint']['tools']['replication-live-server']['property_files']['capclient.properties']['cap.client.timezone.default'] = 'Europe/Berlin'

default['blueprint']['tools']['workflow-server']['dir'] = "#{node['blueprint']['base_dir']}/workflow-server-tools"
default['blueprint']['tools']['workflow-server']['property_files']['capclient.properties'] = node['blueprint']['tools']['content-management-server']['property_files']['capclient.properties']

default['blueprint']['tools']['caefeeder-preview']['dir'] = "#{node['blueprint']['base_dir']}/caefeeder-preview-tools"
default['blueprint']['tools']['caefeeder-preview']['property_files']['resetcaefeeder.properties']['jdbc.driver'] = 'com.mysql.cj.jdbc.Driver'
default['blueprint']['tools']['caefeeder-preview']['property_files']['resetcaefeeder.properties']['jdbc.url'] = 'jdbc:mysql://localhost:3306/cm_mcaefeeder'
default['blueprint']['tools']['caefeeder-preview']['property_files']['resetcaefeeder.properties']['jdbc.user'] = 'cm_mcaefeeder'
default['blueprint']['tools']['caefeeder-preview']['property_files']['resetcaefeeder.properties']['jdbc.password'] = 'cm_mcaefeeder'

default['blueprint']['tools']['caefeeder-live']['dir'] = "#{node['blueprint']['base_dir']}/caefeeder-live-tools"
default['blueprint']['tools']['caefeeder-live']['property_files']['resetcaefeeder.properties']['jdbc.driver'] = 'com.mysql.cj.jdbc.Driver'
default['blueprint']['tools']['caefeeder-live']['property_files']['resetcaefeeder.properties']['jdbc.url'] = 'jdbc:mysql://localhost:3306/cm_caefeeder'
default['blueprint']['tools']['caefeeder-live']['property_files']['resetcaefeeder.properties']['jdbc.user'] = 'cm_caefeeder'
default['blueprint']['tools']['caefeeder-live']['property_files']['resetcaefeeder.properties']['jdbc.password'] = 'cm_caefeeder'

# logging config
default['blueprint']['tools']['logback_config']['includes'] = ['logging-tools.xml', 'logging-file.xml']
default['blueprint']['tools']['logback_config']['logger']['com.coremedia'] = 'info'
default['blueprint']['tools']['logback_config']['logger']['hox.corem'] = 'info'
default['blueprint']['tools']['logback_config']['appender'] = ['file']

# default memory settings
default['blueprint']['tools']['jvm_args']['heap'] = '-Xmx256m'
