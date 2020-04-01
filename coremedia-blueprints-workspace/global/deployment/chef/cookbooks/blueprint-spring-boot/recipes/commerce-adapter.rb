include_recipe 'blueprint-spring-boot::commerce-adapter-sfcc' if node['blueprint']['sfcc']['enabled']
include_recipe 'blueprint-spring-boot::commerce-adapter-ibm-wcs' if node['blueprint']['ibm-wcs']['enabled']
include_recipe 'blueprint-spring-boot::commerce-adapter-sap-hybris' if node['blueprint']['sap-hybris']['enabled']
