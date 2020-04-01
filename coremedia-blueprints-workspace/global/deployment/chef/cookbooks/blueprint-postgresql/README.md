# Description

This is the wrapper cookbook to install postgresql.



# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* blueprint-base
* blueprint-yum
* yum-pgdg (~> 3.0.0)
* postgresql (~> 7.1.4)

# Attributes

* `node['blueprint']['postgresql']['schemas']` - An array of schemas create when using the schema recipe. Defaults to `[ ... ]`.
* `node['blueprint']['postgresql']['version']` -  Defaults to `9.6`.
* `node['blueprint']['postgresql']['initial_root_password']` - inital root password for postgresql db. Defaults to `coremedia`.
* `node['blueprint']['postgresql']['config']['huge_pages']` - enable huge pages valid values are: (try | on | off). Defaults to `off`.
* `node['blueprint']['postgresql']['config']['max_connections']` -  Defaults to `100`.
* `node['blueprint']['postgresql']['config']['shared_buffers']` -  Defaults to `512MB`.
* `node['blueprint']['postgresql']['config']['effective_cache_size']` -  Defaults to `512MB`.
* `node['blueprint']['postgresql']['config']['work_mem']` -  Defaults to `16MB`.
* `node['blueprint']['postgresql']['config']['maintenance_work_mem']` -  Defaults to `64MB`.
* `node['blueprint']['postgresql']['config']['checkpoint_segments']` -  Defaults to `384MB`.
* `node['blueprint']['postgresql']['config']['checkpoint_completion_target']` -  Defaults to `0.9`.
* `node['blueprint']['postgresql']['config']['default_statistics_target']` -  Defaults to `100`.
* `node['blueprint']['postgresql']['config']['autovacuum']` -  Defaults to `false`.

# Recipes

* [blueprint-postgresql::default](#blueprint-postgresqldefault) - This recipe installs and configures postgresql and creates schemas for CoreMedia Blueprint.
* [blueprint-postgresql::schemas](#blueprint-postgresqlschemas) - This recipe installs and configures postgresql client and creates schemas.
* [blueprint-postgresql::server](#blueprint-postgresqlserver) - This recipe installs and configures postgresql.

## blueprint-postgresql::default

This recipe installs and configures postgresql and creates schemas for CoreMedia Blueprint.

## blueprint-postgresql::schemas

This recipe installs and configures postgresql client and creates schemas.

## blueprint-postgresql::server

This recipe installs and configures postgresql.

# Author

Author:: Your Name (<your_name@domain.com>)
