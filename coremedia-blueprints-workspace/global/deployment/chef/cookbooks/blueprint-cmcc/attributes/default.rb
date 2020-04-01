# Corporate site
default['blueprint']['corporate']['ssl_proxy_verify'] = true

default['blueprint']['corporate']['virtual_host']['delivery']['cluster']['default']['host'] = 'localhost'
default['blueprint']['corporate']['virtual_host']['delivery']['cluster']['default']['port'] = '42180'
default['blueprint']['corporate']['virtual_host']['delivery']['context'] = 'blueprint'
default['blueprint']['corporate']['virtual_host']['delivery']['rewrite_log_level'] = 'trace1'
default['blueprint']['corporate']['virtual_host']['delivery']['sites']['corporate']['server_name'] = "corporate.#{node['blueprint']['hostname']}"
default['blueprint']['corporate']['virtual_host']['delivery']['sites']['corporate']['default_site'] = 'corporate'
#<> The id property of the CMSite content associated with this site
default['blueprint']['corporate']['virtual_host']['delivery']['sites']['corporate']['site_id'] = 'abffe57734feeee'

# SFCC
#<> Convenience property to set the hostname of sfcc. Do not use or set this attribute in recipes, use the concrete attributes instead.
default['blueprint']['sfcc']['host'] = 'localhost'
#<> Set to true to activate the Salesforce Commercer Cloud integration
default['blueprint']['sfcc']['enabled'] = false

default['blueprint']['sfcc']['virtual_host']['delivery']['cluster']['default']['host'] = 'localhost'
default['blueprint']['sfcc']['virtual_host']['delivery']['cluster']['default']['port'] = '42180'
default['blueprint']['sfcc']['virtual_host']['delivery']['context'] = 'blueprint'
default['blueprint']['sfcc']['virtual_host']['delivery']['rewrite_log_level'] = 'trace1'
default['blueprint']['sfcc']['virtual_host']['delivery']['sites']['sitegenesis']['server_name'] = "sitegenesis.#{node['blueprint']['hostname']}"
default['blueprint']['sfcc']['virtual_host']['delivery']['sites']['sitegenesis']['default_site'] = 'sitegenesishomepage'
#<> The id property of the CMSite content associated with this site
default['blueprint']['sfcc']['virtual_host']['delivery']['sites']['sitegenesis']['site_id'] = 'SFCC-sitegenesis-UK-Site-ID'

default['blueprint']['sfcc']['cms_public_host'] = 'localhost'
default['blueprint']['proxy']['virtual_host']['preview']['server_aliases']['sfcc'] = "preview-#{node['blueprint']['sfcc']['cms_public_host']}"
default['blueprint']['sfcc']['virtual_host']['preview']['server_aliases']['sfcc'] = "preview-#{node['blueprint']['sfcc']['cms_public_host']}"
default['blueprint']['sfcc']['virtual_host']['delivery']['sites']['sitegenesis']['server_aliases'] = ["#{node['blueprint']['sfcc']['cms_public_host']}","sfra.#{node['blueprint']['hostname']}"]

default['blueprint']['sfcc']['virtual_host']['shop-preview']['server_name'] = "shop-preview-sfcc.#{node['blueprint']['hostname']}"
default['blueprint']['sfcc']['virtual_host']['shop-preview']['server_aliases'] = []
default['blueprint']['sfcc']['virtual_host']['shop-preview']['rewrite_log_level'] = 'trace1'

default['blueprint']['sfcc']['virtual_host']['shop']['server_name'] = "shop-sfcc.#{node['blueprint']['hostname']}"
default['blueprint']['sfcc']['virtual_host']['shop']['server_aliases'] = []
default['blueprint']['sfcc']['virtual_host']['shop']['rewrite_log_level'] = 'trace1'

# set this to true to disable SSLProxyVerify, SSLProxyCheckPeerCN, SSLProxyCheckPeerName
default['blueprint']['sfcc']['ssl_proxy_verify'] = true

# IBM
# WCS configuration
#<> Convenience property to set the hostname of wcs. Do not use or set this attribute in recipes, use the concrete attributes instead.
default['blueprint']['ibm-wcs']['host'] = 'localhost'
#<> Set to true to activate the IBM Websphere Commerce integration
default['blueprint']['ibm-wcs']['enabled'] = false
default['blueprint']['ibm-wcs']['application.properties']['livecontext.service.credentials.username'] = 'cmadmin'
default['blueprint']['ibm-wcs']['application.properties']['livecontext.service.credentials.password'] = 'VTJjyo0AYSnXFHI201yo'
default['blueprint']['ibm-wcs']['application.properties']['livecontext.cookie.domain'] = ".#{node['fqdn']}"
default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.host'] = node['blueprint']['ibm-wcs']['host']
default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.url-keyword'] = 'cm'
default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.store.name.aurora'] = 'AuroraESite'
default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.currency.aurora'] = 'USD'
default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.vendor.aurora'] = 'ibm'
default['blueprint']['ibm-wcs']['application.properties']['cae.crossdomain.whitelist'] = "https://#{node['blueprint']['ibm-wcs']['host']}:8443"

