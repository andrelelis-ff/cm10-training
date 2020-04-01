# Description

This is the wrapper cookbook to install mongodb3.

# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* ulimit (~> 1.0.0)
* mongodb3 (~> 5.3.0)

# Attributes

* `node['ulimit']` - define ulimits for mongod. Defaults to `{ ... }`.
* `node['mongodb3']['mongod']['disable-transparent-hugepages']` - Disable Transparent Huge Pages (THP). Defaults to `true`.
* `node['mongodb3']['version']` - fixed version. Defaults to `4.0.13`.
* `node['mongodb3']['config']['mongod']['net']['bindIp']` - fixed configuration. Defaults to `0.0.0.0`.
* `node['mongodb3']['config']['mongod']['security']['authorization']` -  Defaults to `nil`.
* `node['mongodb3']['config']['mongod']['storage']['mmapv1']['smallFiles']` -  Defaults to `nil`.
* `node['mongodb3']['config']['mongod']['security']['authorization']` -  Defaults to `enabled`.

# Recipes

* blueprint-mongodb::default
* blueprint-mongodb::disable-thp

# Author

Author:: Bodo Schulz (<bodo.schulz@coremedia.com>)

