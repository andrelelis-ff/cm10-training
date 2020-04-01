=begin
#<
This recipe installs and configures mysql client and creates schemas.
#>
=end
include_recipe 'blueprint-base::default'
include_recipe 'blueprint-mysql::_base'

mysql_client_installation_package 'default' do
  version node['blueprint']['mysql']['version']
  package_version node['blueprint']['mysql']['package_version'] unless node['blueprint']['mysql']['package_version'].nil?
  action :create
end

node['blueprint']['mysql']['schemas'].each do |schema|
  sql = "CREATE SCHEMA #{schema} CHARACTER SET utf8mb4 COLLATE utf8mb4_bin; \
         GRANT ALL PRIVILEGES ON #{schema}.* TO '#{schema}'@'localhost' IDENTIFIED BY '#{schema}'; \
         GRANT ALL PRIVILEGES ON #{schema}.* TO '#{schema}'@'#{node['ipaddress']}' IDENTIFIED BY '#{schema}'; \
         GRANT ALL PRIVILEGES ON #{schema}.* TO '#{schema}'@'%' IDENTIFIED BY '#{schema}';"
  execute "create #{schema} schema" do
    command "mysql --user=root --password=coremedia --execute=\"#{sql}\""
    not_if "mysql --user=#{schema} --password=#{schema} --execute='' #{schema}"
    sensitive true
  end
end
