name 'blueprint-spring-boot'
maintainer 'The Authors'
maintainer_email 'you@example.com'
license 'All Rights Reserved'
description 'Installs/Configures blueprint-spring-boot'
long_description 'Installs/Configures blueprint-spring-boot'
version '0.1.0'
chef_version '>= 12.5' if respond_to?(:chef_version)

# The `issues_url` points to the location where issues for this cookbook are
# tracked.  A `View Issues` link will be displayed on this cookbook's page when
# uploaded to a Supermarket.
#
# issues_url 'https://github.com/<insert_org_here>/blueprint-spring-boot/issues'

# The `source_url` points to the development reposiory for this cookbook.  A
# `View Source` link will be displayed on this cookbook's page when uploaded to
# a Supermarket.
#
# source_url 'https://github.com/<insert_org_here>/blueprint-spring-boot'

depends 'blueprint-base'
depends 'coremedia_maven', '~> 3.0.1'
depends 'chef-sugar', '~> 5.0.4'
