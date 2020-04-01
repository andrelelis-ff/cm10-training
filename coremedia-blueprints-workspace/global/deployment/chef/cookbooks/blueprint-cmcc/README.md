# Description

This is the wrapper cookbook to deploy the config for CoreMedia Content Cloud demo sites.

# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* blueprint-base
* blueprint-proxy
* apache2 (~> 7.1.0)

# Attributes

* `node['blueprint']['corporate']['ssl_proxy_verify']` -  Defaults to `true`.
* `node['blueprint']['corporate']['virtual_host']['delivery']['cluster']['default']['host']` -  Defaults to `localhost`.
* `node['blueprint']['corporate']['virtual_host']['delivery']['cluster']['default']['port']` -  Defaults to `42180`.
* `node['blueprint']['corporate']['virtual_host']['delivery']['context']` -  Defaults to `blueprint`.
* `node['blueprint']['corporate']['virtual_host']['delivery']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['corporate']['virtual_host']['delivery']['sites']['corporate']['server_name']` -  Defaults to `corporate.#{node['blueprint']['hostname']}`.
* `node['blueprint']['corporate']['virtual_host']['delivery']['sites']['corporate']['default_site']` -  Defaults to `corporate`.
* `node['blueprint']['corporate']['virtual_host']['delivery']['sites']['corporate']['site_id']` - The id property of the CMSite content associated with this site. Defaults to `abffe57734feeee`.
* `node['blueprint']['sfcc']['host']` - Convenience property to set the hostname of sfcc. Do not use or set this attribute in recipes, use the concrete attributes instead. Defaults to `localhost`.
* `node['blueprint']['sfcc']['enabled']` - Set to true to activate the Salesforce Commercer Cloud integration. Defaults to `false`.
* `node['blueprint']['sfcc']['virtual_host']['delivery']['cluster']['default']['host']` -  Defaults to `localhost`.
* `node['blueprint']['sfcc']['virtual_host']['delivery']['cluster']['default']['port']` -  Defaults to `42180`.
* `node['blueprint']['sfcc']['virtual_host']['delivery']['context']` -  Defaults to `blueprint`.
* `node['blueprint']['sfcc']['virtual_host']['delivery']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['sfcc']['virtual_host']['delivery']['sites']['sitegenesis']['server_name']` -  Defaults to `sitegenesis.#{node['blueprint']['hostname']}`.
* `node['blueprint']['sfcc']['virtual_host']['delivery']['sites']['sitegenesis']['default_site']` -  Defaults to `sitegenesishomepage`.
* `node['blueprint']['sfcc']['virtual_host']['delivery']['sites']['sitegenesis']['site_id']` - The id property of the CMSite content associated with this site. Defaults to `SFCC-sitegenesis-UK-Site-ID`.
* `node['blueprint']['sfcc']['cms_public_host']` -  Defaults to `localhost`.
* `node['blueprint']['proxy']['virtual_host']['preview']['server_aliases']['sfcc']` -  Defaults to `preview-#{node['blueprint']['sfcc']['cms_public_host']}`.
* `node['blueprint']['sfcc']['virtual_host']['preview']['server_aliases']['sfcc']` -  Defaults to `preview-#{node['blueprint']['sfcc']['cms_public_host']}`.
* `node['blueprint']['sfcc']['virtual_host']['delivery']['sites']['sitegenesis']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['sfcc']['virtual_host']['shop-preview']['server_name']` -  Defaults to `shop-preview-sfcc.#{node['blueprint']['hostname']}`.
* `node['blueprint']['sfcc']['virtual_host']['shop-preview']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['sfcc']['virtual_host']['shop-preview']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['sfcc']['virtual_host']['shop']['server_name']` -  Defaults to `shop-sfcc.#{node['blueprint']['hostname']}`.
* `node['blueprint']['sfcc']['virtual_host']['shop']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['sfcc']['virtual_host']['shop']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['sfcc']['ssl_proxy_verify']` -  Defaults to `true`.
* `node['blueprint']['ibm-wcs']['host']` - Convenience property to set the hostname of wcs. Do not use or set this attribute in recipes, use the concrete attributes instead. Defaults to `localhost`.
* `node['blueprint']['ibm-wcs']['enabled']` - Set to true to activate the IBM Websphere Commerce integration. Defaults to `false`.
* `node['blueprint']['ibm-wcs']['application.properties']['livecontext.service.credentials.username']` -  Defaults to `cmadmin`.
* `node['blueprint']['ibm-wcs']['application.properties']['livecontext.service.credentials.password']` -  Defaults to `VTJjyo0AYSnXFHI201yo`.
* `node['blueprint']['ibm-wcs']['application.properties']['livecontext.cookie.domain']` -  Defaults to `.#{node['fqdn']}`.
* `node['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.host']` -  Defaults to `node['blueprint']['ibm-wcs']['host']`.
* `node['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.url-keyword']` -  Defaults to `cm`.
* `node['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.store.name.aurora']` -  Defaults to `AuroraESite`.
* `node['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.currency.aurora']` -  Defaults to `USD`.
* `node['blueprint']['ibm-wcs']['application.properties']['livecontext.ibm.wcs.vendor.aurora']` -  Defaults to `ibm`.
* `node['blueprint']['ibm-wcs']['ssl_proxy_verify']` -  Defaults to `true`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['cluster']['default']['host']` -  Defaults to `localhost`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['cluster']['default']['port']` -  Defaults to `42180`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['context']` -  Defaults to `blueprint`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['helios']['server_name']` -  Defaults to `helios.#{node['blueprint']['hostname']}`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['helios']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['helios']['default_site']` -  Defaults to `aurora`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['helios']['site_id']` -  Defaults to `99c8ef576f385bc322564d5694df6fc2`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['calista']['server_name']` -  Defaults to `calista.#{node['blueprint']['hostname']}`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['calista']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['calista']['default_site']` -  Defaults to `calista`.
* `node['blueprint']['ibm-wcs']['virtual_host']['delivery']['sites']['calista']['site_id']` -  Defaults to `ced8921aa7b7f9b736b90e19afc2dd2a`.
* `node['blueprint']['ibm-wcs']['virtual_host']['shop']['server_name']` -  Defaults to `shop-ibm.#{node['blueprint']['hostname']}`.
* `node['blueprint']['ibm-wcs']['virtual_host']['shop']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['ibm-wcs']['virtual_host']['shop']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['ibm-wcs']['virtual_host']['shop-preview']['server_name']` -  Defaults to `shop-preview-production-ibm.#{node['blueprint']['hostname']}`.
* `node['blueprint']['ibm-wcs']['virtual_host']['shop-preview']['time_travel_alias']` -  Defaults to `shop-preview-ibm.#{node['blueprint']['hostname']}`.
* `node['blueprint']['ibm-wcs']['virtual_host']['shop-preview']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['ibm-wcs']['virtual_host']['shop-preview']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['proxy']['virtual_host']['preview']['server_aliases']['ibm-wcs']` -  Defaults to `preview-fragment.supplier.blueprint-box.vagrant`.
* `node['blueprint']['sap-hybris']['host']` -  Defaults to `hybrishost`.
* `node['blueprint']['sap-hybris']['enabled']` - Set to true to activate the SAP Hybris Commerce integration. Defaults to `false`.
* `node['blueprint']['sap-hybris']['application.properties']['livecontext.cookie.domain']` -  Defaults to `.#{node['fqdn']}`.
* `node['blueprint']['sap-hybris']['virtual_host']['delivery']['cluster']['default']['host']` -  Defaults to `localhost`.
* `node['blueprint']['sap-hybris']['virtual_host']['delivery']['cluster']['default']['port']` -  Defaults to `42180`.
* `node['blueprint']['sap-hybris']['virtual_host']['delivery']['context']` -  Defaults to `blueprint`.
* `node['blueprint']['sap-hybris']['virtual_host']['delivery']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['sap-hybris']['virtual_host']['delivery']['sites']['apparel']['server_name']` -  Defaults to `apparel.#{node['blueprint']['hostname']}`.
* `node['blueprint']['sap-hybris']['virtual_host']['delivery']['sites']['apparel']['default_site']` -  Defaults to `apparelhomepage`.
* `node['blueprint']['sap-hybris']['virtual_host']['delivery']['sites']['apparel']['site_id']` - The id property of the CMSite content associated with this site. Defaults to `Hybris-Apparel-UK-Site-ID`.
* `node['blueprint']['sap-hybris']['virtual_host']['shop-preview']['server_name']` -  Defaults to `shop-preview-hybris.#{node['blueprint']['hostname']}`.
* `node['blueprint']['sap-hybris']['virtual_host']['shop-preview']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['sap-hybris']['virtual_host']['shop-preview']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['sap-hybris']['virtual_host']['shop']['server_name']` -  Defaults to `shop-hybris.#{node['blueprint']['hostname']}`.
* `node['blueprint']['sap-hybris']['virtual_host']['shop']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['sap-hybris']['virtual_host']['shop']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['sap-hybris']['ssl_proxy_verify']` -  Defaults to `true`.