# The following properties are derived from `livecontext.ibm.wcs.host`, if you need to set them explicitly comment in the lines below
# default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.url'] = "http://#{node['blueprint']['ibm-wcs']['host']}"
# default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.secureUrl'] = "https://#{node['blueprint']['ibm-wcs']['host']}"
# default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.rest.search.url'] = "http://#{node['blueprint']['ibm-wcs']['host']}:3737/search/resources"
# default['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.rest.search.secureUrl'] = "https://#{node['blueprint']['ibm-wcs']['host']}:3738/search/resources"
# default['blueprint']['ibm-wcs']['application.properties']['livecontext.managementtool.web.url'] = "https://#{node['blueprint']['ibm-wcs']['host']}:8000/lobtools/CoreMediaManagementCenterWrapper.html"

# set this to true to disable SSLProxyVerify, SSLProxyCheckPeerCN, SSLProxyCheckPeerName
default['blueprint']['ibm-wcs']['ssl_proxy_verify'] = true

default['blueprint']['ibm-wcs']['virtual_host']['delivery']['cluster']['default']['host'] = 'localhost'
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['cluster']['default']['port'] = '42180'
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['context'] = 'blueprint'

# Commerceled-led site, the id property of the CMSite content associated with this site_id
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['rewrite_log_level'] = 'trace1'
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['helios']['server_name'] = "helios.#{node['blueprint']['hostname']}"
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['helios']['server_aliases'] = ['fragment.supplier.blueprint-box.vagrant']
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['helios']['default_site'] = 'aurora'
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['helios']['site_id'] = '99c8ef576f385bc322564d5694df6fc2'

# Content-led site
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['calista']['server_name'] = "calista.#{node['blueprint']['hostname']}"
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['calista']['server_aliases'] = ['fragment.supplier.blueprint-box.vagrant']
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['calista']['default_site'] = 'calista'
default['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['calista']['site_id'] = 'ced8921aa7b7f9b736b90e19afc2dd2a'

default['blueprint']['ibm-wcs']['virtual_host']['shop']['server_name'] = "shop-ibm.#{node['blueprint']['hostname']}"
default['blueprint']['ibm-wcs']['virtual_host']['shop']['server_aliases'] = []
default['blueprint']['ibm-wcs']['virtual_host']['shop']['rewrite_log_level'] = 'trace1'

default['blueprint']['ibm-wcs']['virtual_host']['shop-preview']['server_name'] = "shop-preview-production-ibm.#{node['blueprint']['hostname']}"
default['blueprint']['ibm-wcs']['virtual_host']['shop-preview']['time_travel_alias'] = "shop-preview-ibm.#{node['blueprint']['hostname']}"
default['blueprint']['ibm-wcs']['virtual_host']['shop-preview']['server_aliases'] = []
default['blueprint']['ibm-wcs']['virtual_host']['shop-preview']['rewrite_log_level'] = 'trace1'
# there is only one preview, we need to register an alias for the shop-preview-production fragment augmentation
default['blueprint']['proxy']['virtual_host']['preview']['server_aliases']['ibm-wcs'] = 'preview-fragment.supplier.blueprint-box.vagrant'

# SAP Hybris
# Hybris configuration
default['blueprint']['sap-hybris']['host'] = 'hybrishost'
#<> Set to true to activate the SAP Hybris Commerce integration
default['blueprint']['sap-hybris']['enabled'] = false
default['blueprint']['sap-hybris']['application.properties']['livecontext.cookie.domain'] = ".#{node['fqdn']}"

default['blueprint']['sap-hybris']['virtual_host']['delivery']['cluster']['default']['host'] = 'localhost'
default['blueprint']['sap-hybris']['virtual_host']['delivery']['cluster']['default']['port'] = '42180'
default['blueprint']['sap-hybris']['virtual_host']['delivery']['context'] = 'blueprint'
default['blueprint']['sap-hybris']['virtual_host']['delivery']['rewrite_log_level'] = 'trace1'
default['blueprint']['sap-hybris']['virtual_host']['delivery']['sites']['apparel']['server_name'] = "apparel.#{node['blueprint']['hostname']}"
default['blueprint']['sap-hybris']['virtual_host']['delivery']['sites']['apparel']['default_site'] = 'apparelhomepage'
#<> The id property of the CMSite content associated with this site
default['blueprint']['sap-hybris']['virtual_host']['delivery']['sites']['apparel']['site_id'] = 'Hybris-Apparel-UK-Site-ID'

default['blueprint']['sap-hybris']['virtual_host']['shop-preview']['server_name'] = "shop-preview-hybris.#{node['blueprint']['hostname']}"
default['blueprint']['sap-hybris']['virtual_host']['shop-preview']['server_aliases'] = []
default['blueprint']['sap-hybris']['virtual_host']['shop-preview']['rewrite_log_level'] = 'trace1'

default['blueprint']['sap-hybris']['virtual_host']['shop']['server_name'] = "shop-hybris.#{node['blueprint']['hostname']}"
default['blueprint']['sap-hybris']['virtual_host']['shop']['server_aliases'] = []
default['blueprint']['sap-hybris']['virtual_host']['shop']['rewrite_log_level'] = 'trace1'

# set this to true to disable SSLProxyVerify, SSLProxyCheckPeerCN, SSLProxyCheckPeerName
default['blueprint']['sap-hybris']['ssl_proxy_verify'] = true
