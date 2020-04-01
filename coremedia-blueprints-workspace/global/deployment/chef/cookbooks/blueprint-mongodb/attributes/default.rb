#<> define ulimits for mongod
default['ulimit'] = {
  users: {
    mongod: {
      filehandle_limit: 655_36,
      process_limit: 655_36
    }
  }
}

#<> Disable Transparent Huge Pages (THP)
default['mongodb3']['mongod']['disable-transparent-hugepages'] = true

#<> fixed version
default['mongodb3']['version'] = '4.0.13'

#<> fixed configuration
default['mongodb3']['config']['mongod']['net']['bindIp'] = '0.0.0.0'
default['mongodb3']['config']['mongod']['security']['authorization'] = nil
default['mongodb3']['config']['mongod']['storage']['mmapv1']['smallFiles'] = nil
# default['mongodb3']['config']['mongod']['storage']['journal']['enabled'] =  true
#

default['mongodb3']['config']['mongod']['security']['authorization'] = 'enabled'
