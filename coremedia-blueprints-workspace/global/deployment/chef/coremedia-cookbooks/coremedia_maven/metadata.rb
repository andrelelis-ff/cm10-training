name 'coremedia_maven'
maintainer 'Felix Simmendinger'
maintainer_email 'felix.simmendinger@coremedia.com'
license 'Copyright (C) 2014-2017, CoreMedia AG proprietary License, all rights reserved.'
description 'Installs a maven artifact from a given maven repository to a specified location'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '3.0.2'

#source_url 'https://github.com/coremedia-contributions/coremedia-maven-cookbook'
#issues_url 'https://support.coremedia.com'
chef_version '>= 12.5' if respond_to?(:chef_version)

%w(redhat centos ubuntu debian windows).each do |os|
  supports os
end
