name 'blueprint-cmcc'
maintainer 'The Authors'
maintainer_email 'you@example.com'
license 'all_rights'
description 'Installs/Configures CoreMedia CMCC configs and site configs'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '1.0.0'
chef_version '>= 12.5' if respond_to?(:chef_version)

depends 'blueprint-base'
depends 'blueprint-proxy'
depends 'apache2', '~> 7.1.0'
