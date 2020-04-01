name 'blueprint-tools'
maintainer 'The Authors'
maintainer_email 'you@example.com'
license 'all_rights'
description 'Installs/Configures blueprint tools'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '1.0.0'
chef_version '>= 12.5' if respond_to?(:chef_version)

depends 'blueprint-base'
depends 'coremedia_maven', '~> 3.0.1'
