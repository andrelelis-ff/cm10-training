=begin
#<
This recipe installs and configures postgresql client and creates schemas.
#>
=end

include_recipe 'blueprint-postgresql::_base'

postgresql_client_install 'coremedia-postgresql-client' do
  setup_repo false
  version node['blueprint']['postgresql']['version']
end

sql = 'CREATE DATABASE coremedia WITH OWNER = postgres TEMPLATE template0 ENCODING = \'UTF8\' CONNECTION LIMIT = -1;'
execute 'create database coremedia' do
  command "PGPASSWORD=#{node['blueprint']['postgresql']['initial_root_password']} psql --host=localhost --user=postgres --no-password --command=\"#{sql}\""
  returns [0, 1]
end

node['blueprint']['postgresql']['schemas'].each do |schema|
  sql = "CREATE ROLE #{schema} LOGIN PASSWORD '#{schema}' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE; \
           CREATE SCHEMA #{schema} AUTHORIZATION #{schema};"
  execute "create #{schema} schema" do
    command "PGPASSWORD=#{node['blueprint']['postgresql']['initial_root_password']} psql --host=localhost --dbname=coremedia --username=postgres --no-password --command=\"#{sql}\""
    not_if "PGPASSWORD=#{schema} psql --host=localhost --dbname=coremedia --username=#{schema} --no-password --command=''"
  end
end

sql = 'REVOKE CREATE ON SCHEMA public FROM PUBLIC;'
execute 'revoke create on schema public' do
  command "PGPASSWORD=#{node['blueprint']['postgresql']['initial_root_password']} psql --host=localhost --dbname=coremedia --user=postgres --no-password --command=\"#{sql}\""
end
