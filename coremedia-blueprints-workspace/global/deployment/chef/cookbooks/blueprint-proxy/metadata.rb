name 'blueprint-proxy'
maintainer 'Felix Simmendinger'
maintainer_email 'felix.simmendinger@coremedia.com'
license 'Copyright (C) 2015, CoreMedia AG proprietary License, all rights reserved.'
description 'Installs and configures a webserver proxy'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '1.0.0'
chef_version '>= 12.5' if respond_to?(:chef_version)

depends 'blueprint-base'
depends 'blueprint-spring-boot'
depends 'apache2', '~> 7.1.0'
