=begin
#<
This recipe installs virtual hosts for the CoreMedia headless-server-preview.
#>
=end
include_recipe 'blueprint-proxy::_base'
include_recipe 'blueprint-spring-boot::headless-server-preview'
template 'headless-server-preview' do
  extend  Apache2::Cookbook::Helpers
  source 'vhosts/headless-server-preview.erb'
  path "#{apache_dir}/sites-available/headless-server-preview.conf"
  variables(
          application_name: 'headless-server-preview',
          server_name: node['blueprint']['proxy']['virtual_host']['headless-server-preview']['server_name'],
          server_aliases: node['blueprint']['proxy']['virtual_host']['headless-server-preview']['server_aliases'],
          proxy_host: node['blueprint']['proxy']['virtual_host']['headless-server-preview']['host'],
          proxy_port: node['blueprint']['proxy']['virtual_host']['headless-server-preview']['port'],
          rewrite_log_level: node['blueprint']['proxy']['virtual_host']['headless-server-preview']['rewrite_log_level']
          )
end

apache2_site 'headless-server-preview'
