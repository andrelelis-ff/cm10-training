#<> Global path to the java home, can be overridden using  a more specific hash  i.e. default['blueprint']['spring-boot']['workflow-server']['java_home']
default['blueprint']['spring-boot']['java_home'] = '/usr/lib/jvm/java'
#<> Global jvm network system properties, the defaults disable IPv4 because IPv6 loopback over localhost currently does not work behind an apache
default['blueprint']['spring-boot']['java_opts']['network'] = '-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses'
#<> Global jvm opt to exit on out of memory errors
default['blueprint']['spring-boot']['java_opts']['oom_handling'] = '-XX:+ExitOnOutOfMemoryError'
#<> Use ParallelGC in favor of G1GC. In case of multi service deployments on the same node, you may want to set the maximum threads on a service level, i.e. default['blueprint']['spring-boot']['cae-live']['java_opts']['parallel_gc_threads'] = "-XX:ParallelGCThreads=4"
default['blueprint']['spring-boot']['java_opts']['use_parallel_gc'] = '-XX:+UseParallelGC'
#<> Global Spring Boot opts. This map will be transformed into --<key>=<value> on the command-line, can be overridden using  a more specific hash  i.e. default['blueprint']['spring-boot']['workflow-server']['boot_opts'] = {}
default['blueprint']['spring-boot']['boot_opts'] = {}
#<> Start Services can be overridden using a more specific key, i.e. default['blueprint']['spring-boot']['workflow-server']['start_service'] = false
default['blueprint']['spring-boot']['start_service'] = true
#<> Global remote debugging toggle. Can be overridden for each service, i.e. default['blueprint']['spring-boot']['workflow-server']['debug'] = true
default['blueprint']['spring-boot']['debug'] = false

# global JMX options
# #<> Global toggle to enable remote JMX access. The app will be accessible at service:jmx:rmi://<jmx_remote_server_name>:<jmx_remote_registry_port>/jndi/rmi://<jmx_remote_server_name>:<jmx_remote_server_port>/jmxrmi
default['blueprint']['spring-boot']['jmx_remote'] = true
#<> Global JMX remote server name
default['blueprint']['spring-boot']['jmx_remote_server_name'] = node['fqdn']
#<> Global JMX user for writable access
default['blueprint']['spring-boot']['jmx_remote_control_user'] = 'control'
#<> Global JMX password for writable access
default['blueprint']['spring-boot']['jmx_remote_control_password'] = 'control'
#<> Global JMX user for read access
default['blueprint']['spring-boot']['jmx_remote_monitor_user'] = 'monitor'
#<> Global JMX password for read access
default['blueprint']['spring-boot']['jmx_remote_monitor_password'] = 'monitor'
#<> Global toggle to enable JMX authentication
default['blueprint']['spring-boot']['jmx_remote_authenticate'] = false

default['blueprint']['spring-boot']['content-management-server']['heap'] = '512m'
default['blueprint']['spring-boot']['content-management-server']['boot_opts'] = {}
default['blueprint']['spring-boot']['master-live-server']['heap'] = '384m'
default['blueprint']['spring-boot']['master-live-server']['boot_opts'] = {}
default['blueprint']['spring-boot']['replication-live-server']['heap'] = '256m'
default['blueprint']['spring-boot']['replication-live-server']['boot_opts'] = {}
default['blueprint']['spring-boot']['workflow-server']['heap'] = '384m'
default['blueprint']['spring-boot']['workflow-server']['boot_opts'] = {}
default['blueprint']['spring-boot']['content-feeder']['heap'] = '256m'
default['blueprint']['spring-boot']['content-feeder']['boot_opts'] = {}
default['blueprint']['spring-boot']['user-changes']['heap'] = '256m'
default['blueprint']['spring-boot']['user-changes']['boot_opts'] = {}
default['blueprint']['spring-boot']['elastic-worker']['heap'] = '256m'
default['blueprint']['spring-boot']['elastic-worker']['boot_opts'] = {}
default['blueprint']['spring-boot']['caefeeder-preview']['heap'] = '256m'
default['blueprint']['spring-boot']['caefeeder-preview']['boot_opts'] = {}
default['blueprint']['spring-boot']['caefeeder-live']['heap'] = '256m'
default['blueprint']['spring-boot']['caefeeder-live']['boot_opts'] = {}
default['blueprint']['spring-boot']['cae-preview']['heap'] = '1280m'
default['blueprint']['spring-boot']['cae-preview']['boot_opts'] = {}
#<> The number of instances to deploy. The instance number will be appended to the base name, i.e. cae-live-1, cae-live-2 etc.
default['blueprint']['spring-boot']['cae-live']['instances'] = 1
#<> The http server port, this port will be the base for multiple instances, by adding 100 per instance. The port must
# be a 5-digit integer and must not end on 98 or 99
default['blueprint']['spring-boot']['cae-live']['server.port'] = 42180
default['blueprint']['spring-boot']['cae-live']['heap'] = '1280m'
default['blueprint']['spring-boot']['cae-live']['boot_opts'] = {}
default['blueprint']['spring-boot']['cae-live']['sitemap']['enabled'] = true
default['blueprint']['spring-boot']['studio-server']['heap'] = '1280m'
default['blueprint']['spring-boot']['studio-server']['boot_opts'] = {}
default['blueprint']['spring-boot']['studio-client']['heap'] = '128m'
default['blueprint']['spring-boot']['studio-client']['boot_opts'] = {}
default['blueprint']['spring-boot']['headless-server-preview']['heap'] = '1024m'
default['blueprint']['spring-boot']['headless-server-preview']['boot_opts'] = {}
default['blueprint']['spring-boot']['headless-server-live']['heap'] = '1024m'
default['blueprint']['spring-boot']['headless-server-live']['boot_opts'] = {}
default['blueprint']['spring-boot']['commerce-adapter-mock']['heap'] = '64m'
default['blueprint']['spring-boot']['commerce-adapter-mock']['boot_opts'] = {}
default['blueprint']['spring-boot']['commerce-adapter-hybris']['heap'] = '64m'
default['blueprint']['spring-boot']['commerce-adapter-hybris']['boot_opts'] = {}
default['blueprint']['spring-boot']['commerce-adapter-sfcc']['heap'] = '64m'
default['blueprint']['spring-boot']['commerce-adapter-sfcc']['boot_opts'] = {}
default['blueprint']['spring-boot']['commerce-adapter-wcs']['heap'] = '128m'
default['blueprint']['spring-boot']['commerce-adapter-wcs']['boot_opts'] = {}
