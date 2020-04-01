default['yum']['pgdg']['version'] = '9.3'
version = node['yum']['pgdg']['version']

default['yum']['pgdg']['repositoryid'] = "pgdg-#{version}"
default['yum']['pgdg']['description'] = "PostgreSQL #{version}"
default['yum']['pgdg']['gpgkey'] = 'http://yum.postgresql.org/RPM-GPG-KEY-PGDG'
default['yum']['pgdg']['gpgcheck'] = true
default['yum']['pgdg']['enabled'] = true

case node['platform_family']
when 'rhel'
  default['yum']['pgdg']['baseurl'] = "http://yum.pgrpms.org/#{version}/redhat/rhel-$releasever-$basearch"
when 'fedora'
  default['yum']['pgdg']['baseurl'] = "http://yum.pgrpms.org/#{version}/fedora/fedora-$releasever-$basearch"
end
