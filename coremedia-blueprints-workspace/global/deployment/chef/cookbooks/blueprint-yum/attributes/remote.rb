default['blueprint']['yum']['remote']['managed']         = false
default['blueprint']['yum']['remote']['baseurl']         = ''
default['blueprint']['yum']['remote']['enabled']         = true
default['blueprint']['yum']['remote']['exclude']         = nil
default['blueprint']['yum']['remote']['enablegroups']    = nil
default['blueprint']['yum']['remote']['http_caching']    = 'all'
default['blueprint']['yum']['remote']['includepkgs']     = nil
default['blueprint']['yum']['remote']['max_retries']     = '2'
# this is rather a small default, but in order to update blueprint packages
# in a continuous delivery fashion, 10 minutes is a good default
default['blueprint']['yum']['remote']['metadata_expire'] = '600'
default['blueprint']['yum']['remote']['priority']        = '1'
default['blueprint']['yum']['remote']['proxy']           = nil
default['blueprint']['yum']['remote']['proxy_username']  = nil
default['blueprint']['yum']['remote']['proxy_password']  = nil
default['blueprint']['yum']['remote']['timeout']         = '30'
