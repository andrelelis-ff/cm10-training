name             'blueprint-solr'
maintainer       'CoreMedia AG'
maintainer_email 'daniel.zabel@coremedia.com'
license IO.read(File.join(File.dirname(__FILE__), 'LICENSE'))
description      'Installs the solr search engine.'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
source_url       ''
issues_url       ''
version          '1.0.0'

%w(redhat centos amazon).each do |os|
  supports os, '~> 7.0'
end
chef_version '>= 12.5' if respond_to?(:chef_version)

depends 'blueprint-base'
depends 'coremedia_maven', '~> 3.0.0'
depends 'ulimit', '~> 1.0.0'
