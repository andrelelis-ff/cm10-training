# Description

This is the application cookbook to install CoreMedia Blueprint Tools. It provides a definition to install arbitrary tools created with
the `coremedia-application-maven-plugin` based on the `application-runtime` artifact of CoreMedia.

# Filesystem layout

```
<log_dir> --> <path>/var/logs
<path>
    |-bin
    |-lib
    `-var/logs
```


# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* blueprint-base
* coremedia_maven (~> 3.0.1)

# Attributes

* `node['blueprint']['tools']['java_home']` -  Defaults to `/usr/lib/jvm/java`.
* `node['blueprint']['tools']['content-management-server']['dir']` -  Defaults to `#{node['blueprint']['base_dir']}/content-management-server-tools`.
* `node['blueprint']['tools']['content-management-server']['property_files']['capclient.properties']['cap.client.server.ior.url']` -  Defaults to `http://localhost:40180/ior`.
* `node['blueprint']['tools']['content-management-server']['property_files']['capclient.properties']['cap.client.timezone.default']` -  Defaults to `Europe/Berlin`.
* `node['blueprint']['tools']['theme-importer']['dir']` -  Defaults to `#{node['blueprint']['base_dir']}/theme-importer-tools`.
* `node['blueprint']['tools']['theme-importer']['property_files']['theme-importer.properties']['import.multiResultGeneratorFactory.property.inbox']` -  Defaults to `#{node['blueprint']['base_dir']}/theme-importer-inbox`.
* `node['blueprint']['tools']['theme-importer']['property_files']['theme-importer.properties']['import.transformer.10.property.sourcepath']` -  Defaults to `#{node['blueprint']['base_dir']}/theme-importer-inbox`.
* `node['blueprint']['tools']['theme-importer']['property_files']['theme-importer.properties']['import.transformer.10.property.targetpath']` -  Defaults to `/Themes`.
* `node['blueprint']['tools']['theme-importer']['property_files']['capclient.properties']` -  Defaults to `node['blueprint']['tools']['content-management-server']['property_files']['capclient.properties']`.
* `node['blueprint']['tools']['master-live-server']['dir']` -  Defaults to `#{node['blueprint']['base_dir']}/master-live-server-tools`.
* `node['blueprint']['tools']['master-live-server']['property_files']['capclient.properties']['cap.client.server.ior.url']` -  Defaults to `http://localhost:40280/ior`.
* `node['blueprint']['tools']['master-live-server']['property_files']['capclient.properties']['cap.client.timezone.default']` -  Defaults to `Europe/Berlin`.
* `node['blueprint']['tools']['replication-live-server']['dir']` -  Defaults to `#{node['blueprint']['base_dir']}/replication-live-server-tools`.
* `node['blueprint']['tools']['replication-live-server']['property_files']['capclient.properties']['cap.client.server.ior.url']` -  Defaults to `http://localhost:42080/ior`.
* `node['blueprint']['tools']['replication-live-server']['property_files']['capclient.properties']['cap.client.timezone.default']` -  Defaults to `Europe/Berlin`.
* `node['blueprint']['tools']['workflow-server']['dir']` -  Defaults to `#{node['blueprint']['base_dir']}/workflow-server-tools`.
* `node['blueprint']['tools']['workflow-server']['property_files']['capclient.properties']` -  Defaults to `node['blueprint']['tools']['content-management-server']['property_files']['capclient.properties']`.
* `node['blueprint']['tools']['caefeeder-preview']['dir']` -  Defaults to `#{node['blueprint']['base_dir']}/caefeeder-preview-tools`.
* `node['blueprint']['tools']['caefeeder-preview']['property_files']['resetcaefeeder.properties']['jdbc.driver']` -  Defaults to `com.mysql.cj.jdbc.Driver`.
* `node['blueprint']['tools']['caefeeder-preview']['property_files']['resetcaefeeder.properties']['jdbc.url']` -  Defaults to `jdbc:mysql://localhost:3306/cm_mcaefeeder`.
* `node['blueprint']['tools']['caefeeder-preview']['property_files']['resetcaefeeder.properties']['jdbc.user']` -  Defaults to `cm_mcaefeeder`.
* `node['blueprint']['tools']['caefeeder-preview']['property_files']['resetcaefeeder.properties']['jdbc.password']` -  Defaults to `cm_mcaefeeder`.
* `node['blueprint']['tools']['caefeeder-live']['dir']` -  Defaults to `#{node['blueprint']['base_dir']}/caefeeder-live-tools`.
* `node['blueprint']['tools']['caefeeder-live']['property_files']['resetcaefeeder.properties']['jdbc.driver']` -  Defaults to `com.mysql.cj.jdbc.Driver`.
* `node['blueprint']['tools']['caefeeder-live']['property_files']['resetcaefeeder.properties']['jdbc.url']` -  Defaults to `jdbc:mysql://localhost:3306/cm_caefeeder`.
* `node['blueprint']['tools']['caefeeder-live']['property_files']['resetcaefeeder.properties']['jdbc.user']` -  Defaults to `cm_caefeeder`.
* `node['blueprint']['tools']['caefeeder-live']['property_files']['resetcaefeeder.properties']['jdbc.password']` -  Defaults to `cm_caefeeder`.
* `node['blueprint']['tools']['logback_config']['includes']` -  Defaults to `[ ... ]`.
* `node['blueprint']['tools']['logback_config']['logger']['com.coremedia']` -  Defaults to `info`.
* `node['blueprint']['tools']['logback_config']['logger']['hox.corem']` -  Defaults to `info`.
* `node['blueprint']['tools']['logback_config']['appender']` -  Defaults to `[ ... ]`.
* `node['blueprint']['tools']['jvm_args']['heap']` -  Defaults to `-Xmx256m`.

