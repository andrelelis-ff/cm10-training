=begin
#<
This recipe installs and configures mysql and creates schemas for CoreMedia Blueprint.
#>
=end
include_recipe 'blueprint-mysql::server'
include_recipe 'blueprint-mysql::schemas'
