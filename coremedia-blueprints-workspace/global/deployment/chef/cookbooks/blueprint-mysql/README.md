# Description

This is the wrapper cookbook to install mysql.


# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* mysql (~> 8.5.1)
* blueprint-base

# Attributes

* `node['blueprint']['mysql']['schemas']` - An array of schemas create when using the schema recipe. Defaults to `[ ... ]`.
* `node['blueprint']['mysql']['innodb_buffer_pool_size_mb']` - the innodb buffer pool size in megabytes. Defaults to `512`.
* `node['blueprint']['mysql']['log_dir']` - The directory to log to. Defaults to `/var/log/mysql`.
* `node['blueprint']['mysql']['slow_query_log']` - Toggle to activate slow query log. Defaults to `false`.
* `node['blueprint']['mysql']['general_log']` - Toggle to activate general logging of all queries. Defaults to `false`.
* `node['blueprint']['mysql']['timezone']` - The default timezone. Defaults to `+00:00`.
* `node['blueprint']['mysql']['version']` - mysql version to use. Defaults to `5.7`.
* `node['blueprint']['mysql']['initial_root_password']` - inital root password for mysql db. Defaults to `coremedia`.
* `node['blueprint']['mysql']['port']` - port to be used by mysql. Defaults to `3306`.
* `node['blueprint']['mysql']['socket']` - mysql socket file path. Defaults to `/var/lib/mysql/mysql.sock`.
* `node['blueprint']['mysql']['baseurl']` - mysql yum repo base url using attribute node['yum']['mysql57-community'] when avail (used to be backward compatible). Defaults to `node['yum']['mysql57-community']['baseurl'] unless node['yum']['mysql57-community']['baseurl'].nil?`.
* `node['blueprint']['mysql']['mirrorlist']` - mysql yum repo mirrorlist url using attribute node['yum']['mysql57-community'] when avail (used to be backward compatible). Defaults to `node['yum']['mysql57-community']['mirrorlist'] unless node['yum']['mysql57-community']['mirrorlist'].nil?`.
* `node['blueprint']['mysql']['gpgkey']` - mysql yum repo gpgkey. Defaults to `https://repo.mysql.com/RPM-GPG-KEY-mysql`.
* `node['blueprint']['mysql']['failovermethod']` - mysql yum repo failovermethod. Defaults to `priority`.

# Recipes

* [blueprint-mysql::default](#blueprint-mysqldefault) - This recipe installs and configures mysql and creates schemas for CoreMedia Blueprint.
* [blueprint-mysql::schemas](#blueprint-mysqlschemas) - This recipe installs and configures mysql client and creates schemas.
* [blueprint-mysql::server](#blueprint-mysqlserver) - This recipe installs and configures mysql.

## blueprint-mysql::default

This recipe installs and configures mysql and creates schemas for CoreMedia Blueprint.

## blueprint-mysql::schemas

This recipe installs and configures mysql client and creates schemas.

## blueprint-mysql::server

This recipe installs and configures mysql.

# Author

Author:: Your Name (<your_name@domain.com>)
