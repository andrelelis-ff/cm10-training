apache2_install 'default_install'

service 'apache2' do
  extend Apache2::Cookbook::Helpers
  service_name lazy { apache_platform_service_name }
  supports restart: true, status: true, reload: true
  action :nothing
end

apache2_module 'deflate'
apache2_module 'expires'
apache2_module 'filter'
apache2_module 'headers'
apache2_module 'lbmethod_byrequests'
apache2_module 'mime'
apache2_module 'proxy'
apache2_module 'proxy_ajp'
apache2_module 'proxy_balancer'
apache2_module 'proxy_http'
apache2_module 'rewrite'
apache2_module 'setenvif'
apache2_module 'slotmem_shm'
apache2_module 'status'
apache2_module 'ssl' do
  mod_conf({
   cipher_suite: 'ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:DHE-DSS-AES128-GCM-SHA256:kEDH+AESGCM:ECDHE-RSA-AES128-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES256-SHA384:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA:ECDHE-ECDSA-AES256-SHA:DHE-RSA-AES128-SHA256:DHE-RSA-AES128-SHA:DHE-DSS-AES128-SHA256:DHE-RSA-AES256-SHA256:DHE-DSS-AES256-SHA:DHE-RSA-AES256-SHA:AES128-GCM-SHA256:AES256-GCM-SHA384:AES128-SHA256:AES256-SHA256:AES128-SHA:AES256-SHA:AES:CAMELLIA:DES-CBC3-SHA:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!PSK:!aECDH:!EDH-DSS-DES-CBC3-SHA:!EDH-RSA-DES-CBC3-SHA:!KRB5-DES-CBC3-SHA',
   honor_cipher_order: 'On',
   protocol: 'all -SSLv2 -SSLv3',
   directives: {
    SSLCertificateFile: '/etc/pki/tls/certs/localhost.crt',
    SSLCertificateKeyFile:'/etc/pki/tls/private/localhost.key'
   }})
end

apache2_conf 'global-settings' do
  template_cookbook 'blueprint-proxy'
  action :enable
end

node['blueprint']['proxy']['default_mod_config'].each_pair do |mod, enabled|
  apache2_conf "#{mod}" do
    template_cookbook 'blueprint-proxy'
    action enabled ? :enable : :disable
  end
end
