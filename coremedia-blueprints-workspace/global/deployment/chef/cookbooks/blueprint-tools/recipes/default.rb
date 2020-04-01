=begin
#<
This recipe installs all tools.
#>
=end
include_recipe 'blueprint-tools::content-management-server-tools'
include_recipe 'blueprint-tools::theme-importer-tools'
include_recipe 'blueprint-tools::workflow-server-tools'
include_recipe 'blueprint-tools::caefeeder-preview-tools'
include_recipe 'blueprint-tools::master-live-server-tools'
include_recipe 'blueprint-tools::caefeeder-live-tools'