# Recipes

* blueprint-cmcc::cae-live-config
* [blueprint-cmcc::cae-preview-config](#blueprint-cmcccae-preview-config) - This recipe installs and configures the CoreMedia Blueprint Preview CAE.
* [blueprint-cmcc::delivery-proxy](#blueprint-cmccdelivery-proxy) - This recipe installs virtual hosts for the CoreMedia Blueprint Live CAE.
* [blueprint-cmcc::overview](#blueprint-cmccoverview)
* [blueprint-cmcc::shop-preview-proxy](#blueprint-cmccshop-preview-proxy) - This recipe installs virtual hosts for the CoreMedia Blueprint LiveContext SFCC Preview Shop.
* [blueprint-cmcc::shop-proxy](#blueprint-cmccshop-proxy) - This recipe installs virtual hosts for the CoreMedia Blueprint LiveContext SFCC Live Shop.
* [blueprint-cmcc::studio-config](#blueprint-cmccstudio-config) - This recipe configures the CoreMedia Blueprint Studio.
* [blueprint-cmcc::test-data-config](#blueprint-cmcctest-data-config)

## blueprint-cmcc::cae-preview-config

This recipe installs and configures the CoreMedia Blueprint Preview CAE.

## blueprint-cmcc::delivery-proxy

This recipe installs virtual hosts for the CoreMedia Blueprint Live CAE.

## blueprint-cmcc::overview

creates a simple overview page with all necessary links at `overview.<hostname>` for dev systems

## blueprint-cmcc::shop-preview-proxy

This recipe installs virtual hosts for the CoreMedia Blueprint LiveContext SFCC Preview Shop.

## blueprint-cmcc::shop-proxy

This recipe installs virtual hosts for the CoreMedia Blueprint LiveContext SFCC Live Shop.

## blueprint-cmcc::studio-config

This recipe configures the CoreMedia Blueprint Studio.

## blueprint-cmcc::test-data-config

This recipe sets properties only necessary if the test content is being used

# Author

Author:: Your Name (<your_name@domain.com>)