# Recipes

* blueprint-tools::caefeeder-live-tools
* blueprint-tools::caefeeder-preview-tools
* blueprint-tools::content-management-server-tools
* [blueprint-tools::default](#blueprint-toolsdefault) - This recipe installs all tools.
* blueprint-tools::master-live-server-tools
* blueprint-tools::replication-live-server-tools
* blueprint-tools::theme-importer-tools
* blueprint-tools::workflow-server-tools

## blueprint-tools::default

This recipe installs all tools.

# Definitions

* [blueprint_tool](#blueprint_tool) - This definition installs CoreMedia Blueprint tools.

## blueprint_tool

This definition installs CoreMedia Blueprint tools.

### Parameters

- path: The path to install the tool. Defaults to `/opt/<name of definition>`.
- group_id: The maven groupId of the tools artifact..
- artifact_id: The maven artifactId of the tools artifact..
- version: The maven version of the tools artifact..
- checksum: The SHA-256 checksum of the tools artifact..
- java_home: The JAVA_HOME to use for the tool..
- jvm_args: Extra JVM args to set for tool executions..
- sensitive: Set to true to disable template logging.. Defaults to: true
- property_files: A hash where the name of a property file below `properties/corem` can map to a hash of key value pairs representing the properties of that file. If present the file will be rendered with the hashes content..
- user: The user to install the tool for..
- group: The group to install the tool for..
- log_dir: The directory a symlink for the logs should be created for..
- nexus_url: The nexus base url if you want to retrieve artifacts using the rest API of nexus. This allows versions like `LATEST`, `RELEASE` and `x-SNAPSHOT` to be resolved correctly. (optional).
- nexus_repo: The nexus repo name. Use the part of the url not the numeric id. (optional defaults to `releases`).

### Examples

```ruby
blueprint_tool 'content-management-server-tools' do
  group_id  'com.coremedia.blueprint'
  artifact_id 'content-management-server-tools'
  version '7.5-SNAPSHOT'
  user 'coremedia'
  group 'coremedia'
  path "/opt/coremedia/content-management-server-tools"
  property_files ('capclient.properties' => { 'repository.url' => 'http://localhost/40080/ior' })
end
```
# Author

Author:: Your Name (<your_name@domain.com>)
