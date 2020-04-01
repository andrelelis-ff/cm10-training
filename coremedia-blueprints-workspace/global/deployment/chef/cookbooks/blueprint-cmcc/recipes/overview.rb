=begin
#<
creates a simple overview page with all necessary links at `overview.<hostname>` for dev systems
#>
=end
include_recipe 'blueprint-proxy::_base'

www_dir = directory "#{node['blueprint']['base_dir']}/overview" do
  owner node['blueprint']['user']
  group node['blueprint']['group']
end

template "#{www_dir.path}/index.html" do
  source 'overview/index.html.erb'
  owner node['blueprint']['user']
  group node['blueprint']['group']
end

remote_directory "#{www_dir.path}/assets" do
  source 'overview/assets'
  owner node['blueprint']['user']
  group node['blueprint']['group']
  sensitive true
end

template "#{www_dir.path}/jmx-java-mission-control.xml" do
  source 'overview/jmx-jmc.xml.erb'
  owner node['blueprint']['user']
  group node['blueprint']['group']
end

template 'overview' do
  extend  Apache2::Cookbook::Helpers
  source 'vhosts/overview.erb'
  path "#{apache_dir}/sites-available/_overview.conf"
  variables(
          application_name: 'overview',
          server_name: "overview.#{node['blueprint']['hostname']}",
          docroot: www_dir.path
          )
end

apache2_site '_overview'

# restart it immediately and not in delayed phase so we can use the overview before the content import has been finished.
log 'restart apache immediately' do
  notifies :restart, 'service[apache2]', :immediately
end
