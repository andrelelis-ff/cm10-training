name 'blueprint-mysql'
maintainer 'The Authors'
maintainer_email 'you@example.com'
license 'all_rights'
description 'Installs/Configures blueprint-mysql'
long_description 'Installs/Configures blueprint-mysql'
version '1.0.0'
chef_version '>= 12.5' if respond_to?(:chef_version)

depends 'mysql', '~> 8.5.1'
depends 'blueprint-base'
