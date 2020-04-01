=begin
#<

Downloads a Maven artifact from a local or remote Maven repository.

If attribute `nexus_url` and `nexus_repo` is set, the artifact will be retrieved using the rest API of nexus and the version
of the artifact may be set to `RELEASE`, `LATEST` or `X-SNAPSHOT` and the versions will be resolved accordingly.

@action install Install an artifact

@section Examples

```ruby
coremedia_maven '/var/tmp/solr.war' do
  group_id 'org.apache.solr'
  artifact_id 'solr'
  version '4.5.0'
  owner 'coremedia'
  group 'coremedia'
  mode 0644
  extract_to '/usr/local/tomcat/webapps/solr'
  notify :restart, 'tomcat[solr]', :immediately
end
```
#>
=end

require 'fileutils'
require 'chef/digester'
require 'digest'
require 'uri'
require 'json'

resource_name :coremedia_maven
actions :install
default_action :install

#<> @attribute path The path to download the artifact to. Defaults to the name of the resource.
property :path, kind_of: String, name_property: true
#<> @attribute group_id Maven groupId.
property :group_id, kind_of: String, required: true
#<> @attribute artifact_id Maven artifactId.
property :artifact_id, kind_of: String, required: true
#<> @attribute version Maven version.
property :version, kind_of: String, required: true
#<> @attribute packaging Maven packaging type, defaults to the file extension of the path attribute.
property :packaging, kind_of: String, default: lazy { |r| ::File.extname(r.path)[1..-1] }
#<> @attribute classifier Maven classifier, defaults to not set.
property :classifier, kind_of: String, default: nil
#<> @attribute checksum The SHA-256 checksum of the artifact
property :checksum, kind_of: String, default: nil
#<> @attribute repository_url The Url of the maven repository, supports all protocols of the remote_file resource. Defaults to Maven Central.
property :repository_url, kind_of: String, default: 'http://repo1.maven.org/maven2/',  callbacks: {
        'should be a valid URL' => lambda {
                |url| url =~ URI::regexp
        }
}
#<> @attribute username The username for the Maven repository, defaults to not set.
property :username, kind_of: String, default: nil
#<> @attribute password The password for the Maven repository, defaults to not set.
property :password, kind_of: String, default: nil
#<> @attribute owner User ownership (linux only), defaults to "root".
property :owner, kind_of: String, default: 'root'
#<> @attribute group Group membership (linux only), defaults to "root".
property :group, kind_of: String, default: 'root'
#<> @attribute mode File permissions, defaults to "00755".
property :mode, kind_of: [String, Integer], default: 00755
#<> @attribute extract_to Extract to folder path if downloaded artifact has changed. Extraction is skipped if no folder path is set.
property :extract_to, kind_of: String, default: nil
#<> @attribute extract_force_clean Set this to true to force deletion of target folder upfront.
property :extract_force_clean, kind_of: [TrueClass, FalseClass], default: false
#<> @attribute backup Number of version to backup in the Chef cache. This works only with non file:// urls. Set to false to disable backup.
property :backup, kind_of: [Integer, FalseClass], default: false
#<> @attribute nexus_url Set this to the base url of your nexus to use the REST API for artifact resolution. If set this method has precedence to the repository_url attribute.
property :nexus_url, kind_of: String, callbacks: {
        'should be a valid URL' => lambda {
                |url| url =~ URI::regexp
        }
}
#<> @attribute nexus_repo The repo name from which to resolve artifacts.
property :nexus_repo, kind_of: String, default: 'releases'


