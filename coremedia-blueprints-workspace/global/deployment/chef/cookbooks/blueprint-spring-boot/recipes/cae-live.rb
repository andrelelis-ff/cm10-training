include_recipe 'blueprint-spring-boot::_base'

base_service_name = 'cae-live'
base_service_group = node['blueprint']['group']
base_service_port = node['blueprint']['spring-boot'][base_service_name]['server.port']
base_service_jmx_registry_port = (base_service_port.to_s[0...-2] << '98').to_i
base_service_jmx_server_port = (base_service_port.to_s[0...-2] << '99').to_i
base_service_debug_port = (base_service_port.to_s[0...-2] << '06').to_i

# use default_unless to allow configuration in recipes run prior to this one
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['repository.url'] = 'http://localhost:40280/ior'
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['solr.url'] = 'http://localhost:40080/solr'
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['solr.collection.cae'] = 'live'
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['elastic.solr.url'] = 'http://localhost:40080/solr'
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['mongoDb.clientURI'] = 'mongodb://coremedia:coremedia@localhost:27017'
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['mongoDb.prefix'] = 'blueprint'
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['repository.heapCacheSize'] = 100 * 1024 * 1024
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['repository.blobCacheSize'] = 10 * 1024 * 1024 * 1024
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['repository.blobStreamingSizeThreshold'] = -1
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['repository.blobStreamingThreads'] = -1
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['repository.maxCachedBlobSize'] = -1
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['cae.is.standalone'] = false
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['view.debug.enabled'] = false
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['cae.coderesources.maxAge'] = 180
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['blueprint.sitemap.target.root'] = "#{node['blueprint']['cache_dir']}/sitemap"
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['link.urlPrefixType'] = 'live'
node.default_unless['blueprint']['apps'][base_service_name]['application.properties']['spring.http.encoding.force'] = true

# merge application properties
application_config_hash = Mash.new
# legacy compatibility step. Here we merge the defaults from old node.json files
application_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(application_config_hash, node['blueprint']['webapps'][base_service_name]['application.properties']) if node.deep_fetch('blueprint', 'webapps', base_service_name, 'application.properties')
# and now the new ones
application_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(application_config_hash, node['blueprint']['apps'][base_service_name]['application.properties'])

# merge boot opts
boot_opts_config_hash = Mash.new
boot_opts_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(boot_opts_config_hash, node['blueprint']['spring-boot']['boot_opts'])
boot_opts_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(boot_opts_config_hash, node['blueprint']['spring-boot'][base_service_name]['boot_opts']) if node.deep_fetch('blueprint', 'spring-boot', base_service_name, 'boot_opts')

# merge java opts
java_opts_hash = Mash.new
java_opts_hash = Chef::Mixin::DeepMerge.hash_only_merge!(java_opts_hash, node['blueprint']['spring-boot']['java_opts']) if node.deep_fetch('blueprint', 'spring-boot', 'java_opts')
java_opts_hash = Chef::Mixin::DeepMerge.hash_only_merge!(java_opts_hash, node['blueprint']['spring-boot'][base_service_name]['java_opts']) if base_service_name && node.deep_fetch('blueprint', 'spring-boot', base_service_name, 'java_opts')

(node['blueprint']['spring-boot'][base_service_name]['instances']+1..10).to_a.each do |i|
  uninstall_service_name = "#{base_service_name}-#{i}"
  uninstall_service_dir = "#{node['blueprint']['base_dir']}/#{uninstall_service_name}"
  spring_boot_application uninstall_service_name do
    path uninstall_service_dir
    group_id "not_required_for_uninstall"
    artifact_id "not_required_for_uninstall"
    version "not_required_for_uninstall"
    action :uninstall
    ignore_failure true
    only_if { ::File.exist?(uninstall_service_dir) }
  end
end

