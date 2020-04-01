=begin
#<
This recipe installs virtual hosts for the CoreMedia Blueprint Live CAE.
#>
=end

include_recipe 'blueprint-proxy::_base'

# if on the same host as the live cae
if node.deep_fetch('blueprint', 'spring-boot', 'cae-live', 'instances')
  node.rm_default('blueprint', 'corporate', 'virtual_host', 'delivery', 'cluster', 'default')
  node.rm_default('blueprint', 'sfcc', 'virtual_host', 'delivery', 'cluster', 'default') if node['blueprint']['sfcc']['enabled']
  node.rm_default('blueprint', 'ibm-wcs', 'virtual_host', 'delivery', 'cluster', 'default') if node['blueprint']['ibm-wcs']['enabled']
  node.rm_default('blueprint', 'sap-hybris', 'virtual_host', 'delivery', 'cluster', 'default') if node['blueprint']['sap-hybris']['enabled']

  (1..node['blueprint']['spring-boot']['cae-live']['instances']).to_a.each do |i|
    cae_instance_port = (node['blueprint']['spring-boot']['cae-live']['server.port'] + i * 100 - 100).to_s

    node.default['blueprint']['corporate']['virtual_host']['delivery']['cluster']["cae-live-#{i}"]['host'] = 'localhost'
    node.default['blueprint']['corporate']['virtual_host']['delivery']['cluster']["cae-live-#{i}"]['port'] = cae_instance_port

    if node['blueprint']['sfcc']['enabled']
      node.default['blueprint']['sfcc']['virtual_host']['delivery']['cluster']["cae-live-#{i}"]['host'] = 'localhost'
      node.default['blueprint']['sfcc']['virtual_host']['delivery']['cluster']["cae-live-#{i}"]['port'] = cae_instance_port
    end

    if node['blueprint']['ibm-wcs']['enabled']
      node.default['blueprint']['ibm-wcs']['virtual_host']['delivery']['cluster']["cae-live-#{i}"]['host'] = 'localhost'
      node.default['blueprint']['ibm-wcs']['virtual_host']['delivery']['cluster']["cae-live-#{i}"]['port'] = cae_instance_port
    end

    if node['blueprint']['sap-hybris']['enabled']
      node.default['blueprint']['sap-hybris']['virtual_host']['delivery']['cluster']["cae-live-#{i}"]['host'] = 'localhost'
      node.default['blueprint']['sap-hybris']['virtual_host']['delivery']['cluster']["cae-live-#{i}"]['port'] = cae_instance_port
    end
  end
end

node['blueprint']['corporate']['virtual_host']['delivery']['sites'].keys.each do |site|
  server_name = node['blueprint']['corporate']['virtual_host']['delivery']['sites'][site]['server_name']
  server_aliases = node['blueprint']['corporate']['virtual_host']['delivery']['sites'][site]['server_aliases']
  default_site = node['blueprint']['corporate']['virtual_host']['delivery']['sites'][site]['default_site']
  site_id = node['blueprint']['corporate']['virtual_host']['delivery']['sites'][site]['site_id']

  template "delivery-#{site}" do
    extend Apache2::Cookbook::Helpers
    source 'vhosts/cae.erb'
    path "#{apache_dir}/sites-available/delivery-#{site}.conf"
    cookbook 'blueprint-proxy'
    variables(
            application_name: "delivery-#{site}",
            server_name: server_name,
            server_aliases: server_aliases,
            servlet_context: node['blueprint']['corporate']['virtual_host']['delivery']['context'],
            cluster: node['blueprint']['corporate']['virtual_host']['delivery']['cluster'],
            default_site: default_site,
            rewrite_log_level: node['blueprint']['corporate']['virtual_host']['delivery']['rewrite_log_level'],
            site_id: site_id,
            preview: false
    )
  end
  apache2_site "delivery-#{site}"
end

