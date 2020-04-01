=begin
#<
This recipe installs and configures postgresql and creates schemas for CoreMedia Blueprint.
#>
=end
include_recipe 'blueprint-postgresql::server'
include_recipe 'blueprint-postgresql::schemas'
