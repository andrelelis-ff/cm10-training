=begin
#<
This definition installs CoreMedia Blueprint tools.

@param path The path to install the tool. Defaults to `/opt/<name of definition>`
@param group_id The maven groupId of the tools artifact.
@param artifact_id The maven artifactId of the tools artifact.
@param version The maven version of the tools artifact.
@param checksum The SHA-256 checksum of the tools artifact.
@param java_home The JAVA_HOME to use for the tool.
@param jvm_args Extra JVM args to set for tool executions.
@param sensitive Set to true to disable template logging.
@param property_files A hash where the name of a property file below `properties/corem` can map to a hash of key value pairs representing the properties of that file. If present the file will be rendered with the hashes content.
@param user The user to install the tool for.
@param group The group to install the tool for.
@param log_dir The directory a symlink for the logs should be created for.
@param nexus_url The nexus base url if you want to retrieve artifacts using the rest API of nexus. This allows versions like `LATEST`, `RELEASE` and `x-SNAPSHOT` to be resolved correctly. (optional)
@param nexus_repo The nexus repo name. Use the part of the url not the numeric id. (optional defaults to `releases`)

@section Examples

```ruby
blueprint_tool 'content-management-server-tools' do
  group_id  'com.coremedia.blueprint'
  artifact_id 'content-management-server-tools'
  version '7.5-SNAPSHOT'
  user 'coremedia'
  group 'coremedia'
  path "/opt/coremedia/content-management-server-tools"
  property_files ('capclient.properties' => { 'repository.url' => 'http://localhost/40080/ior' })
end
```

#>
=end

define :blueprint_tool, sensitive: true do
  tool_name = params[:name]
  params[:path] ||= node['blueprint']['tools'][tool_name]['dir']
  params[:user] ||= node['blueprint']['user']
  params[:group] ||= node['blueprint']['group']
  params[:property_files]
  params[:log_dir] ||= "#{node['blueprint']['log_dir']}/#{tool_name}-tools"
  params[:group_id] ||= node['blueprint']['tools'][tool_name]['group_id']
  params[:artifact_id] ||= node['blueprint']['tools'][tool_name]['artifact_id']
  params[:version] ||= node['blueprint']['tools'][tool_name]['version']
  params[:checksum] ||= node['blueprint']['tools'][tool_name]['checksum']
  params[:maven_repository_url] ||= node['blueprint']['maven_repository_url']
  params[:java_home] ||= node['blueprint']['tools']['java_home']
  params[:java_home] ||= '$JAVA_HOME'
  params[:property_files] ||= node['blueprint']['tools'][tool_name]['property_files']
  params[:property_files] ||= {}
  params[:nexus_url] ||= node['blueprint']['nexus_url'] if node['blueprint']['nexus_url']
  params[:nexus_repo] ||= node['blueprint']['nexus_repo'] if node['blueprint']['nexus_repo']
  tools_zip = "/var/tmp/#{tool_name}-#{params[:artifact_id]}-#{params[:version]}.zip"

  raise("Argument group_id missing on blueprint_tool[#{tool_name}]") unless params[:group_id]
  raise("Argument artifact_id missing on blueprint_tool[#{tool_name}]") unless params[:artifact_id]
  raise("Argument version missing on blueprint_tool[#{tool_name}]") unless params[:version]

  jvm_args = Mash.new
  jvm_args = Chef::Mixin::DeepMerge.hash_only_merge!(jvm_args, node['blueprint']['tools']['jvm_args']) if node.deep_fetch('blueprint', 'tools', 'jvm_args')
  jvm_args = Chef::Mixin::DeepMerge.hash_only_merge!(jvm_args, node['blueprint']['tools'][tool_name]['jvm_args']) if node.deep_fetch('blueprint', 'tools', tool_name, 'jvm_args')
  params[:jvm_args] ||= jvm_args.values

  # default config
  # noinspection RubyStringKeysInHashInspection
  default_logback_config = { 'properties' => { 'application.name' => "#{tool_name}-tools",
                                               'application.version' => node['blueprint']['tools'][tool_name]['version'],
                                               'log.dir' => params[:log_dir] } }
  logback_config_hash = Mash.new
  logback_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(logback_config_hash, node['blueprint']['tools']['logback_config']) if node.deep_fetch('blueprint', 'tools', 'logback_config')
  logback_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(logback_config_hash, node['blueprint']['tools'][tool_name]['logback_config']) if node.deep_fetch('blueprint', 'tools', tool_name, 'logback_config')
  logback_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(logback_config_hash, default_logback_config) unless logback_config_hash.empty?

  directory params[:path] do
    owner params[:user]
    group params[:group]
    mode 0775
  end

  directory "#{params[:path]}/var" do
    owner params[:user]
    group params[:group]
    mode 0775
  end

  directory "#{params[:path]}/var/logs" do
    path "#{params[:path]}/var/logs"
    owner params[:user]
    group params[:group]
    mode 0775
  end

  directory params[:log_dir] do
    recursive true
    owner params[:user]
    group params[:group]
    mode 0775
  end

  coremedia_maven tools_zip do
    extract_to params[:path]
    extract_force_clean true
    group_id params[:group_id]
    artifact_id params[:artifact_id]
    version params[:version]
    packaging 'zip'
    checksum params[:checksum]
    repository_url params[:maven_repository_url]
    nexus_url params[:nexus_url] if params[:nexus_url]
    nexus_repo params[:nexus_repo] if params[:nexus_repo]
    owner params[:user]
    group params[:group]
  end

  %w(cm common.sh).each do |executable_file|
    file "#{params[:path]}/bin/#{executable_file}" do
      owner params[:user]
      group params[:group]
      mode 0775
      only_if { File.exist?("#{params[:path]}/bin/#{executable_file}") }
    end
  end

  unless params[:property_files].empty?
    directory "#{params[:path]}/properties" do
      owner params[:user]
      group params[:group]
    end

    directory "#{params[:path]}/properties/corem" do
      owner params[:user]
      group params[:group]
    end

    params[:property_files].keys.select { |key| key =~ /.*\.properties/ }.each do |properties_file|
      template "#{params[:path]}/properties/corem/#{properties_file}" do
        source 'properties.erb'
        cookbook 'blueprint-tools'
        owner params[:user]
        group params[:group]
        variables(props: params[:property_files][properties_file])
        sensitive params[:sensitive]
      end
    end
  end

  template "#{params[:path]}/bin/pre-config.jpif" do
    source 'pre-config.jpif.erb'
    cookbook 'blueprint-tools'
    variables(java_home: params[:java_home],
              jvm_args: params[:jvm_args])
    owner params[:user]
    group params[:group]
  end

  template "#{tool_name}-tools-logging-config" do
    path "#{params[:path]}/properties/corem/tools-logback.xml"
    cookbook 'blueprint-tools'
    owner params[:user]
    group params[:group]
    source 'logback.xml.erb'
    variables config: logback_config_hash
    not_if { logback_config_hash.empty? }
  end
end
