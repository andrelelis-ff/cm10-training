=begin
#<
This recipe includes all necessary recipes to set up the yum repo configuration. To disable either mysql or postgres repos use the `managed` attributes.
#>
=end

include_recipe 'yum::default'
include_recipe 'blueprint-yum::centos'
include_recipe 'blueprint-yum::remote'
