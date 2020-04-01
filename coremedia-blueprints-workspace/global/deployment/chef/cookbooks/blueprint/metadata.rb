name 'blueprint'
maintainer 'The Authors'
maintainer_email 'you@example.com'
license 'all_rights'
description 'Sets blueprint environment variables'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '1.0.0'
chef_version '>= 12.5' if respond_to?(:chef_version)
