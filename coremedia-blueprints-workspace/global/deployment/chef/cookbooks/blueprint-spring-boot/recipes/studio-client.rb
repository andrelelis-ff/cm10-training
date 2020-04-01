include_recipe 'blueprint-base'

# if you want to change this name make sure to change it in the proxy config in blueprint-proxy too
service_name = 'studio-client'
service_group = node['blueprint']['group']
service_owner = node['blueprint']['user']
service_dir = "#{node['blueprint']['base_dir']}/#{service_name}"

directory service_dir do
  owner service_owner
  group service_group
  recursive true
end

working_dir = directory "#{service_dir}/exploded" do
  owner service_owner
  group service_group
  action :create
end

notify_r = ruby_block "unpack_#{service_name}" do
  block do
    r = resources(:execute => 'extract-studio-client-resources')
    a = Array.new(r.action)
    a << :run unless a.include?(:run)
    a.delete(:nothing) if a.include?(:run)
    r.action(a)
  end
  action :nothing
end

coremedia_maven "#{service_dir}/studio-base-app.jar" do
  group_id node['blueprint']['apps'][service_name]['base_app_group_id']
  artifact_id node['blueprint']['apps'][service_name]['base_app_artifact_id']
  version node['blueprint']['apps'][service_name]['base_app_version']
  packaging 'jar'
  repository_url node['blueprint']['maven_repository_url']
  nexus_url node['blueprint']['nexus_url'] if node['blueprint']['nexus_url']
  nexus_repo node['blueprint']['nexus_repo'] if node['blueprint']['nexus_repo']
  group service_group
  owner service_owner
  notifies :create, notify_r, :immediately
end

coremedia_maven "#{service_dir}/studio-app.jar" do
  group_id node['blueprint']['apps'][service_name]['app_group_id']
  artifact_id node['blueprint']['apps'][service_name]['app_artifact_id']
  version node['blueprint']['apps'][service_name]['app_version']
  packaging 'jar'
  repository_url node['blueprint']['maven_repository_url']
  nexus_url node['blueprint']['nexus_url'] if node['blueprint']['nexus_url']
  nexus_repo node['blueprint']['nexus_repo'] if node['blueprint']['nexus_repo']
  group service_group
  owner service_owner
  notifies :create, notify_r, :immediately
end

execute "extract-studio-client-resources" do
  command "rm -rf #{service_dir}/www && unzip -uoqq \\*-app.jar -d #{working_dir.path} && mv #{working_dir.path}/META-INF/resources #{service_dir}/www"
  cwd service_dir
  action :nothing
  user service_owner
  group service_group
end
