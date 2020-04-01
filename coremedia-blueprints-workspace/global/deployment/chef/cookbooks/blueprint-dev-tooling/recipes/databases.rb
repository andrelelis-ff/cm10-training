#
# Cookbook Name:: blueprint-dev-tooling
# Recipe:: databases
#
# Copyright (c) 2015 The Authors, All Rights Reserved.

=begin
#<
This recipe sets the db properties of all applications depending on the database type selected. Use this recipe only in
development environments. If the database host property is set to localhost, the recipe will also create the schemas for
the database.

@section Example

```yaml
suites:
- name: default
  run_list:
    - recipe[blueprint-dev-tooling::databases]
  attributes:
    blueprint:
      dev:
        db:
          host: localhost
          type: postgresql
```
#>
=end

# workflowserver uses schema of content-management-server
node.default['blueprint']['dev']['db']['schemas']['workflow-server'] = node['blueprint']['dev']['db']['schemas']['content-management-server']
db_schemas = node['blueprint']['dev']['db']['schemas']

case node['blueprint']['dev']['db']['type']
when 'postgresql'
  %w(content-management-server workflow-server master-live-server replication-live-server).each do |app|
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.driver'] = 'org.postgresql.Driver'
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.url'] = "jdbc:postgresql://#{node['blueprint']['dev']['db']['host']}:5432/coremedia"
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.dbProperties'] = 'corem/postgresql'
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.user'] = db_schemas[app]
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.password'] = db_schemas[app]
  end
  %w(caefeeder-preview caefeeder-live).each do |app|
    node.force_default['blueprint']['apps'][app]['application.properties']['jdbc.driver'] = 'org.postgresql.Driver'
    node.force_default['blueprint']['apps'][app]['application.properties']['jdbc.url'] = "jdbc:postgresql://#{node['blueprint']['dev']['db']['host']}:5432/coremedia"
    node.force_default['blueprint']['apps'][app]['application.properties']['jdbc.user'] = db_schemas[app]
    node.force_default['blueprint']['apps'][app]['application.properties']['jdbc.password'] = db_schemas[app]
    node.force_default['blueprint']['tools'][app]['property_files']['resetcaefeeder.properties']['jdbc.driver'] = 'org.postgresql.Driver'
    node.force_default['blueprint']['tools'][app]['property_files']['resetcaefeeder.properties']['jdbc.url'] = "jdbc:postgresql://#{node['blueprint']['dev']['db']['host']}:5432/coremedia"
    node.force_default['blueprint']['tools'][app]['property_files']['resetcaefeeder.properties']['jdbc.user'] = db_schemas[app]
    node.force_default['blueprint']['tools'][app]['property_files']['resetcaefeeder.properties']['jdbc.password'] = db_schemas[app]
  end
  node.override['blueprint']['postgresql']['schemas'] = db_schemas.values
  include_recipe 'blueprint-postgresql::schemas' if node['blueprint']['dev']['db']['host'] == 'localhost'
when 'mysql'
  %w(content-management-server workflow-server master-live-server replication-live-server).each do |app|
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.driver'] = 'com.mysql.cj.jdbc.Driver'
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.url'] = "jdbc:mysql://#{node['blueprint']['dev']['db']['host']}:3306/#{db_schemas[app]}"
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.dbProperties'] = 'corem/mysql'
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.user'] = db_schemas[app]
    node.force_default['blueprint']['apps'][app]['application.properties']['sql.store.password'] = db_schemas[app]
  end
  %w(caefeeder-preview caefeeder-live).each do |app|
    node.force_default['blueprint']['apps'][app]['application.properties']['jdbc.driver'] = 'com.mysql.cj.jdbc.Driver'
    node.force_default['blueprint']['apps'][app]['application.properties']['jdbc.url'] = "jdbc:mysql://#{node['blueprint']['dev']['db']['host']}:3306/#{db_schemas[app]}"
    node.force_default['blueprint']['apps'][app]['application.properties']['jdbc.user'] = db_schemas[app]
    node.force_default['blueprint']['apps'][app]['application.properties']['jdbc.password'] = db_schemas[app]
    node.force_default['blueprint']['tools'][app]['property_files']['resetcaefeeder.properties']['jdbc.driver'] = 'com.mysql.cj.jdbc.Driver'
    node.force_default['blueprint']['tools'][app]['property_files']['resetcaefeeder.properties']['jdbc.url'] = "jdbc:mysql://#{node['blueprint']['dev']['db']['host']}:3306/#{db_schemas[app]}"
    node.force_default['blueprint']['tools'][app]['property_files']['resetcaefeeder.properties']['jdbc.user'] = db_schemas[app]
    node.force_default['blueprint']['tools'][app]['property_files']['resetcaefeeder.properties']['jdbc.password'] = db_schemas[app]
  end
  node.override['blueprint']['mysql']['schemas'] = db_schemas.values.uniq
  include_recipe 'blueprint-mysql::schemas' if node['blueprint']['dev']['db']['host'] == 'localhost'
end
