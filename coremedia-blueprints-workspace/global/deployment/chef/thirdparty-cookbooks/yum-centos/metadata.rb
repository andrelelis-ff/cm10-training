name 'yum-centos'
maintainer 'Chef Software, Inc.'
maintainer_email 'cookbooks@chef.io'
license 'Apache-2.0'
description 'Installs and configures the Centos Yum repositories'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '3.1.0'

supports 'centos'
supports 'xenserver'

source_url 'https://github.com/chef-cookbooks/yum-centos'
issues_url 'https://github.com/chef-cookbooks/yum-centos/issues'
chef_version '>= 12.14' if respond_to?(:chef_version)
