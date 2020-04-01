include_recipe 'blueprint-spring-boot::_base'

service_name = 'replication-live-server'
service_user = service_name
service_group = node['blueprint']['group']
service_dir = "#{node['blueprint']['base_dir']}/#{service_name}"
corem_home = "#{service_dir}/corem_home"

node.default_unless['blueprint']['apps'][service_name]['application.properties']['spring.application.name'] = 'replication-live-server'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['cap.server.http-port'] = '42080'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['server.port'] = '42080'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['management.server.port'] = '42081'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['com.coremedia.corba.server.host']  = node['blueprint']['hostname']
node.default_unless['blueprint']['apps'][service_name]['application.properties']['com.coremedia.corba.server.port']  = "42083"

application_config_hash = Mash.new
# legacy compatibility step. Here we merge the defaults from old node.json files
application_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(application_config_hash, node['blueprint']['webapps'][service_name]['application.properties']) if node.deep_fetch('blueprint', 'webapps', service_name, 'application.properties')
# and now the new ones
application_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(application_config_hash, node['blueprint']['apps'][service_name]['application.properties'])

blueprint_service_user service_user do
  home service_dir
  group service_group
  notifies :create, "ruby_block[restart_#{service_name}]", :immediately
end

coremedia_maven "#{service_dir}/corem_home.zip" do
  group_id node['blueprint']['apps'][service_name]['config_group_id']
  artifact_id node['blueprint']['apps'][service_name]['config_artifact_id']
  version node['blueprint']['apps'][service_name]['config_version']
  packaging 'zip'
  repository_url node['blueprint']['maven_repository_url']
  nexus_url node['blueprint']['nexus_url'] if node['blueprint']['nexus_url']
  nexus_repo node['blueprint']['nexus_repo'] if node['blueprint']['nexus_repo']
  group service_group
  owner service_name
  extract_to corem_home
  extract_force_clean true
  notifies :create, "ruby_block[restart_#{service_name}]", :immediately
end

boot_opts_config_hash = Mash.new
boot_opts_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(boot_opts_config_hash, node['blueprint']['spring-boot']['boot_opts'])
boot_opts_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(boot_opts_config_hash, node['blueprint']['spring-boot'][service_name]['boot_opts']) if node.deep_fetch('blueprint', 'spring-boot', service_name, 'boot_opts')

# merge java opts
java_opts_hash = Mash.new
java_opts_hash = Chef::Mixin::DeepMerge.hash_only_merge!(java_opts_hash, node['blueprint']['spring-boot']['java_opts']) if node.deep_fetch('blueprint', 'spring-boot', 'java_opts')
java_opts_hash = Chef::Mixin::DeepMerge.hash_only_merge!(java_opts_hash, node['blueprint']['spring-boot'][service_name]['java_opts']) if service_name && node.deep_fetch('blueprint', 'spring-boot', service_name, 'java_opts')

if spring_boot_default(service_name, 'debug')
  java_opts_hash['debug'] = '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:42006'
end

spring_boot_application service_name do
  path service_dir
  maven_repository_url node['blueprint']['maven_repository_url']
  group_id node['blueprint']['apps'][service_name]['group_id']
  artifact_id node['blueprint']['apps'][service_name]['artifact_id']
  version node['blueprint']['apps'][service_name]['version']
  owner service_name
  group service_group
  java_opts "-Xmx#{node['blueprint']['spring-boot'][service_name]['heap']} #{java_opts_hash.values.join(' ')} -Dcorem.home=#{corem_home}"
  java_home spring_boot_default(service_name, 'java_home')
  boot_opts boot_opts_config_hash
  application_properties application_config_hash
  post_start_wait_url "http://localhost:42080/ior"
  log_dir "#{node['blueprint']['log_dir']}/#{service_name}"
  jmx_remote spring_boot_default(service_name, 'jmx_remote')
  jmx_remote_server_name spring_boot_default(service_name, 'jmx_remote_server_name')
  jmx_remote_registry_port 42099
  jmx_remote_server_port 42098
  jmx_remote_authenticate spring_boot_default(service_name, 'jmx_remote_authenticate')
  jmx_remote_control_user spring_boot_default(service_name, 'jmx_remote_control_user')
  jmx_remote_control_password spring_boot_default(service_name, 'jmx_remote_control_password')
  jmx_remote_monitor_user spring_boot_default(service_name, 'jmx_remote_monitor_user')
  jmx_remote_monitor_password spring_boot_default(service_name, 'jmx_remote_monitor_password')
  notifies :create, "ruby_block[restart_#{service_name}]", :immediately
end

# in case cae-live and replicator are installed on the same node we set the default
node.force_default['blueprint']['apps']['cae-live']['application.properties']['repository.url'] = "http://localhost:42080/ior"

service service_name do
  action spring_boot_default(service_name, 'start_service') ? [:enable, :start] : [:enable]
end

ruby_block "restart_#{service_name}" do
  block do
    if spring_boot_default(service_name, 'start_service')
      r = resources(:service => service_name)
      a = Array.new(r.action)

      a << :restart unless a.include?(:restart)
      a.delete(:start) if a.include?(:restart)

      r.action(a)
    end
  end
  action :nothing
end