if node['blueprint']['sfcc']['enabled']
  node['blueprint']['sfcc']['virtual_host']['delivery']['sites'].keys.each do |site|
    server_name = node['blueprint']['sfcc']['virtual_host']['delivery']['sites'][site]['server_name']
    server_aliases = node['blueprint']['sfcc']['virtual_host']['delivery']['sites'][site]['server_aliases']
    default_site = node['blueprint']['sfcc']['virtual_host']['delivery']['sites'][site]['default_site']
    site_id = node['blueprint']['sfcc']['virtual_host']['delivery']['sites'][site]['site_id']

    template "delivery-#{site}" do
      extend Apache2::Cookbook::Helpers
      source 'vhosts/cae.erb'
      path "#{apache_dir}/sites-available/delivery-#{site}.conf"
      cookbook 'blueprint-proxy'
      variables(
              application_name: "delivery-#{site}",
              server_name: server_name,
              server_aliases: server_aliases,
              servlet_context: node['blueprint']['sfcc']['virtual_host']['delivery']['context'],
              cluster: node['blueprint']['sfcc']['virtual_host']['delivery']['cluster'],
              default_site: default_site,
              rewrite_log_level: node['blueprint']['sfcc']['virtual_host']['delivery']['rewrite_log_level'],
              site_id: site_id,
              preview: false
      )
    end
    apache2_site "delivery-#{site}"
  end
end

if node['blueprint']['ibm-wcs']['enabled']
  node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites'].keys.each do |site|
    server_name = node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites'][site]['server_name']
    server_aliases = node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites'][site]['server_aliases']
    default_site = node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites'][site]['default_site']
    site_id = node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites'][site]['site_id']

    template "delivery-#{site}" do
      extend Apache2::Cookbook::Helpers
      source 'vhosts/cae.erb'
      path "#{apache_dir}/sites-available/delivery-#{site}.conf"
      cookbook 'blueprint-proxy'
      variables(
              application_name: "delivery-#{site}",
              server_name: server_name,
              server_aliases: server_aliases,
              servlet_context: node['blueprint']['ibm-wcs']['virtual_host']['delivery']['context'],
              cluster: node['blueprint']['ibm-wcs']['virtual_host']['delivery']['cluster'],
              default_site: default_site,
              rewrite_log_level: node['blueprint']['ibm-wcs']['virtual_host']['delivery']['rewrite_log_level'],
              site_id: site_id,
              preview: false
      )
    end
    apache2_site "delivery-#{site}"
  end
end

if node['blueprint']['sap-hybris']['enabled']

  node['blueprint']['sap-hybris']['virtual_host']['delivery']['sites'].keys.each do |site|
    server_name = node['blueprint']['sap-hybris']['virtual_host']['delivery']['sites'][site]['server_name']
    server_aliases = node['blueprint']['sap-hybris']['virtual_host']['delivery']['sites'][site]['server_aliases']
    default_site = node['blueprint']['sap-hybris']['virtual_host']['delivery']['sites'][site]['default_site']
    site_id = node['blueprint']['sap-hybris']['virtual_host']['delivery']['sites'][site]['site_id']

    template "delivery-#{site}" do
      extend  Apache2::Cookbook::Helpers
      source 'vhosts/cae.erb'
      path "#{apache_dir}/sites-available/delivery-#{site}.conf"
      cookbook 'blueprint-proxy'
      variables(
              application_name: "delivery-#{site}",
              server_name: server_name,
              server_aliases: server_aliases,
              servlet_context: node['blueprint']['sap-hybris']['virtual_host']['delivery']['context'],
              cluster: node['blueprint']['sap-hybris']['virtual_host']['delivery']['cluster'],
              default_site: default_site,
              rewrite_log_level: node['blueprint']['sap-hybris']['virtual_host']['delivery']['rewrite_log_level'],
              site_id: site_id,
              preview: false
      )
    end
    apache2_site "delivery-#{site}"
  end
end
