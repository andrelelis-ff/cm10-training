=begin
#<
This recipe installs and configures postgresql.
#>
=end

include_recipe 'blueprint-postgresql::_base'

postgresql_server_install 'coremedia-postgresql-server' do
  version node['blueprint']['postgresql']['version']
  setup_repo false
  password node['blueprint']['postgresql']['initial_root_password']
  initdb_locale 'C'

  action [:install, :create]
end

# Using this to generate a service resource to control
find_resource(:service, 'postgresql') do
  extend PostgresqlCookbook::Helpers
  service_name lazy { platform_service_name }
  supports restart: true, status: true, reload: true
  action [:enable, :start]
end

postgresql_server_conf 'coremedia-postgresql-config' do
  version node['blueprint']['postgresql']['version']
  data_directory '/var/lib/pgsql/data'
  additional_config node['blueprint']['postgresql']['config']
  notifies :reload, 'service[postgresql]'
end

postgresql_access 'local_postgres_superuser' do
  comment 'Local postgres superuser access'
  access_type 'local'
  access_db 'all'
  access_user 'postgres'
  access_addr nil
  access_method 'ident'
end

postgresql_access 'local_all' do
  comment 'Local user access'
  access_type 'local'
  access_db 'all'
  access_user 'all'
  access_addr nil
  access_method 'ident'
end

postgresql_access 'localhost_all' do
  comment 'Localhost access'
  access_type 'host'
  access_db 'all'
  access_user 'all'
  access_addr '127.0.0.1/32'
  access_method 'md5'
end

postgresql_access 'loopback_all' do
  comment 'Loopback access'
  access_type 'host'
  access_db 'all'
  access_user 'all'
  access_addr '::1/128'
  access_method 'md5'
end

postgresql_access 'global_access' do
  comment 'global access'
  access_type 'host'
  access_db 'all'
  access_user 'all'
  access_addr '10.0.0.0/8'
  access_method 'md5'
end
