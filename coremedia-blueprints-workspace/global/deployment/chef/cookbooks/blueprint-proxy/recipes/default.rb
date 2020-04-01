=begin
#<
This recipe wraps all recipes of this cookbook for apache running in front of tomcat. To work with websphere add websphere recipe before this recipe.
#>
=end

include_recipe 'blueprint-proxy::studio'
include_recipe 'blueprint-proxy::preview'
include_recipe 'blueprint-proxy::delivery'
include_recipe 'blueprint-proxy::sitemanager'
include_recipe 'blueprint-proxy::headless-server-preview'
include_recipe 'blueprint-proxy::headless-server-live'
