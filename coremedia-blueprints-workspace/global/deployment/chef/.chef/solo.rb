log_level         :info
log_location      STDOUT
verbose_logging   true
cookbook_path     [File.join(File.dirname(__FILE__), '../cookbooks'),
                  File.join(File.dirname(__FILE__), '../coremedia-cookbooks'),
                  File.join(File.dirname(__FILE__), '../thirdparty-cookbooks')]
role_path         File.join(File.dirname(__FILE__), '../roles')
environment_path  File.join(File.dirname(__FILE__), '../environments')
lockfile          "/var/chef/cache/chef-solo-running.pid"
environment       'development'
json_attribs      File.join(File.dirname(__FILE__), '../nodes/single.json')
