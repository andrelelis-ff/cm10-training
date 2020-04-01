cors_hosts = []

if node['blueprint']['sfcc']['enabled']
  cors_hosts << "https://#{node['blueprint']['sfcc']['virtual_host']['shop']['server_name']}"
  node['blueprint']['sfcc']['virtual_host']['shop']['server_aliases'].each do |s|
    cors_hosts << "https://#{s}"
  end
end

if node['blueprint']['ibm-wcs']['enabled']
  node.default['blueprint']['apps']['cae-live']['application.properties']['livecontext.apache.wcs.host'] = "shop-ibm.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-live']['application.properties']['livecontext.apache.preview.production.wcs.host'] = "shop-ibm.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-live']['application.properties']['livecontext.apache.preview.wcs.host'] = "shop-ibm.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-live']['application.properties']['livecontext.apache.live.production.wcs.host'] = "shop-ibm.#{node['blueprint']['hostname']}"

  cors_hosts << "https://#{node['blueprint']['ibm-wcs']['virtual_host']['shop']['server_name']}"
  node['blueprint']['ibm-wcs']['virtual_host']['shop']['server_aliases'].each do |s|
    cors_hosts << "https://#{s}"
  end
  node.default['blueprint']['apps']['cae-live']['application.properties']['livecontext.crossdomain.whitelist'] = cors_hosts.join(',')

  # inject wcs configuration
  if node['blueprint']['ibm-wcs']['application.properties']
    node['blueprint']['ibm-wcs']['application.properties'].each_pair do |k, v|
      node.default['blueprint']['apps']['cae-live']['application.properties'][k] = v
    end
  end
end

if node['blueprint']['sap-hybris']['enabled']
  node.default['blueprint']['apps']['cae-live']['application.properties']['commerce.hub.data.customEntityParams.catalogVersion'] = 'Online'

  cors_hosts << "https://#{node['blueprint']['sap-hybris']['virtual_host']['shop']['server_name']}"
  node['blueprint']['sap-hybris']['virtual_host']['shop']['server_aliases'].each do |s|
    cors_hosts << "https://#{s}"
  end

  # inject sap hybris configuration
  if node['blueprint']['sap-hybris']['application.properties']
    node['blueprint']['sap-hybris']['application.properties'].each_pair do |k, v|
      node.default['blueprint']['apps']['cae-live']['application.properties'][k] = v
    end
  end
end

node.default['blueprint']['apps']['cae-live']['application.properties']['livecontext.crossdomain.whitelist'] = cors_hosts.join(',')

