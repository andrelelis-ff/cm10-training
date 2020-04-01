=begin
#<
This recipe installs and configures mysql.
#>
=end

include_recipe 'blueprint-mysql::_base'

mysql_service 'default' do
  port node['blueprint']['mysql']['port']
  version node['blueprint']['mysql']['version']
  package_version node['blueprint']['mysql']['package_version'] unless node['blueprint']['mysql']['package_version'].nil?
  initial_root_password node['blueprint']['mysql']['initial_root_password']
  socket node['blueprint']['mysql']['socket']
  action [:create, :start]
  install_method 'package' if node['platform'] == 'amazon'
  service_manager 'sysvinit' if node['platform'] == 'amazon'
end

directory node['blueprint']['mysql']['log_dir'] do
  owner 'mysql'
  group 'mysql'
end

mysql_config 'mysql-config' do
  source 'my_conf.erb'
  variables(timezone: node['blueprint']['mysql']['timezone'],
            innodb_buffer_pool_size_mb: node['blueprint']['mysql']['innodb_buffer_pool_size_mb'],
            log_dir: node['blueprint']['mysql']['log_dir'],
            slow_query_log: node['blueprint']['mysql']['slow_query_log'],
            general_query_log: node['blueprint']['mysql']['general_log']
           )
  notifies :restart, 'mysql_service[default]', :immediate
  action :create
end
