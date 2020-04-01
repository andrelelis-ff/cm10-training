#
# Cookbook Name:: solr
# Attributes:: default
#
# Copyright 2018, CoreMedia AG
#

# <> define solr version to use
default['blueprint']['solr']['version'] = '8.4.1'
# <> define solr download url
default['blueprint']['solr']['url'] = "http://archive.apache.org/dist/lucene/solr/#{node['blueprint']['solr']['version']}/solr-#{node['blueprint']['solr']['version']}.tgz"
# <> define artifact checksum
default['blueprint']['solr']['checksum'] = 'ec39e1e024b2e37405149de41e39e875a39bf11a53f506d07d96b47b8d2a4301'
# <> define solr home
default['blueprint']['solr']['solr_home'] = '/opt/coremedia/solr-home'
#<> clean solr home on update
default['blueprint']['solr']['clean_solr_home_on_update'] = true
# <> define solr index data directory
default['blueprint']['solr']['solr_data_dir'] = '/var/coremedia/solr-data'
# <> define solr dir
default['blueprint']['solr']['dir'] = '/opt/solr'
# <> define solr port
default['blueprint']['solr']['port'] = '40080'
# <> define solr JMX RMI port
default['blueprint']['solr']['jmx_port'] = '40099'
# <> define whether Solr should activate the JMX RMI connector to allow remote JMX client applications to connect
default['blueprint']['solr']['jmx_enable'] = 'false'
# <> define solr pid dir
default['blueprint']['solr']['pid_dir'] = '/var/run/solr/'
# <> define solr log dir
default['blueprint']['solr']['log_dir'] = '/var/log/solr/'
# <> define solr user
default['blueprint']['solr']['user'] = 'solr'
# <> define solr group
default['blueprint']['solr']['group'] = node['blueprint']['group']
# <> set to the java home used for solr
default['blueprint']['solr']['java_home'] = '/usr/lib/jvm/java'
# <> define solr_java options
default['blueprint']['solr']['java_mem'] = '-Xms128M -Xmx512M'
# <> define the maven group id for solr-config.zip
default['blueprint']['solr']['config_zip_group_id'] = 'com.coremedia.blueprint'
# <> define the maven artifact id for solr-config.zip
default['blueprint']['solr']['config_zip_artifact_id'] = 'solr-config'
# <> The repository url from which the webapp artifacts are downloaded
default['blueprint']['maven_repository_url'] = 'file://localhost/maven-repo/'
# <> Toggles if the service should be started or not
default['blueprint']['solr']['start_service'] = true
