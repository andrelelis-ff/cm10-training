=begin
#<
This recipe installs and configures postgresql and creates schemas for CoreMedia Blueprint.
#>
=end
# require 'chef/version_constraint'

node.default['yum']['pgdg']['version'] = node['blueprint']['postgresql']['version']
node.default['yum']['pgdg']['repositoryid'] = "pgdg-#{node['blueprint']['postgresql']['version']}"
node.default['yum']['pgdg']['description'] = "PostgreSQL #{node['blueprint']['postgresql']['version']}"
node.default['yum']['pgdg']['gpgkey'] = "https://download.postgresql.org/pub/repos/yum/RPM-GPG-KEY-PGDG-#{node['blueprint']['postgresql']['version'].gsub('.','')}"
node.default['yum']['pgdg']['gpgcheck'] = true
node.default['yum']['pgdg']['enabled'] = true
node.default['yum']['pgdg']['baseurl'] = "https://download.postgresql.org/pub/repos/yum/#{node['blueprint']['postgresql']['version']}/redhat/rhel-$releasever-$basearch"

include_recipe 'blueprint-base::default'
include_recipe 'blueprint-yum::default'
include_recipe 'yum-pgdg::default'
