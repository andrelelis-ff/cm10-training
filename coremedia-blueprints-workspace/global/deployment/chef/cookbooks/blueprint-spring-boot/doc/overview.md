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
