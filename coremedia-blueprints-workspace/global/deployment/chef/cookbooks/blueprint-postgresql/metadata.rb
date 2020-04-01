name 'blueprint-postgresql'
maintainer 'The Authors'
maintainer_email 'you@example.com'
license 'all_rights'
description 'Installs/Configures blueprint-postgresql'
long_description 'Installs/Configures blueprint-postgresql'
version '1.0.0'
chef_version '>= 12.5' if respond_to?(:chef_version)

depends 'blueprint-base'
depends 'blueprint-yum'
depends 'yum-pgdg', '~> 3.0.0'
depends 'postgresql', '~> 7.1.4'
