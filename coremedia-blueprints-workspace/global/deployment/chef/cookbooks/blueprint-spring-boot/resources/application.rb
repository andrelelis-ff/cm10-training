=begin
#<
This resource manages the installation of a Spring-Boot application jar.

@action install Downloads the application jar and renders application.properties file and SystemD init file.
@action uninstalls Stops the service (must be named like the resource) and removes all files.

@section File System layour

This resource expects a content_dir directory to have the following layout

```
|- content
|     |- some-content.xml
|     `- some-blob.jpg
`- users
     `- some-users.xml
```

The content below `content` may be structured arbitrary but must be a valid serverexport dump. User files should be placed
directly below the `users` directory.

@section Guards

All actions are guarded so content is only imported once. The guards are achieved by using marker files.

* If content has already been imported, a marker file `CONTENT_IMPORTED` is placed at the root of the content dir.
* If content has been published, a marker file `CONTENT_PUBLISHED` is placed at the root of the content dir.
* If builtin workflows have been uploaded, a marker file `BUILTIN_WORKFLOWS_UPLOADED` is placed at the root of the content dir. If this resource is used multiple times, this may lead to multiple imports so take care using the upload actions.
* If custom workflows have been uploaded, a marker file `<workflow-defintion file>_WORKFLOW_UPLOADED` is being placed aside the workflow defintion file.
* If a users file has been imported, a marker file `<users file>_USERS_IMPORTED` is being placed beside the users file.

@section Examples

```ruby
blueprint_dev_tooling_content '/some/path/to/a/content/dir' do
 builtin_workflows ['two-step-publication.xml']
 custom_workflows ['/some/path/to/a/workflow/definition.xml']
 action [:import_content, :import_user]
