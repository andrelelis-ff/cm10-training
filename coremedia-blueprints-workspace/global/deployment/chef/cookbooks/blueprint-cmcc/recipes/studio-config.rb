=begin
#<
This recipe configures the CoreMedia Blueprint Studio.
#>
=end

whitelist_hosts = ["*.#{node['blueprint']['hostname']}"]

if node['blueprint']['sfcc']['enabled']
  # inject sfcc configuration
  if node['blueprint']['sfcc']['application.properties']
    node['blueprint']['sfcc']['application.properties'].each_pair do |k, v|
      node.default['blueprint']['apps']['studio-server']['application.properties'][k] = v
    end
  end
  whitelist_hosts << "http://#{node['blueprint']['sfcc']['host']}"
  whitelist_hosts << "https://#{node['blueprint']['sfcc']['host']}"
end

if node['blueprint']['ibm-wcs']['enabled']
  whitelist_hosts << "#{node['blueprint']['ibm-wcs']['host']}"
  whitelist_hosts << "#{node['blueprint']['ibm-wcs']['host']}:8000"
  node.default['blueprint']['apps']['studio-server']['application.properties']['livecontext.apache.wcs.host'] = "shop-preview-production-ibm.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio-server']['application.properties']['livecontext.apache.preview.production.wcs.host'] = "shop-preview-production-ibm.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio-server']['application.properties']['livecontext.apache.preview.wcs.host'] = "shop-preview-ibm.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio-server']['application.properties']['livecontext.apache.live.production.wcs.host'] = "shop-ibm.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio-server']['application.properties']['livecontext.ibm.contract.preview.credentials.username'] = 'preview'
  node.default['blueprint']['apps']['studio-server']['application.properties']['livecontext.ibm.contract.preview.credentials.password'] = 'passw0rd'
  # inject wcs configuration
  if node['blueprint']['ibm-wcs']['application.properties']
    node['blueprint']['ibm-wcs']['application.properties'].each_pair do |k, v|
      node.default['blueprint']['apps']['studio-server']['application.properties'][k] = v
    end
  end
end

if node['blueprint']['sap-hybris']['enabled']
  node.default['blueprint']['apps']['studio-server']['application.properties']['commerce.hub.data.customEntityParams.catalogversion'] = 'Staged'
  # inject sap-hybris configuration
  if node['blueprint']['sap-hybris']['application.properties']
    node['blueprint']['sap-hybris']['application.properties'].each_pair do |k, v|
      node.default['blueprint']['apps']['studio-server']['application.properties'][k] = v
    end
  end
  whitelist_hosts << "http://#{node['blueprint']['sap-hybris']['host']}"
  whitelist_hosts << "https://#{node['blueprint']['sap-hybris']['host']}"
end

node.default['blueprint']['apps']['studio-server']['application.properties']['studio.previewUrlWhitelist'] = whitelist_hosts.join(",")
