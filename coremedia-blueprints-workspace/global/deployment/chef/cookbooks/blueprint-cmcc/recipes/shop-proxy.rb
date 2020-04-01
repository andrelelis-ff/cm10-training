=begin
#<
This recipe installs virtual hosts for the CoreMedia Blueprint LiveContext SFCC Live Shop.
#>
=end

include_recipe 'blueprint-proxy::_base'
if node['blueprint']['sfcc']['enabled']
  template "sfcc-commerce-shop" do
    extend Apache2::Cookbook::Helpers
    source 'vhosts/sfcc/shop.erb'
    path "#{apache_dir}/sites-available/sfcc-commerce-shop.conf"
    variables(
            application_name: "sfcc-commerce-shop",
            server_name: node['blueprint']['sfcc']['virtual_host']['shop']['server_name'],
            server_aliases: node['blueprint']['sfcc']['virtual_host']['shop']['server_aliases'],
            sfcc_host: node['blueprint']['sfcc']['host'],
            rewrite_log_level: node['blueprint']['sfcc']['virtual_host']['shop']['rewrite_log_level'],
            headers: node['blueprint']['sfcc']['virtual_host']['shop']['headers'],
            ssl_proxy_verify: node['blueprint']['sfcc']['ssl_proxy_verify'],
            preview: false
    )
  end
  apache2_site "sfcc-commerce-shop"
end

if node['blueprint']['ibm-wcs']['enabled']
  template "ibm-wcs-commerce-shop" do
    extend Apache2::Cookbook::Helpers
    source 'vhosts/ibm-wcs/shop.erb'
    path "#{apache_dir}/sites-available/ibm-wcs-commerce-shop.conf"
    variables(
            application_name: "ibm-wcs-commerce-shop",
            server_name: node['blueprint']['ibm-wcs']['virtual_host']['shop']['server_name'],
            server_aliases: node['blueprint']['ibm-wcs']['virtual_host']['shop']['server_aliases'],
            time_travel_alias: node['blueprint']['ibm-wcs']['virtual_host']['shop']['time_travel_alias'],
            wcs_host: node['blueprint']['ibm-wcs']['host'],
            rewrite_log_level: node['blueprint']['ibm-wcs']['virtual_host']['shop']['rewrite_log_level'],
            site_server_name: node['blueprint']['proxy']['virtual_host']['preview']['server_name'],
            headers: node['blueprint']['ibm-wcs']['virtual_host']['shop']['headers'],
            ssl_proxy_verify: node['blueprint']['ibm-wcs']['ssl_proxy_verify'],
            ssl: "true",
            protocol: "https"
    )
  end
  apache2_site "ibm-wcs-commerce-shop"
end

if node['blueprint']['sap-hybris']['enabled']
  template "sap-hybris-commerce-shop" do
    extend  Apache2::Cookbook::Helpers
    source 'vhosts/sap-hybris/shop.erb'
    path "#{apache_dir}/sites-available/sap-hybris-commerce-shop.conf"
    variables(
            application_name: "sap-hybris-commerce-shop",
            server_name: node['blueprint']['sap-hybris']['virtual_host']['shop']['server_name'],
            server_aliases: node['blueprint']['sap-hybris']['virtual_host']['shop']['server_aliases'],
            hybris_host: node['blueprint']['sap-hybris']['host'],
            rewrite_log_level: node['blueprint']['sap-hybris']['virtual_host']['shop']['rewrite_log_level'],
            headers: node['blueprint']['sap-hybris']['virtual_host']['shop']['headers'],
            ssl_proxy_verify: node['blueprint']['sap-hybris']['ssl_proxy_verify'],
            preview: false
    )
  end
  apache2_site "sap-hybris-commerce-shop"
end
