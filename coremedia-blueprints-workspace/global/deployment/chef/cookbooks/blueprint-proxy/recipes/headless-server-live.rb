=begin
#<
This recipe installs virtual hosts for the CoreMedia headless-server-live.
#>
=end
include_recipe 'blueprint-proxy::_base'
include_recipe 'blueprint-spring-boot::headless-server-live'
template 'headless-server-live' do
  extend  Apache2::Cookbook::Helpers
  source 'vhosts/headless-server-live.erb'
  path "#{apache_dir}/sites-available/headless-server-live.conf"
  variables(
          application_name: 'headless-server-live',
          server_name: node['blueprint']['proxy']['virtual_host']['headless-server-live']['server_name'],
          server_aliases: node['blueprint']['proxy']['virtual_host']['headless-server-live']['server_aliases'],
          proxy_host: node['blueprint']['proxy']['virtual_host']['headless-server-live']['host'],
          proxy_port: node['blueprint']['proxy']['virtual_host']['headless-server-live']['port'],
          rewrite_log_level: node['blueprint']['proxy']['virtual_host']['headless-server-live']['rewrite_log_level']
          )
end

apache2_site 'headless-server-live'
