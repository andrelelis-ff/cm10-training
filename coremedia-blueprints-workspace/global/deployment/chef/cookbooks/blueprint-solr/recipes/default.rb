#
# Cookbook Name:: solr
# Recipe:: default
#
# Copyright 2016, CoreMedia AG
#
include_recipe 'blueprint-base'

src_filename = ::File.basename(node['blueprint']['solr']['url'])
src_filepath = "#{Chef::Config['file_cache_path']}/#{src_filename}"

extract_path = "#{node['blueprint']['solr']['dir']}-#{node['blueprint']['solr']['version']}"
solr_path = "#{extract_path}/server"

package 'unzip'
package 'lsof'

remote_file src_filepath do
  source node['blueprint']['solr']['url']
  checksum node['blueprint']['solr']['checksum']
  backup false
  action :create
  notifies :create, 'ruby_block[restart_solr_service]', :immediately
end

group node['blueprint']['solr']['group']

user node['blueprint']['solr']['user'] do
  comment 'Solr User'
  shell '/bin/bash'
  home extract_path
  gid node['blueprint']['solr']['group']
end

user_ulimit node['blueprint']['solr']['user'] do
  filehandle_limit 25_000
  process_limit 5000
end

directory extract_path do
  owner node['blueprint']['solr']['user']
  group node['blueprint']['solr']['group']
  recursive true
  action :create
end

directory node['blueprint']['solr']['log_dir'] do
  owner node['blueprint']['solr']['user']
  group node['blueprint']['solr']['group']
  recursive true
  action :create
end

directory node['blueprint']['solr']['solr_home'] do
  owner node['blueprint']['solr']['user']
  group node['blueprint']['solr']['group']
  recursive true
  action :create
end

directory node['blueprint']['solr']['solr_data_dir'] do
  owner node['blueprint']['solr']['user']
  group node['blueprint']['solr']['group']
  recursive true
  action :create
end

bash 'unpack_solr' do
  cwd ::File.dirname(src_filepath)
  code <<-EOH
    tar xzf #{src_filename} -C #{extract_path} --strip 1
  EOH
  user node['blueprint']['solr']['user']
  not_if { ::File.exist?("#{extract_path}/server") }
end

template '/etc/default/solr.in.sh' do
  source 'solr.in.sh.erb'
  owner 'root'
  group 'root'
  mode '0755'
  variables(
    host_name: node['blueprint']['hostname'],
    solr_dir: solr_path,
    solr_home: node['blueprint']['solr']['solr_home'],
    solr_data_dir: node['blueprint']['solr']['solr_data_dir'],
    pid_dir: node['blueprint']['solr']['pid_dir'],
    log_dir: node['blueprint']['solr']['log_dir'],
    solr_port: node['blueprint']['solr']['port'],
    solr_jmx_port: node['blueprint']['solr']['jmx_port'],
    solr_jmx_enable: node['blueprint']['solr']['jmx_enable'],
    java_mem: node['blueprint']['solr']['java_mem'],
    java_home: node['blueprint']['solr']['java_home']
  )
  only_if { !platform_family?('debian') }
  notifies :create, 'ruby_block[restart_solr_service]', :immediately
end

link node['blueprint']['solr']['dir'] do
  to extract_path
end

# Workaround for SOLR-12737: custom init script that creates SOLR_PID_DIR
cookbook_file '/etc/init.d/solr' do
  source 'init.d/solr'
  owner 'root'
  group 'root'
  mode '0755'
end

coremedia_maven "#{node['blueprint']['cache_dir']}/solr-config.zip" do
  group_id node['blueprint']['solr']['config_zip_group_id']
  artifact_id node['blueprint']['solr']['config_zip_artifact_id']
  version node['blueprint']['solr']['config_zip_version']
  repository_url node['blueprint']['maven_repository_url']
  packaging 'zip'
  extract_to node['blueprint']['solr']['solr_home']
  owner node['blueprint']['solr']['user']
  group node['blueprint']['solr']['group']
  # overwrite all solr-home dir if artifact changed
  extract_force_clean node['blueprint']['solr']['clean_solr_home_on_update']
  notifies :create, 'ruby_block[restart_solr_service]', :immediately
end

ruby_block 'restart_solr_service' do
  block do
    # guard to prevent a restart action to be written into the runlist if start_service is false
    if node['blueprint']['solr']['start_service']
      r = resources(:service => 'solr')
      a = Array.new(r.action)
      a << :restart unless a.include?(:restart)
      a.delete(:start) if a.include?(:restart)
      r.action(a)
    end
  end
  action :nothing
end

service 'solr' do
  supports restart: true, status: true
  action node['blueprint']['solr']['start_service'] ? [:enable, :start] : [:enable]
end
