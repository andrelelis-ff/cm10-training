yum_repository "mysql#{node['blueprint']['mysql']['version']}-community" do
  baseurl node['blueprint']['mysql']['baseurl']
  description "MySQL #{node['blueprint']['mysql']['version']} Community Server"
  enabled true
  gpgcheck true
  gpgkey node['blueprint']['mysql']['gpgkey']
  make_cache true
  failovermethod node['blueprint']['mysql']['failovermethod'] unless node['blueprint']['mysql']['failovermethod'].nil?
  mirrorlist node['blueprint']['mysql']['mirrorlist'] unless node['blueprint']['mysql']['mirrorlist'].nil?
end
