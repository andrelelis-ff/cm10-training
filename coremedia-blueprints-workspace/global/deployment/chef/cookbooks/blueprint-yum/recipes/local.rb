=begin
#<
This recipe configures a local repository, i.e. for development purposes.
#>
=end
local_repository = node['blueprint']['yum']['local']['path']
local_repository_archive_url = node['blueprint']['yum']['local']['archive']

unless local_repository_archive_url.empty?
  package 'unzip' do
    retries 3
  end
  if local_repository_archive_url =~ /s3-.*/
    credentials = Chef::EncryptedDataBagItem.load('credentials', 'aws')
    s3_file "#{Chef::Config[:file_cache_path]}/coremedia-rpm-repository.zip" do
      source local_repository_archive_url
      access_key_id credentials['access_key']
      secret_access_key credentials['secret_key']
    end
  end

  remote_file "#{Chef::Config[:file_cache_path]}/coremedia-rpm-repository.zip" do
    source local_repository_archive_url
    backup false
    only_if { local_repository_archive_url =~ /(file|http(s)?):/ }
  end

  directory local_repository do
    recursive true
  end

  execute "unzip -qq -uo -j #{Chef::Config[:file_cache_path]}/coremedia-rpm-repository.zip -d #{local_repository}"
end

package 'createrepo' do
  only_if { ::File.exist?(local_repository) }
end

create_repo_res = execute "createrepo --update --skip-stat #{local_repository}" do
  only_if { ::File.exist?(local_repository) }
  not_if "test -d #{local_repository}/repodata/ && test -z $(find #{local_repository} -name '*.rpm' -newer #{local_repository}/repodata/)"
end

yum_repository 'repo_local' do
  description 'Local Blueprint Repo'
  repositoryid 'local'
  baseurl "file://#{local_repository}"
  gpgcheck false
  skip_if_unavailable true
  # expire immediately as this is for development only
  metadata_expire '30'
  http_caching 'none'
  action :create
  only_if { ::File.exist?(local_repository) }
  if ::File.exist?("#{local_repository}/repodata")
    subscribes :makecache, create_repo_res, :immediately
  end
end
