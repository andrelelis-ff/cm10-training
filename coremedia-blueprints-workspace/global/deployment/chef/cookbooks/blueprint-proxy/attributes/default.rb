default['blueprint']['proxy']['virtual_host']['studio']['host'] = 'localhost'
default['blueprint']['proxy']['virtual_host']['studio']['port'] = '41080'
default['blueprint']['proxy']['virtual_host']['studio']['server_name'] = "studio.#{node['blueprint']['hostname']}"
default['blueprint']['proxy']['virtual_host']['studio']['rewrite_log_level'] = 'trace1'
default['blueprint']['proxy']['virtual_host']['studio']['server_aliases'] = []
#TODO rename server_ to rest_
default['blueprint']['proxy']['virtual_host']['studio']['server_cluster'] = {
        'blue' => { 'host' => 'localhost', 'port' => 41080 }
}
default['blueprint']['proxy']['virtual_host']['studio']['client_cluster'] = {
        'blue' => { 'host' => 'localhost', 'port' => 43080 }
}

default['blueprint']['proxy']['virtual_host']['preview']['host'] = 'localhost'
default['blueprint']['proxy']['virtual_host']['preview']['port'] = '40980'
default['blueprint']['proxy']['virtual_host']['preview']['context'] = 'blueprint'
default['blueprint']['proxy']['virtual_host']['preview']['rewrite_log_level'] = 'trace1'
default['blueprint']['proxy']['virtual_host']['preview']['server_name'] = "preview.#{node['blueprint']['hostname']}"
default['blueprint']['proxy']['virtual_host']['preview']['server_aliases'] = {}
default['blueprint']['proxy']['virtual_host']['preview']['live_servlet_context'] = 'blueprint'
default['blueprint']['proxy']['virtual_host']['preview']['default_site'] = 'corporate'


default['blueprint']['proxy']['virtual_host']['headless-server-live']['host'] = 'localhost'
default['blueprint']['proxy']['virtual_host']['headless-server-live']['port'] = '41280'
default['blueprint']['proxy']['virtual_host']['headless-server-live']['server_name'] = "headless-server-live.#{node['blueprint']['hostname']}"
default['blueprint']['proxy']['virtual_host']['headless-server-live']['rewrite_log_level'] = 'trace1'


default['blueprint']['proxy']['virtual_host']['headless-server-preview']['host'] = 'localhost'
default['blueprint']['proxy']['virtual_host']['headless-server-preview']['port'] = '41180'
default['blueprint']['proxy']['virtual_host']['headless-server-preview']['server_name'] = "headless-server-preview.#{node['blueprint']['hostname']}"
default['blueprint']['proxy']['virtual_host']['headless-server-preview']['rewrite_log_level'] = 'trace1'

# Apache Httpd config
default['blueprint']['proxy']['default_mod_config']['deflate'] = true
default['blueprint']['proxy']['default_mod_config']['expires'] = true
default['blueprint']['proxy']['default_mod_config']['headers'] = true
default['blueprint']['proxy']['default_mod_config']['mime'] = true
default['blueprint']['proxy']['default_mod_config']['rewrite'] = true
default['blueprint']['proxy']['default_mod_config']['cors'] = true

####################################
###     DEVELOPMENT ATTRIBUTES   ###
####################################
#<> The cookbook from which to load the test system overview template
default['blueprint']['proxy']['overview_template']['cookbook'] = 'blueprint-proxy'
#<> The source parameter of the overview template resource
default['blueprint']['proxy']['overview_template']['source'] = 'overview/overview.html.erb'