end
```

#>
=end

resource_name :spring_boot_application
actions :install
default_action :install

require 'uri'

#<> @attribute path the installation dir
property :path, kind_of: String, required: true
#<> @attribute group_id the maven groupId of the service artifact
property :group_id, kind_of: String, required: true
#<> @attribute artifact_id the maven artifactId of the service artifact
property :artifact_id, kind_of: String, required: true
#<> @attribute version the maven version of the service artifact
property :version, kind_of: String, required: true
#<> @attribute classifier the maven classifier of the service artifact
property :classifier, kind_of: String
#<> @attribute checksum the checksum of the maven artifact
property :checksum, kind_of: String
#<> @attribute packaging the maven packaging type of the service artifact
property :packaging, kind_of: String, default: 'jar', equal_to: ['jar', 'war']
#<> @attribute maven_repository_url the url of the maven repository to retrieve the service artifact
property :maven_repository_url, kind_of: String, callbacks: {
        'should be a valid URL' => lambda {
                |url| url =~ URI::regexp
        }
}
#<> @attribute nexus_url the url of the nexus repository. This property is optional and only required if special versions
# like `<VERSION>-SNAPSHOT`, `RELEASE` or `LATEST` are being used.
# used.
property :nexus_url, kind_of: String, callbacks: {
        'should be a valid URL' => lambda {
                |url| url =~ URI::regexp
        }
}
#<> @attribute nexus_repo the id of the nexus repo to use. This property is optional and only mandatory if nexus_url is set.
property :nexus_repo, kind_of: String
#<> @attribute username the username to retrieve the artifact if authentication is required
property :username, kind_of: String
#<> @attribute username the password to retrieve the artifact if authentication is required
property :password, kind_of: String
#<> @attribute group the group of the service user
property :group, kind_of: String
#<> @attribute owner the service user
property :owner, kind_of: String
#<> @attribute service_description the description of the service used in Systemd/SystemV service definition
property :service_description, kind_of: String
#<> @attribute service_timeout the timeout for the service to start
property :service_timeout, kind_of: Integer, default: 300
#<> @attribute java_opts JAVA_OPTS for the service
property :java_opts, kind_of: String, default: ''
#<> @attribute java_home the Jave installation dir
property :java_home, kind_of: String, default: '/usr/bin/java'
#<> @attribute boot_opts a hash of key/values to put on the command-line. Will be transformed to --<key>=<value>
property :boot_opts, kind_of: Hash, default: {}
#<> @attribute application_properties a hash of properties rendered to the application.properties file
property :application_properties, kind_of: Hash, default: {}
#<> @attribute clean_log_dir_on_start set to true to delete the logs on restart
property :clean_log_dir_on_start, kind_of: [TrueClass,FalseClass], default: false
#<> @attribute log_dir defaults to service_dir/log, if set to something different make sure the directory exists and is writable by the service user
property :log_dir, kind_of: String, default: lazy { |r| "#{r.path}/log" }
#<> @attribute post_start_cmds an array of scripts to execute after the service starts
property :post_start_cmds, kind_of: Array, default: []
#<> @attribute pre_start_cmds an array of scripts to execute before the service starts
property :pre_start_cmds, kind_of: Array, default: []
#<> @attribute post_start_wait_url an url to wait for after the service starts.
property :post_start_wait_url, kind_of: String, callbacks: {
        'should be a valid URL' => lambda {
                |url| url =~ URI::regexp
        }
}
#<> @attribute post_start_wait_code the HTTP return code to wait for
property :post_start_wait_code, kind_of: Integer, default: 200
#<> @attribute post_start_wait_timeout the timeout to wait for the url check to succeed
property :post_start_wait_timeout, kind_of: Integer, default: 600

#<> @attribute jmx_remote Flag to enable or disable remote jmx.
property :jmx_remote, kind_of: [FalseClass, TrueClass], default: false
#<> @attribute jmx_remote_server_name The host name or ip that resolves to this node. If set to nil, node[:fqdn] will be used.
property :jmx_remote_server_name, kind_of: String, default: lazy { node['fqdn'] }
#<> @attribute jmx_remote_monitor_user The user for readonly access.
property :jmx_remote_monitor_user, kind_of: String, default: 'monitor'
#<> @attribute jmx_remote_monitor_password The password for readonly access.
property :jmx_remote_monitor_password, kind_of: String, default: 'monitor'
#<> @attribute jmx_remote_control_user The user for readwrite access.
property :jmx_remote_control_user, kind_of: String, default: 'control'
#<> @attribute jmx_remote_control_password The password for readwrite access.
property :jmx_remote_control_password, kind_of: String, default: 'control'
#<> @attribute jmx_remote_registry_port The port of the JMX registry.
property :jmx_remote_registry_port, kind_of: Integer, default: 8099
#<> @attribute jmx_remote_server_port The port of the JMX server
property :jmx_remote_server_port, kind_of: Integer, default: 8098
#<> @attribute jmx_remote_authenticate A flag to disable jmx authentication.
property :jmx_remote_authenticate, kind_of: [FalseClass, TrueClass], default: false

action :install do

  post_start_cmds = Array.new
  post_start_cmds += new_resource.post_start_cmds
  pre_start_cmds = Array.new
  pre_start_cmds += new_resource.pre_start_cmds
  pre_start_cmds << "rm -f #{new_resource.path.log_dir}" if new_resource.clean_log_dir_on_start

  directory new_resource.path do
    owner new_resource.owner
    group new_resource.group
    recursive true
    mode '0700'
  end

  directory new_resource.log_dir do
    owner new_resource.owner
    group new_resource.group
    mode '0700'
    not_if { ::File.exist?(new_resource.log_dir) }
  end

  coremedia_maven "#{new_resource.path}/#{new_resource.name}.jar" do
    group_id new_resource.group_id
    artifact_id new_resource.artifact_id
    version new_resource.version
    classifier new_resource.classifier unless new_resource.classifier.nil?
    packaging new_resource.packaging
    checksum new_resource.checksum unless new_resource.checksum.nil?
    repository_url new_resource.maven_repository_url
    nexus_url new_resource.nexus_url unless new_resource.nexus_url.nil?
    nexus_repo new_resource.nexus_repo unless new_resource.nexus_repo.nil?
    group new_resource.group
    owner new_resource.owner
    backup 1
  end

  app_properties = Mash.new
  Chef::Mixin::DeepMerge.hash_only_merge!(app_properties, new_resource.application_properties)

  template "#{new_resource.path}/application.properties" do
    source 'properties.erb'
    cookbook 'blueprint-spring-boot'
    variables props: app_properties
    owner new_resource.owner
    group new_resource.group
    sensitive true
  end

  boot_opts_cmd_line = ''
  unless new_resource.boot_opts.empty?
    new_resource.boot_opts.keys.sort.each do | boot_opt_key|
      boot_opts_cmd_line += "--#{boot_opt_key}=#{new_resource.boot_opts[boot_opt_key]} "
    end
  end

  java_opts_cmd_line = new_resource.java_opts
  java_opts_cmd_line << " -Dlogging.file.name=#{new_resource.log_dir}/#{new_resource.name}.log"

  if new_resource.jmx_remote
    java_opts_cmd_line << " -Djava.rmi.server.hostname=#{new_resource.jmx_remote_server_name}"
    java_opts_cmd_line << " -Dcom.sun.management.jmxremote"
    # to enable jmx over ssl, there are many more configuration options to do. For secured read/write access
    # evaluate using spring boots jmx actuator or jolokia.
    java_opts_cmd_line << " -Dcom.sun.management.jmxremote.ssl=false"
    java_opts_cmd_line << " -Dcom.sun.management.jmxremote.port=#{new_resource.jmx_remote_registry_port}"
    java_opts_cmd_line << " -Dcom.sun.management.jmxremote.rmi.port=#{new_resource.jmx_remote_server_port}"
    java_opts_cmd_line << " -Dcom.sun.management.jmxremote.authenticate=#{new_resource.jmx_remote_authenticate}"
    java_opts_cmd_line << " -Dcom.sun.management.jmxremote.password.file=#{new_resource.path}/jmxremote.password"
    java_opts_cmd_line << " -Dcom.sun.management.jmxremote.access.file=#{new_resource.path}/jmxremote.access"

    template "#{new_resource.path}/jmxremote.access" do
      source 'jmxremote.access.erb'
      cookbook 'blueprint-spring-boot'
      owner new_resource.owner
      group new_resource.group
      mode 00500
      variables(:jmx_remote_monitor_user => new_resource.jmx_remote_monitor_user,
                :jmx_remote_control_user => new_resource.jmx_remote_control_user)
    end

    template "#{new_resource.path}/jmxremote.password" do
      source 'jmxremote.password.erb'
      cookbook 'blueprint-spring-boot'
      owner new_resource.owner
      group new_resource.group
      mode 00500
      sensitive true
      variables(:jmx_remote_monitor_user => new_resource.jmx_remote_monitor_user,
                :jmx_remote_control_user => new_resource.jmx_remote_control_user,
                :jmx_remote_monitor_password => new_resource.jmx_remote_monitor_password,
                :jmx_remote_control_password => new_resource.jmx_remote_control_password)
    end
  end

  if new_resource.post_start_wait_url
    check_script = template "#{new_resource.path}/post-start-check.sh" do
      source 'post-start-wait.sh.erb'
      owner new_resource.owner
      group new_resource.group
      mode 0700
      variables(
              wait_url: new_resource.post_start_wait_url,
              wait_code: new_resource.post_start_wait_code,
              wait_timeout: new_resource.post_start_wait_timeout,
              service_name: new_resource.name
      )
    end
    post_start_cmds << check_script.path
  end

  reload_service_r = execute "#{new_resource.name} systemctl daemon-reload" do
    command '/bin/systemctl daemon-reload'
    action :nothing
  end

  template "/etc/systemd/system/#{new_resource.name}.service" do
    source "bootapp.service_systemD.erb"
    mode '0664'
    owner 'root'
    group 'root'
    cookbook 'blueprint-spring-boot'
    variables(
            description: new_resource.service_description ||= "CoreMedia #{new_resource.name}",
            user: new_resource.owner,
            group: new_resource.group,
            jar_path: "#{new_resource.path}/#{new_resource.name}.jar",
            java_opts: java_opts_cmd_line,
            java_home: new_resource.java_home,
            boot_opts: boot_opts_cmd_line,
            pre_start_cmds: pre_start_cmds,
            post_start_cmds: post_start_cmds,
            service_timeout: new_resource.service_timeout,
            working_dir: new_resource.path
    )
    # reload the service unit file after changes from the template rendering
    # during the first run this will not do anything as the service is not yet registered with chef
    notifies :run, reload_service_r, :immediately
  end
end

action :uninstall do
  service new_resource.name do
    action [:stop]
    # ignore_failure true
  end
  directory new_resource.path do
    action [:delete]
    recursive true
    only_if { ::File.exist?(new_resource.path) }
  end
  file "/etc/systemd/system/#{new_resource.name}.service" do
    action [:delete]
    only_if { ::File.exist?("/etc/systemd/system/#{new_resource.name}.service") }
  end
end

