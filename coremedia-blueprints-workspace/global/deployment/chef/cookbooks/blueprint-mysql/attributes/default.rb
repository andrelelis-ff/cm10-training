#<> An array of schemas create when using the schema recipe
default['blueprint']['mysql']['schemas'] = []

#<> the innodb buffer pool size in megabytes
default['blueprint']['mysql']['innodb_buffer_pool_size_mb'] = 512
#<> The directory to log to
default['blueprint']['mysql']['log_dir'] = '/var/log/mysql'
#<> Toggle to activate slow query log
default['blueprint']['mysql']['slow_query_log'] = false
#<> Toggle to activate general logging of all queries
default['blueprint']['mysql']['general_log'] = false
#<> The default timezone
default['blueprint']['mysql']['timezone'] = '+00:00'

#<> mysql version to use
default['blueprint']['mysql']['version'] = '5.7'
#<> inital root password for mysql db
default['blueprint']['mysql']['initial_root_password'] = 'coremedia'
#<> port to be used by mysql
default['blueprint']['mysql']['port'] = '3306'
#<> mysql socket file path
default['blueprint']['mysql']['socket'] = '/var/lib/mysql/mysql.sock'

#<> mysql yum repo base url
default['blueprint']['mysql']['baseurl'] = "http://repo.mysql.com/yum/mysql-#{node['blueprint']['mysql']['version']}-community/el/$releasever/$basearch/"

#<> mysql yum repo base url for amazon platform
default['blueprint']['mysql']['baseurl'] = "http://repo.mysql.com/yum/mysql-#{node['blueprint']['mysql']['version']}-community/el/6/$basearch/" if node['platform'] == 'amazon'

if node['yum']['mysql57-community']
  #<> mysql yum repo base url using attribute node['yum']['mysql57-community'] when avail (used to be backward compatible)
  default['blueprint']['mysql']['baseurl'] =  node['yum']['mysql57-community']['baseurl'] unless node['yum']['mysql57-community']['baseurl'].nil?
  Chef::Log.warn("The attribute to define the remote mysql rpm repository url has been changed from node['yum']['mysql57-community']['baseurl'] to node['blueprint']['mysql']['baseurl']. Please change your attribute accordingly, as we will remove this backwards compatibility with the next AEP") unless node['yum']['mysql57-community']['baseurl'].nil?

  #<> mysql yum repo mirrorlist url using attribute node['yum']['mysql57-community'] when avail (used to be backward compatible)
  default['blueprint']['mysql']['mirrorlist'] =  node['yum']['mysql57-community']['mirrorlist'] unless node['yum']['mysql57-community']['mirrorlist'].nil?
  Chef::Log.warn("The attribute to define the remote mysql rpm repository url has been changed from node['yum']['mysql57-community']['mirrorlist'] to node['blueprint']['mysql']['mirrorlist']. Please change your attribute accordingly, as we will remove this backwards compatibility with the next AEP") unless node['yum']['mysql57-community']['mirrorlist'].nil?
end

#<> mysql yum repo gpgkey
default['blueprint']['mysql']['gpgkey'] = 'https://repo.mysql.com/RPM-GPG-KEY-mysql'

#<> mysql yum repo failovermethod
default['blueprint']['mysql']['failovermethod'] = 'priority'
