# Description

This cookbook is a wrapper cookbook for the yum cookbook to setup the default yum repos.

# Requirements


## Chef Client:

*No Chef versions defined*

## Platform:

* redhat
* centos
* amazon

## Cookbooks:

* yum (~> 5.1.0)
* yum-centos (~> 3.1.0)

# Attributes

* `node['yum']['base']['managed']` -  Defaults to `true`.
* `node['yum']['centosplus']['managed']` -  Defaults to `false`.
* `node['yum']['updates']['managed']` -  Defaults to `false`.
* `node['yum']['extras']['managed']` -  Defaults to `false`.
* `node['yum']['contrib']['managed']` -  Defaults to `false`.
* `node['yum']['cr']['managed']` -  Defaults to `false`.
* `node['yum']['fasttrack']['managed']` -  Defaults to `false`.
* `node['yum']['base']['exclude']` -  Defaults to ``.
* `node['yum']['main']['keepcache']` -  Defaults to `true`.
* `node['blueprint']['yum']['local']['path']` - the path of the local rpm repository. Defaults to `/var/tmp/rpm-repo`.
* `node['blueprint']['yum']['local']['archive']` - the URL from which to retrieve a repository archive, optional. Defaults to ``.
* `node['blueprint']['yum']['remote']['managed']` -  Defaults to `false`.
* `node['blueprint']['yum']['remote']['baseurl']` -  Defaults to ``.
* `node['blueprint']['yum']['remote']['enabled']` -  Defaults to `true`.
* `node['blueprint']['yum']['remote']['exclude']` -  Defaults to `nil`.
* `node['blueprint']['yum']['remote']['enablegroups']` -  Defaults to `nil`.
* `node['blueprint']['yum']['remote']['http_caching']` -  Defaults to `all`.
* `node['blueprint']['yum']['remote']['includepkgs']` -  Defaults to `nil`.
* `node['blueprint']['yum']['remote']['max_retries']` -  Defaults to `2`.
* `node['blueprint']['yum']['remote']['metadata_expire']` -  Defaults to `600`.
* `node['blueprint']['yum']['remote']['priority']` -  Defaults to `1`.
* `node['blueprint']['yum']['remote']['proxy']` -  Defaults to `nil`.
* `node['blueprint']['yum']['remote']['proxy_username']` -  Defaults to `nil`.
* `node['blueprint']['yum']['remote']['proxy_password']` -  Defaults to `nil`.
* `node['blueprint']['yum']['remote']['timeout']` -  Defaults to `30`.

# Recipes

* [blueprint-yum::centos](#blueprint-yumcentos) - This recipe configures yum repos for centos.
* [blueprint-yum::default](#blueprint-yumdefault) - This recipe includes all necessary recipes to set up the yum repo configuration.
* [blueprint-yum::local](#blueprint-yumlocal) - This recipe configures a local repository, i.e.
* [blueprint-yum::remote](#blueprint-yumremote) - This recipe configures a remote repository for blueprint RPMs, i.e.

## blueprint-yum::centos

This recipe configures yum repos for centos. This is a wrapper recipe for the official recipe from `yum-centos` cookbook.

## blueprint-yum::default

This recipe includes all necessary recipes to set up the yum repo configuration. To disable either mysql or postgres repos use the `managed` attributes.

## blueprint-yum::local

This recipe configures a local repository, i.e. for development purposes.

## blueprint-yum::remote

This recipe configures a remote repository for blueprint RPMs, i.e. a nexus or artifactory repo.

# Author

Author:: Your Name (<your_name@domain.com>)
