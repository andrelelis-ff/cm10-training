name 'blueprint-base'
maintainer 'The Authors'
maintainer_email 'you@example.com'
license 'all_rights'
description 'Installs/Configures blueprint-base'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '1.0.0'
chef_version '>= 12.5' if respond_to?(:chef_version)

depends 'ulimit', '~> 1.0.0'
# to automatically install java_se extract the `java_se.tgz` downloaded from https://supermarket.chef.io/cookbooks/java_se
# to the `thirdparty-cookbooks` directory and add `java_se::default` at the beginning of your runlist.
suggests 'java_se', '~> 8.0'
