=begin
#<
This recipe installs virtual hosts for the CoreMedia Studio.
#>
=end
include_recipe 'blueprint-proxy::_base'
include_recipe 'blueprint-spring-boot::studio-client'
template 'studio' do
  extend  Apache2::Cookbook::Helpers
  source 'vhosts/studio.erb'
  path "#{apache_dir}/sites-available/studio.conf"
  variables(
          application_name: 'studio',
          server_name: node['blueprint']['proxy']['virtual_host']['studio']['server_name'],
          server_aliases: node['blueprint']['proxy']['virtual_host']['studio']['server_aliases'],
          server_cluster: node['blueprint']['proxy']['virtual_host']['studio']['server_cluster'],
          client_dir: "#{node['blueprint']['base_dir']}/studio-client/www",
          rewrite_log_level: node['blueprint']['proxy']['virtual_host']['studio']['rewrite_log_level'],
          ssl_proxy_verify: true
          )
end

apache2_site 'studio'