(1..node['blueprint']['spring-boot'][base_service_name]['instances']).to_a.each do |i|
  service_name = "#{base_service_name}-#{i}"
  service_user = service_name
  cache_dir = "#{node['blueprint']['cache_dir']}/#{service_name}"
  service_port = base_service_port + i * 100 - 100
  service_management_port = base_service_port + i * 100 - 99
  service_dir = "#{node['blueprint']['base_dir']}/#{service_name}"
  service_jmx_registry_port = base_service_jmx_registry_port + i * 100 - 100
  service_jmx_server_port = base_service_jmx_server_port + i * 100 - 100
  service_debug_port = base_service_debug_port + i * 100 - 100
  # clone the base_service config hashes
  service_application_config = application_config_hash.clone
  service_boot_opts = boot_opts_config_hash.clone
  service_java_opts = java_opts_hash.clone

  if spring_boot_default(service_name, 'debug', 'cae-live')
    service_java_opts['debug'] = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:#{service_debug_port}"
  end

  node.override['blueprint']['apps'][service_name]['application.properties']['repository.blobCachePath'] = cache_dir
  node.override['blueprint']['apps'][service_name]['application.properties']['com.coremedia.transform.blobCache.basePath'] = "#{cache_dir}/persistent-transformed-blobcache"
  node.override['blueprint']['apps'][service_name]['application.properties']['server.port'] = service_port
  node.override['blueprint']['apps'][service_name]['application.properties']['management.server.port'] = service_management_port

  if node.deep_fetch('blueprint', 'spring-boot', service_name, 'sitemap', 'enabled')
    start_time = node.deep_fetch('blueprint', 'spring-boot', service_name, 'sitemap', 'start_time')
    node.default_unless['blueprint']['apps'][service_name]['application.properties']['blueprint.sitemap.starttime'] = start_time.nil? ? '+200' : start_time
    node.default_unless['blueprint']['apps'][service_name]['application.properties']['blueprint.sitemap.cae.port'] = service_port.to_s
  end

  blueprint_service_user service_user do
    home service_dir
    group base_service_group
    notifies :create, "ruby_block[restart_#{service_name}]", :immediately
  end

  directory node['blueprint']['apps'][service_name]['application.properties']['repository.blobCachePath'] do
    owner service_user
    group base_service_group
    recursive true
    notifies :create, "ruby_block[restart_#{service_name}]", :immediately
  end

  # create the instance specific config hashes. No node attribute should be set after these merges
  service_boot_opts = Chef::Mixin::DeepMerge.hash_only_merge!(service_boot_opts, node['blueprint']['spring-boot'][service_name]['boot_opts']) if node.deep_fetch('blueprint', 'spring-boot', service_name, 'boot_opts')
  service_application_config = Chef::Mixin::DeepMerge.hash_only_merge!(service_application_config, node['blueprint']['apps'][service_name]['application.properties']) if node.deep_fetch('blueprint', 'apps', service_name, 'application.properties')
  service_java_opts = Chef::Mixin::DeepMerge.hash_only_merge!(service_java_opts, node['blueprint']['spring-boot'][service_name]['java_opts']) if node.deep_fetch('blueprint', 'spring-boot', service_name, 'java_opts')

  # allow to define attributes using cae-live as key but allow overwrite using
  # the instance key i.e. cae-live-2
  app_group_id = node.deep_fetch('blueprint', 'apps', service_name, 'group_id')
  app_group_id ||= node.deep_fetch('blueprint', 'apps', base_service_name, 'group_id')
  app_artifact_id = node.deep_fetch('blueprint', 'apps', service_name, 'artifact_id')
  app_artifact_id ||= node.deep_fetch('blueprint', 'apps', base_service_name, 'artifact_id')
  app_version = node.deep_fetch('blueprint', 'apps', service_name, 'version')
  app_version ||= node.deep_fetch('blueprint', 'apps', base_service_name, 'version')

  spring_boot_application service_name do
    path service_dir
    maven_repository_url node['blueprint']['maven_repository_url']
    group_id app_group_id
    artifact_id app_artifact_id
    version app_version
    owner service_user
    group base_service_group
    java_opts "-Xmx#{spring_boot_default(service_name, 'heap', 'cae-live')} #{service_java_opts.values.join(' ')}"
    java_home spring_boot_default(service_name, 'java_home', 'cae-live')
    boot_opts service_boot_opts
    application_properties service_application_config
    post_start_wait_url "http://localhost:#{service_management_port}/actuator/health"
    log_dir "#{node['blueprint']['log_dir']}/#{service_name}"
    jmx_remote spring_boot_default(service_name, 'jmx_remote', 'cae-live')
    jmx_remote_server_name spring_boot_default(service_name, 'jmx_remote_server_name', 'cae-live')
    jmx_remote_registry_port service_jmx_registry_port
    jmx_remote_server_port service_jmx_server_port
    jmx_remote_authenticate spring_boot_default(service_name, 'jmx_remote_authenticate', 'cae-live')
    jmx_remote_control_user spring_boot_default(service_name, 'jmx_remote_control_user', 'cae-live')
    jmx_remote_control_password spring_boot_default(service_name, 'jmx_remote_control_password', 'cae-live')
    jmx_remote_monitor_user spring_boot_default(service_name, 'jmx_remote_monitor_user', 'cae-live')
    jmx_remote_monitor_password spring_boot_default(service_name, 'jmx_remote_monitor_password', 'cae-live')

    notifies :create, "ruby_block[restart_#{service_name}]", :immediately
  end

  service service_name do
    action spring_boot_default(service_name, 'start_service', 'cae-live') ? [:enable, :start] : [:enable]
  end

  ruby_block "restart_#{service_name}" do
    block do
      if spring_boot_default(service_name, 'start_service', 'cae-live')
        r = resources(:service => service_name)
        a = Array.new(r.action)

        a << :restart unless a.include?(:restart)
        a.delete(:start) if a.include?(:restart)

        r.action(a)
      end
    end
    action :nothing
  end
end
