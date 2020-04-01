=begin
#<
This recipe sets properties only necessary if the test content is being used
#>
=end

node.default['blueprint']['apps']['cae-live']['application.properties']['blueprint.site.mapping.corporate'] = "//corporate.#{node['blueprint']['hostname']}"
node.default['blueprint']['apps']['cae-preview']['application.properties']['blueprint.site.mapping.corporate'] = "//preview.#{node['blueprint']['hostname']}"
node.default['blueprint']['apps']['studio']['application.properties']['blueprint.site.mapping.corporate'] = "//preview.#{node['blueprint']['hostname']}"
node.default['blueprint']['apps']['headless-server-preview']['application.properties']['blueprint.site.mapping.corporate'] = "//preview.#{node['blueprint']['hostname']}"
node.default['blueprint']['apps']['headless-server-live']['application.properties']['blueprint.site.mapping.corporate'] = "//corporate.#{node['blueprint']['hostname']}"

if node['blueprint']['sfcc']['enabled']
  node.default['blueprint']['apps']['cae-live']['application.properties']['blueprint.site.mapping.sitegenesis'] = "//sitegenesis.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio']['application.properties']['blueprint.site.mapping.sitegenesis'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-preview']['application.properties']['blueprint.site.mapping.sitegenesis'] = "//preview.#{node['blueprint']['hostname']}"

  node.default['blueprint']['apps']['cae-live']['application.properties']['blueprint.site.mapping.sfra'] = "//sfra.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio']['application.properties']['blueprint.site.mapping.sfra'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-preview']['application.properties']['blueprint.site.mapping.sfra'] = "//preview.#{node['blueprint']['hostname']}"

  node.default['blueprint']['apps']['headless-server-preview']['application.properties']['blueprint.site.mapping.sfra'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['headless-server-live']['application.properties']['blueprint.site.mapping.sfra'] = "//sfra.#{node['blueprint']['hostname']}"

  node.default['blueprint']['apps']['headless-server-preview']['application.properties']['blueprint.site.mapping.sitegenesis'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['headless-server-live']['application.properties']['blueprint.site.mapping.sitegenesis'] = "//sitegenesis.#{node['blueprint']['hostname']}"
end

if node['blueprint']['ibm-wcs']['enabled']
  node.default['blueprint']['apps']['cae-live']['application.properties']['blueprint.site.mapping.calista'] = "//calista.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-preview']['application.properties']['blueprint.site.mapping.calista'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio']['application.properties']['blueprint.site.mapping.calista'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-live']['application.properties']['blueprint.site.mapping.helios'] = "//helios.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-preview']['application.properties']['blueprint.site.mapping.helios'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio']['application.properties']['blueprint.site.mapping.helios'] = "//preview.#{node['blueprint']['hostname']}"

  node.default['blueprint']['apps']['headless-server-preview']['application.properties']['blueprint.site.mapping.helios'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['headless-server-live']['application.properties']['blueprint.site.mapping.helios'] = "//helios.#{node['blueprint']['hostname']}"

  node.default['blueprint']['apps']['headless-server-preview']['application.properties']['blueprint.site.mapping.calista'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['headless-server-live']['application.properties']['blueprint.site.mapping.calista'] = "//calista.#{node['blueprint']['hostname']}"
end

if node['blueprint']['sap-hybris']['enabled']
  node.default['blueprint']['apps']['cae-live']['application.properties']['blueprint.site.mapping.apparel'] = "//apparel.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['studio']['application.properties']['blueprint.site.mapping.apparel'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['cae-preview']['application.properties']['blueprint.site.mapping.apparel'] = "//preview.#{node['blueprint']['hostname']}"

  node.default['blueprint']['apps']['headless-server-preview']['application.properties']['blueprint.site.mapping.apparel'] = "//preview.#{node['blueprint']['hostname']}"
  node.default['blueprint']['apps']['headless-server-live']['application.properties']['blueprint.site.mapping.apparel'] = "//apparel.#{node['blueprint']['hostname']}"
end
