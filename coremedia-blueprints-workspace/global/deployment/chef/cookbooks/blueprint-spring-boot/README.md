# Description

This is the application cookbook to deploy CoreMedia Blueprint Spring Boot apps

# Filesystem layout

```
/opt/coremedia/<name>
    |- application.properties
    |- <application>.jar
    |- log
    |- post-start-check.sh
    |- jmx-remote.access
    `- jmx-remote.password

/etc/systemd/system/<name>.conf
```

# Portschema

The port schema is not mandatory but highly recommended. All ports can be configured using standard Spring Boot
properties but the default application configuration relies on the schema below.

```
<3-Digit Prefix><2-Digit Suffix>
```

| Suffix  | Semantic          |
| ------- | ------------------|
| 80      | HTTP              |
| 81      | Spring Management |
| 98      | JMX Server        |
| 99      | JMX Registry      |
| 65      | gRPC              |

The Spring Boot management port is not configured for all applications yet, but it is intended to do so to separate the
management from the application interface.

By default there is no AJP configured in the CoreMedia Blueprint Spring Boot applications. To enable AJP, follow
the standard Spring Boot documentation.

The port prefixes and the application contexts are as follows:

| App Key                   | Prefix  | Context         |
| ------------------------- | ------- | ----------------|
| content-management-server | 401     |                 |
| master-live-server        | 402     |                 |
| workflow-server           | 403     |                 |
| content-feeder            | 404     |                 |
| user-changes              | 405     |                 |
| elastic-worker            | 406     |                 |
| caefeeder-preview         | 407     |                 |
| caefeeder-live            | 408     |                 |
| cae-preview               | 409     | blueprint       |
| studio-server             | 410     | api             |
| headless-server-preview   | 411     |                 |
| headless-server-live      | 412     |                 |
| studio client             | 430     |                 |
| replication-live-server   | 420     |                 |
| cae-live                  | 421     | blueprint       |
| commerce-adapter-mock     | 440     |                 |
| commerce-adapter-sfcc     | 441     |                 |
| commerce-adapter-hybris   | 442     |                 |
| commerce-adapter-wcs      | 443     |                 |

## Application configuration

All apps will load their properties from a chef attribute hash below:

      node['blueprint']['apps']['<service name>']['application.properties']

For example the `repository.url` property for the `cae-preview` service can be set using:

     node.default['blueprint']['apps']['<service name>']['application.properties']['repository.url'] = URL

To provide backwards compatiblity to the previous tomcat deployment, application properties below
`node['blueprint']['webapps']['<SERVICE KEY>']['application.properties]` are read first and then overwritten using
application properties below `node['blueprint']['apps']['<SERVICE KEY>']['application.properties]`.

## Installation configuration

Beside application configuration properties there are configuration options for the installation using Chef i.e. the
JVM memory settings or even application specific settings like the HTTP port in the cae-live recipe to provide the
scaling feature.

All installation configurations are structured in two layers for convenience:
* global configurations are defined directly below `node['blueprint']['spring-boot']`
* application specific installation configurations are defined below `node['blueprint']['spring-boot']['<SERVICE KEY>']`
  and will override all global configurations for that service.

For the `cae-live` recipe there is *base service* layer between the both layers above. This *base service* defines all
configuration values common to all instances of the `cae-live`. To set a specific value only for one instance, set the
attribute below `node['blueprint']['spring-boot']['cae-live-<INSTANCE NUMBER>']`.

## JMX

JMX address = `service:jmx:rmi://<HOST>:<PREFIX>99/jndi/rmi://<HOST>:<PREFIX>98/jmxrmi`
JMX Login (readonly) = (monitor / monitor)
JMX Login (readwrite) = (control / control)

## Adding custom resources to the recipes

Before you can add custom functionality or resources to the existing recipes, you have to be aware of the notification
lifecycle using the chef resources.

Each recipe contains at least the following building blocks:
* a `blueprint_service_user` definition call to create the service user
* some custom `directory` resources for caching or other data directories
* some custom `template` resources for additional configuration files
* a `spring_boot_application` resource to install the application
* a `service` resource to manage the state of the service
* a `ruby_block` resource to prevent an additional restart on the first run

Each of the resources manifesting the configuration or application code state, needs to notify the `ruby_block` resource
on changes immediately. The `ruby_block` resource then checks if there is already a `restart` action registered at the
`service` resource and if found removes a possible `start` action. When the `service` resource is processed it has either
a start or a restart action but not both.

 ```ruby
  template 'my_service_custom_conf' do
    # renders additional application config. If changed trigger restart check
    notifies :create, 'ruby_block[restart_my_service]', :immediately
  end

  spring_boot_application 'my_application' do
    # install and configure application. If changed trigger restart check
    notifies :create, 'ruby_block[restart_my_service]', :immediately
  end

  service 'my_service' do
    # manage service state
    action [:enable, :start]
  end

  ruby_block 'restart_my_service' do
    # restart check
    block do
      # some code removing unnecessary restart action if start already present
    end
    action :nothing
  end
  ```

# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* blueprint-base
* coremedia_maven (~> 3.0.1)
* chef-sugar (~> 5.0.1)

# Attributes

* `node['blueprint']['spring-boot']['java_home']` - Global path to the java home, can be overridden using  a more specific hash  i.e. default['blueprint']['spring-boot']['workflow-server']['java_home']. Defaults to `/usr/lib/jvm/java`.
* `node['blueprint']['spring-boot']['java_opts']['network']` - Global jvm network system properties, the defaults disable IPv4 because IPv6 loopback over localhost currently does not work behind an apache. Defaults to `-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses`.
* `node['blueprint']['spring-boot']['java_opts']['oom_handling']` - Global jvm opt to exit on out of memory errors. Defaults to `-XX:+ExitOnOutOfMemoryError`.
* `node['blueprint']['spring-boot']['boot_opts']` - Global Spring Boot opts. This map will be transformed into --<key>=<value> on the command-line, can be overridden using  a more specific hash  i.e. default['blueprint']['spring-boot']['workflow-server']['boot_opts'] = {}. Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['start_service']` - Start Services can be overridden using a more specific key, i.e. default['blueprint']['spring-boot']['workflow-server']['start_service'] = false. Defaults to `true`.
* `node['blueprint']['spring-boot']['debug']` - Global remote debugging toggle. Can be overridden for each service, i.e. default['blueprint']['spring-boot']['workflow-server']['debug'] = true. Defaults to `false`.
* `node['blueprint']['spring-boot']['jmx_remote']` -  Defaults to `true`.
* `node['blueprint']['spring-boot']['jmx_remote_server_name']` - Global JMX remote server name. Defaults to `node['fqdn']`.
* `node['blueprint']['spring-boot']['jmx_remote_control_user']` - Global JMX user for writable access. Defaults to `control`.
* `node['blueprint']['spring-boot']['jmx_remote_control_password']` - Global JMX password for writable access. Defaults to `control`.
* `node['blueprint']['spring-boot']['jmx_remote_monitor_user']` - Global JMX user for read access. Defaults to `monitor`.
* `node['blueprint']['spring-boot']['jmx_remote_monitor_password']` - Global JMX password for read access. Defaults to `monitor`.
* `node['blueprint']['spring-boot']['jmx_remote_authenticate']` - Global toggle to enable JMX authentication. Defaults to `false`.
* `node['blueprint']['spring-boot']['content-management-server']['heap']` -  Defaults to `512m`.
* `node['blueprint']['spring-boot']['content-management-server']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['master-live-server']['heap']` -  Defaults to `384m`.
* `node['blueprint']['spring-boot']['master-live-server']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['replication-live-server']['heap']` -  Defaults to `256m`.
* `node['blueprint']['spring-boot']['replication-live-server']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['workflow-server']['heap']` -  Defaults to `384m`.
* `node['blueprint']['spring-boot']['workflow-server']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['content-feeder']['heap']` -  Defaults to `256m`.
* `node['blueprint']['spring-boot']['content-feeder']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['user-changes']['heap']` -  Defaults to `256m`.
* `node['blueprint']['spring-boot']['user-changes']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['elastic-worker']['heap']` -  Defaults to `256m`.
* `node['blueprint']['spring-boot']['elastic-worker']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['caefeeder-preview']['heap']` -  Defaults to `256m`.
* `node['blueprint']['spring-boot']['caefeeder-preview']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['caefeeder-live']['heap']` -  Defaults to `256m`.
* `node['blueprint']['spring-boot']['caefeeder-live']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['cae-preview']['heap']` -  Defaults to `1280m`.
* `node['blueprint']['spring-boot']['cae-preview']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['cae-live']['instances']` - The number of instances to deploy. The instance number will be appended to the base name, i.e. cae-live-1, cae-live-2 etc. Defaults to `1`.
* `node['blueprint']['spring-boot']['cae-live']['server.port']` -  Defaults to `42180`.
* `node['blueprint']['spring-boot']['cae-live']['heap']` -  Defaults to `1280m`.
* `node['blueprint']['spring-boot']['cae-live']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['cae-live']['sitemap']['enabled']` -  Defaults to `true`.
* `node['blueprint']['spring-boot']['studio-server']['heap']` -  Defaults to `1280m`.
* `node['blueprint']['spring-boot']['studio-server']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['studio-client']['heap']` -  Defaults to `128m`.
* `node['blueprint']['spring-boot']['studio-client']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['headless-server-preview']['heap']` -  Defaults to `512m`.
* `node['blueprint']['spring-boot']['headless-server-preview']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['headless-server-live']['heap']` -  Defaults to `512m`.
* `node['blueprint']['spring-boot']['headless-server-live']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['commerce-adapter-mock']['heap']` -  Defaults to `64m`.
* `node['blueprint']['spring-boot']['commerce-adapter-mock']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['commerce-adapter-hybris']['heap']` -  Defaults to `64m`.
* `node['blueprint']['spring-boot']['commerce-adapter-hybris']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['commerce-adapter-sfcc']['heap']` -  Defaults to `64m`.
* `node['blueprint']['spring-boot']['commerce-adapter-sfcc']['boot_opts']` -  Defaults to `{ ... }`.
* `node['blueprint']['spring-boot']['commerce-adapter-wcs']['heap']` -  Defaults to `64m`.
* `node['blueprint']['spring-boot']['commerce-adapter-wcs']['boot_opts']` -  Defaults to `{ ... }`.

# Recipes

* blueprint-spring-boot::cae-live
* blueprint-spring-boot::cae-preview
* blueprint-spring-boot::caefeeder-live
* blueprint-spring-boot::caefeeder-preview
* blueprint-spring-boot::commerce-adapter-ibm-wcs
* blueprint-spring-boot::commerce-adapter-mock
* blueprint-spring-boot::commerce-adapter-sap-hybris
* blueprint-spring-boot::commerce-adapter-sfcc
* blueprint-spring-boot::commerce-adapter
* blueprint-spring-boot::content-feeder
* blueprint-spring-boot::content-management-server
* blueprint-spring-boot::elastic-worker
* blueprint-spring-boot::headless-server-live
* blueprint-spring-boot::headless-server-preview
* blueprint-spring-boot::master-live-server
* blueprint-spring-boot::replication-live-server
* blueprint-spring-boot::studio-client
* blueprint-spring-boot::studio-server
* blueprint-spring-boot::user-changes
* blueprint-spring-boot::workflow-server

# Definitions

* [blueprint_service_user](#blueprint_service_user) - This definition creates the service user.

## blueprint_service_user

This definition creates the service user.

### Parameters

- : process_limit.
- filehandle_limit: . Defaults to: 25000
- process_limit: . Defaults to: 5000

### Examples

```ruby
blueprint_service_user 'solr' do
  user  'solr'
  group 'tomcat'
  home '/opt/coremedia/solr'
  filehandle_limit 25_000
  process_limit 5000
end
```
# Resources

* [spring_boot_application](#spring_boot_application) - This resource manages the installation of a Spring-Boot application jar.

## spring_boot_application

This resource manages the installation of a Spring-Boot application jar.

### Actions

- install: Downloads the application jar and renders application.properties file and SystemD init file. Default action.
- uninstall:

### Attribute Parameters

- path: the installation dir
- group_id: the maven groupId of the service artifact
- artifact_id: the maven artifactId of the service artifact
- version: the maven version of the service artifact
- classifier: the maven classifier of the service artifact
- checksum: the checksum of the maven artifact
- packaging: the maven packaging type of the service artifact Defaults to <code>"jar"</code>.
- maven_repository_url: the url of the maven repository to retrieve the service artifact
- nexus_url: the url of the nexus repository. This property is optional and only required if special versions
- nexus_repo: the id of the nexus repo to use. This property is optional and only mandatory if nexus_url is set.
- username: the password to retrieve the artifact if authentication is required
- password:
- group: the group of the service user
- owner: the service user
- service_description: the description of the service used in Systemd/SystemV service definition
- service_timeout: the timeout for the service to start Defaults to <code>300</code>.
- java_opts: JAVA_OPTS for the service Defaults to <code>""</code>.
- java_home: the Jave installation dir Defaults to <code>"/usr/bin/java"</code>.
- boot_opts: a hash of key/values to put on the command-line. Will be transformed to --<key>=<value> Defaults to <code>{}</code>.
- application_properties: a hash of properties rendered to the application.properties file Defaults to <code>{}</code>.
- clean_log_dir_on_start: set to true to delete the logs on restart Defaults to <code>false</code>.
- log_dir: defaults to service_dir/log, if set to something different make sure the directory exists and is writable by the service user Defaults to <code>lazy { ... }</code>.
- post_start_cmds: an array of scripts to execute after the service starts Defaults to <code>[]</code>.
- pre_start_cmds: an array of scripts to execute before the service starts Defaults to <code>[]</code>.
- post_start_wait_url: an url to wait for after the service starts.
- post_start_wait_code: the HTTP return code to wait for Defaults to <code>200</code>.
- post_start_wait_timeout: the timeout to wait for the url check to succeed Defaults to <code>600</code>.
- jmx_remote: Flag to enable or disable remote jmx. Defaults to <code>false</code>.
- jmx_remote_server_name: The host name or ip that resolves to this node. If set to nil, node[:fqdn] will be used. Defaults to <code>lazy { ... }</code>.
- jmx_remote_monitor_user: The user for readonly access. Defaults to <code>"monitor"</code>.
- jmx_remote_monitor_password: The password for readonly access. Defaults to <code>"monitor"</code>.
- jmx_remote_control_user: The user for readwrite access. Defaults to <code>"control"</code>.
- jmx_remote_control_password: The password for readwrite access. Defaults to <code>"control"</code>.
- jmx_remote_registry_port: The port of the JMX registry. Defaults to <code>8099</code>.
- jmx_remote_server_port: The port of the JMX server Defaults to <code>8098</code>.
- jmx_remote_authenticate: A flag to disable jmx authentication. Defaults to <code>false</code>.

### File System layour

This resource expects a content_dir directory to have the following layout

```
|- content
|     |- some-content.xml
|     `- some-blob.jpg
`- users
     `- some-users.xml
```

The content below `content` may be structured arbitrary but must be a valid serverexport dump. User files should be placed
directly below the `users` directory.

### Guards

All actions are guarded so content is only imported once. The guards are achieved by using marker files.

* If content has already been imported, a marker file `CONTENT_IMPORTED` is placed at the root of the content dir.
* If content has been published, a marker file `CONTENT_PUBLISHED` is placed at the root of the content dir.
* If builtin workflows have been uploaded, a marker file `BUILTIN_WORKFLOWS_UPLOADED` is placed at the root of the content dir. If this resource is used multiple times, this may lead to multiple imports so take care using the upload actions.
* If custom workflows have been uploaded, a marker file `<workflow-defintion file>_WORKFLOW_UPLOADED` is being placed aside the workflow defintion file.
* If a users file has been imported, a marker file `<users file>_USERS_IMPORTED` is being placed beside the users file.

### Examples

```ruby
blueprint_dev_tooling_content '/some/path/to/a/content/dir' do
 builtin_workflows ['two-step-publication.xml']
 custom_workflows ['/some/path/to/a/workflow/definition.xml']
 action [:import_content, :import_user]
end
```

# Author

Author:: Your Name (<your_name@domain.com>)
