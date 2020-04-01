name 'blueprint-dev-tooling'
maintainer 'The Authors'
maintainer_email 'you@example.com'
license 'all_rights'
description 'Installs/Configures blueprint'
long_description 'Installs/Configures blueprint'
version '1.0.0'
chef_version '>= 12.5' if respond_to?(:chef_version)

depends 'blueprint-base'
depends 'blueprint-yum'
depends 'blueprint-mysql'
depends 'blueprint-postgresql'
depends 'blueprint-mongodb'
depends 'blueprint-tools'

depends 'chef-sugar', '~> 5.0.4'