action :install do
  suffix = (new_resource.classifier.nil? || new_resource.classifier.empty?) ? ".#{new_resource.packaging}" : "-#{new_resource.classifier}.#{new_resource.packaging}"
  gav_path = "#{new_resource.group_id.tr('.', '/')}/#{new_resource.artifact_id}/#{new_resource.version}/#{new_resource.artifact_id}-#{new_resource.version}#{suffix}"
  repository_url = new_resource.repository_url.end_with?('/') ? new_resource.repository_url : "#{new_resource.repository_url}/"
  artifact_source_uri = URI("#{repository_url}#{gav_path}")
  unless new_resource.nexus_url.nil?
    nexus_url = new_resource.nexus_url.end_with?('/') ?  new_resource.nexus_url : "#{new_resource.nexus_url}/"
    artifact_source_uri = URI("#{nexus_url}service/local/artifact/maven/redirect?g=#{new_resource.group_id}&a=#{new_resource.artifact_id}&v=#{new_resource.version}&p=#{new_resource.packaging}&c=#{new_resource.classifier}&r=#{new_resource.nexus_repo}")
  end

  # hashing for update detection
  sha256 = ::Digest::SHA256.new

  target_file_hash = "#{Chef::Config[:file_cache_path]}/#{sha256.hexdigest(new_resource.path)}"
  gav_hash = sha256.hexdigest(gav_path)
  old_gav_hash = ''
  if ::File.exist?(target_file_hash)
    old_gav_hash = ::File.open(target_file_hash, 'r').read
  end

  directory "parent dir of #{new_resource.name}" do
    path ::File.dirname(new_resource.path)
    recursive true
    group new_resource.group
    owner new_resource.owner
    not_if { ::File.exist?(::File.dirname(new_resource.path)) }
  end

  filemode_res = file "set filemode for #{new_resource.path}" do
    action :nothing
    path new_resource.path
    owner new_resource.owner
    group new_resource.group
    mode new_resource.mode
  end

  if new_resource.extract_to
    extract_dir = directory "#{new_resource.name}-exploded" do
      path new_resource.extract_to
      recursive true
      action :nothing
    end
  end

  if platform_family?('windows')
    extract_res = powershell_script "unzip #{new_resource.name} to #{new_resource.extract_to}" do
      code <<-EOH
      Add-Type -AssemblyName System.IO.Compression.FileSystem
      [System.IO.Compression.ZipFile]::ExtractToDirectory(#{new_resource.path}, #{new_resource.extract_to})
      EOH
      action :nothing
    end
  else
    package 'unzip'
    extract_res = execute "unzip_#{new_resource.name}_to_#{new_resource.extract_to}" do
      command "unzip -uo -qq #{new_resource.path} -d #{new_resource.extract_to}"
      user new_resource.owner
      group new_resource.group
      action :nothing
    end
  end

  if artifact_source_uri.scheme == 'file'
    ruby_block "copy local source to #{new_resource.name}" do
      block do
        ::FileUtils.copy_file(artifact_source_uri.path, new_resource.path, true, true)
      end
      not_if { artifact_uptodate?(artifact_source_uri.path, new_resource.path) }
      notifies :create, filemode_res, :immediately
      notifies :delete, extract_dir, :immediately if new_resource.extract_to && new_resource.extract_force_clean
      notifies :run, extract_res, :immediately if new_resource.extract_to
    end
  else
    remote_res = remote_file new_resource.name do
      backup new_resource.backup
      path new_resource.path
      source artifact_source_uri.to_s
      retries 3
      retry_delay 3
      unless new_resource.username.nil? || new_resource.password.nil?
        headers('AUTHORIZATION' => "Basic #{Base64.encode64("#{new_resource.username}:#{new_resource.password}")}")
      end
      action :nothing
      checksum new_resource.checksum if new_resource.checksum
    end
    http_request "Checking modified header for remote_file[#{new_resource.name}]" do
      message ''
      url artifact_source_uri.to_s
      retries 3
      retry_delay 3
      action :head
      if ::File.exist?(new_resource.path) && (old_gav_hash == gav_hash)
        headers 'If-Modified-Since' => ::File.mtime(new_resource.path).httpdate
      end
      notifies :create, remote_res, :immediately
      notifies :create, filemode_res, :immediately
      notifies :delete, extract_dir, :immediately if new_resource.extract_to && new_resource.extract_force_clean
      notifies :run, extract_res, :immediately if new_resource.extract_to
    end
  end

  ::File.open(target_file_hash, 'w+') do |metadata|
    metadata.write(gav_hash)
  end
end

# checks if the artifact needs to be copied from cached path to artifact path
def artifact_uptodate?(source, target)
  (::File.exist?(target) && ::File.exist?(source)) && Chef::Digester.checksum_for_file(source) == Chef::Digester.checksum_for_file(target)
end
