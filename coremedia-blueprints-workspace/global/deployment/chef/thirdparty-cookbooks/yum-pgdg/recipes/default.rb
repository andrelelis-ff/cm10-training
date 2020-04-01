#
# Author:: Sean OMeara (<someara@chef.io>)
# Recipe:: default
# Cookbook:: yum-pgdg
#
# Copyright:: 2013-2017, Chef Software, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

yum_repository 'pgdg' do
  description node['yum']['pgdg']['description'] unless node['yum']['pgdg']['description'].nil?
  baseurl node['yum']['pgdg']['baseurl'] unless node['yum']['pgdg']['baseurl'].nil?
  mirrorlist node['yum']['pgdg']['mirrorlist'] unless node['yum']['pgdg']['mirrorlist'].nil?
  gpgcheck node['yum']['pgdg']['gpgcheck'] unless node['yum']['pgdg']['gpgcheck'].nil?
  gpgkey node['yum']['pgdg']['gpgkey'] unless node['yum']['pgdg']['gpgkey'].nil?
  enabled node['yum']['pgdg']['enabled'] unless node['yum']['pgdg']['enabled'].nil?
  cost node['yum']['pgdg']['cost'] unless node['yum']['pgdg']['cost'].nil?
  exclude node['yum']['pgdg']['exclude'] unless node['yum']['pgdg']['exclude'].nil?
  enablegroups node['yum']['pgdg']['enablegroups'] unless node['yum']['pgdg']['enablegroups'].nil?
  failovermethod node['yum']['pgdg']['failovermethod'] unless node['yum']['pgdg']['failovermethod'].nil?
  http_caching node['yum']['pgdg']['http_caching'] unless node['yum']['pgdg']['http_caching'].nil?
  include_config node['yum']['pgdg']['include_config'] unless node['yum']['pgdg']['include_config'].nil?
  includepkgs node['yum']['pgdg']['includepkgs'] unless node['yum']['pgdg']['includepkgs'].nil?
  keepalive node['yum']['pgdg']['keepalive'] unless node['yum']['pgdg']['keepalive'].nil?
  max_retries node['yum']['pgdg']['max_retries'] unless node['yum']['pgdg']['max_retries'].nil?
  metadata_expire node['yum']['pgdg']['metadata_expire'] unless node['yum']['pgdg']['metadata_expire'].nil?
  mirror_expire node['yum']['pgdg']['mirror_expire'] unless node['yum']['pgdg']['mirror_expire'].nil?
  priority node['yum']['pgdg']['priority'] unless node['yum']['pgdg']['priority'].nil?
  proxy node['yum']['pgdg']['proxy'] unless node['yum']['pgdg']['proxy'].nil?
  proxy_username node['yum']['pgdg']['proxy_username'] unless node['yum']['pgdg']['proxy_username'].nil?
  proxy_password node['yum']['pgdg']['proxy_password'] unless node['yum']['pgdg']['proxy_password'].nil?
  repositoryid node['yum']['pgdg']['repositoryid'] unless node['yum']['pgdg']['repositoryid'].nil?
  sslcacert node['yum']['pgdg']['sslcacert'] unless node['yum']['pgdg']['sslcacert'].nil?
  sslclientcert node['yum']['pgdg']['sslclientcert'] unless node['yum']['pgdg']['sslclientcert'].nil?
  sslclientkey node['yum']['pgdg']['sslclientkey'] unless node['yum']['pgdg']['sslclientkey'].nil?
  sslverify node['yum']['pgdg']['sslverify'] unless node['yum']['pgdg']['sslverify'].nil?
  timeout node['yum']['pgdg']['timeout'] unless node['yum']['pgdg']['timeout'].nil?
  action :create
end
