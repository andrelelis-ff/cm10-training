=begin
#<
This resource encapsulates some repository actions often used in test systems. Do not use this resource in production, it
is not a resource as chef intended to. It does not care about idempotency.

You should also be aware, that the publish actions always publish the whole repository. There is no way to publish only changed
resources.

@action import_content Imports all content from the given `content_dir` parameter or the path given as the resource name.
@action publishall_content Publishes all content from the content management server repository using the `publishall` server tool. This action only works with an empty master live server repository. This action consumes considerably less memory than the `bulkpublish_content` action.
@action bulkpublish_content Publishes all content from the content management server repository using the `bulkpublish` server tool.
@action import_users Imports all users from the given array of user files filenames.
@action upload_workflows Upload all workflows given by the arguments `builtin_workflows` and `custom_workflows`

@section Content User Directory Layoyt

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

resource_name :blueprint_dev_tooling_content
actions :import_content, :import_themes, :publishall_content, :bulkpublish_content, :import_users, :upload_workflows
default_action :nothing

default_action :nothing

# <> @attribute content_dir The directory containing the content. See above description about the expected layout.
property :content_dir, kind_of: String, name_property: true
# <> @attribute serverimport_extra_args An array of extra args passed to the contentimport call.
property :serverimport_extra_args, kind_of: Array, default: []
# <> @attribute builtin_workflows An array of workflow names from the builtin workflows to upload.
property :builtin_workflows, kind_of: Array, default: []
# <> @attribute custom_workflows An array of paths to custom workflow definitions to upload.
property :custom_workflows, kind_of: Array, default: []
# <> @attribute cms_ior The ior of the content management server. Only necessary if publishall_content is being used.
property :cms_ior, kind_of: String, default: 'http://localhost:40180/ior'
# <> @attribute cms_tools_dir The path of the directory, where the server tools of the content management server are installed.
property :cms_tools_dir, kind_of: String, default: lazy { node['blueprint']['tools']['content-management-server']['dir'] }
# <> @attribute mls_ior The ior of the master live server. Only necessary if publishall_content is being used.
property :mls_ior, kind_of: String, default: 'http://localhost:40280/ior'
# <> @attribute mls_tools_dir The path of the directory, where the server tools of the master live server are installed.
property :mls_tools_dir, kind_of: String, default: lazy { node['blueprint']['tools']['master-live-server']['dir'] }
# <> @attribute wfs_tools_dir The path of the directory, where the server tools of the workflow server are installed.
property :wfs_tools_dir, kind_of: String, default: lazy { node['blueprint']['tools']['workflow-server']['dir'] }
# <> @attribute theme_importer_dir The path of the directory, where the theme-importer is installed.
property :theme_importer_dir, kind_of: String, default: lazy { node['blueprint']['tools']['theme-importer']['dir'] }
# <> @attribute themes_zip_url The url of the themes archive, can be a local file URL.
property :themes_zip_url, kind_of: String
# <> @attribute user The username to use for execute and filesystem resources.
property :user, kind_of: String, default: lazy { node['blueprint']['user'] }
# <> @attribute group The group to use for execute and filesystem resources.
property :group, kind_of: String, default: lazy { node['blueprint']['group'] }
# <> @attribute timeout The timeout for all execute resource blocks.
property :timeout, kind_of: Integer, default: 2_400
# <> @attribute publishall_contentquery The contentquery to determine which content should not be published. Only effective on publishall_content action.
property :publishall_contentquery, kind_of: String, default: 'NOT BELOW PATH \'/Home\''
# <> @attribute publishall_threads The number of concurrent threads for replicating content
property :publishall_threads, kind_of: Integer, default: 1


CONTENT_IMPORTED = 'CONTENT_IMPORTED'.freeze
CONTENT_PUBLISHED = 'CONTENT_PUBLISHED'.freeze
BUILTIN_WORKFLOWS_UPLOADED = 'BUILTIN_WORKFLOWS_UPLOADED'.freeze
CUSTOM_WORKFLOW_UPLOADED = 'WORKFLOW_UPLOADED'.freeze
USERS_IMPORTED = 'USERS_IMPORTED'.freeze
THEMES_IMPORTED ='THEMES_IMPORTED'.freeze

action :import_themes do
  raise "Cannot import themes, theme archive url:#{new_resource.themes_zip_url}] not defined" unless new_resource.themes_zip_url
  raise "Cannot import themes, theme-importer not found at #{new_resource.theme_importer_dir}" unless ::File.exist?("#{new_resource.theme_importer_dir}/bin/import-themes.jpif")
  import_themes_marker_file = "#{new_resource.content_dir}/#{THEMES_IMPORTED}"
  workaround_log "THEMES ALREADY IMPORTED, DELETE #{new_resource.content_dir}/#{THEMES_IMPORTED} TO REIMPORT" if ::File.exist?(import_themes_marker_file)

  themes_import = execute "import themes from #{new_resource.themes_zip_url}" do
    command "#{new_resource.theme_importer_dir}/bin/cm import-themes -u admin -p admin #{new_resource.themes_zip_url}"
    timeout new_resource.timeout
    user new_resource.user
    group new_resource.group
    not_if { ::File.exist?(import_themes_marker_file) }
  end

  file import_themes_marker_file do
    owner new_resource.user
    group new_resource.group
    action :nothing
    subscribes :create, themes_import, :immediately
  end
end

action :import_content do
  raise "Cannot import content, content dir does not exist at #{new_resource.content_dir}" unless ::File.exist?(new_resource.content_dir)
  raise "Cannot import content, Content Management Server Tools not found at #{new_resource.cms_tools_dir}" unless ::File.exist?("#{new_resource.cms_tools_dir}/bin/serverimport.jpif")
  import_marker_file = "#{new_resource.content_dir}/#{CONTENT_IMPORTED}"
  workaround_log "CONTENT ALREADY IMPORTED, DELETE #{new_resource.content_dir} TO REIMPORT" if ::File.exist?(import_marker_file)

  content_import = execute "import content from #{new_resource.content_dir}" do
    command "#{new_resource.cms_tools_dir}/bin/cm serverimport -r -u admin -p admin --no-validate-xml #{new_resource.serverimport_extra_args.join(' ')} #{new_resource.content_dir}/content"
    timeout new_resource.timeout
    user new_resource.user
    group new_resource.group
    not_if { ::File.exist?(import_marker_file) }
  end

  file import_marker_file do
    owner new_resource.user
    group new_resource.group
    action :nothing
    subscribes :create, content_import, :immediately
  end
end

action :publishall_content do
  raise "Cannot publish content, content dir does not exist at #{new_resource.content_dir}" unless ::File.exist?(new_resource.content_dir)
  raise "Cannot publish content, Content Management Server Tools not found at #{new_resource.cms_tools_dir}" unless ::File.exist?("#{new_resource.cms_tools_dir}/bin/publishall.jpif")
  raise "Cannot publish content, Content Management Server Tools not found at #{new_resource.cms_tools_dir}" unless ::File.exist?("#{new_resource.cms_tools_dir}/bin/bulkpublish.jpif")
  publish_marker_file = "#{new_resource.content_dir}/#{CONTENT_PUBLISHED}"
  workaround_log 'CONTENT ALREADY PUBLISHED, DROP MASTER DB TO REPUBLISH' if ::File.exist?(publish_marker_file)

  execute "check in all content from #{new_resource.content_dir}" do
    command "#{new_resource.cms_tools_dir}/bin/cm bulkpublish -u admin -p admin -c"
    timeout new_resource.timeout
    user new_resource.user
    group new_resource.group
    not_if { ::File.exist?(publish_marker_file) }
  end

  content_publish = execute "publish content from #{new_resource.content_dir}" do
    command "#{new_resource.cms_tools_dir}/bin/cm publishall -a -cq \"#{new_resource.publishall_contentquery}\" -t #{new_resource.publishall_threads} #{new_resource.cms_ior} admin admin #{new_resource.mls_ior} admin admin"
    timeout new_resource.timeout
    user new_resource.user
    group new_resource.group
    not_if { ::File.exist?(publish_marker_file) }
  end

  file publish_marker_file do
    owner new_resource.user
    group new_resource.group
    action :nothing
    subscribes :create, content_publish, :immediately
  end
end

action :bulkpublish_content do
  raise "Cannot publish content, content dir does not exist at #{new_resource.content_dir}" unless ::File.exist?(new_resource.content_dir)
  raise "Cannot publish content, Content Management Server Tools not found at #{new_resource.cms_tools_dir}" unless ::File.exist?("#{new_resource.cms_tools_dir}/bin/publishall.jpif")
  raise "Cannot publish content, Content Management Server Tools not found at #{new_resource.cms_tools_dir}" unless ::File.exist?("#{new_resource.cms_tools_dir}/bin/bulkpublish.jpif")
  publish_marker_file = "#{new_resource.content_dir}/#{CONTENT_PUBLISHED}"
  workaround_log 'CONTENT ALREADY PUBLISHED, DROP MASTER DB TO REPUBLISH' if ::File.exist?(publish_marker_file)

  content_publish = execute 'publish content' do
    command "#{new_resource.cms_tools_dir}/bin/cm bulkpublish -u admin -p admin -a -b -c"
    timeout new_resource.timeout
    user new_resource.user
    group new_resource.group
    not_if { ::File.exist?(publish_marker_file) }
  end

  file publish_marker_file do
    owner new_resource.user
    group new_resource.group
    action :nothing
    subscribes :create, content_publish, :immediately
  end
end

action :import_users do
  raise "Cannot import users, content dir does not exist at #{new_resource.content_dir}/users" unless ::File.exist?("#{new_resource.content_dir}/users")
  raise "Cannot import users, Content Management Server Tools not found at #{new_resource.cms_tools_dir}" unless ::File.exist?("#{new_resource.cms_tools_dir}/bin/restoreusers.jpif")

  Dir.glob("#{new_resource.content_dir}/users/*.xml") do |user_file|
    user_marker_file = "#{user_file}_#{USERS_IMPORTED}"
    workaround_log "USERS FROM #{user_file} ALREADY IMPORTED, DELETE #{user_file}_#{USERS_IMPORTED} TO REIMPORT" if ::File.exist?(user_marker_file)
    user_import = execute "import users from #{user_file}" do
      command "#{new_resource.cms_tools_dir}/bin/cm restoreusers -u admin -p admin -f #{user_file}"
      timeout new_resource.timeout
      user node['blueprint']['user']
      group node['blueprint']['group']
      not_if { ::File.exist?(user_marker_file) }
    end

    file user_marker_file do
      owner new_resource.user
      group new_resource.group
      action :nothing
      subscribes :create, user_import, :immediately
    end
  end
end

action :upload_workflows do
  raise "Cannot upload workflows, content dir does not exist at #{new_resource.content_dir}" unless ::File.exist?(new_resource.content_dir)
  raise "Cannot upload workflows, Content Management Server Tools not found at #{new_resource.wfs_tools_dir}" unless ::File.exist?("#{new_resource.wfs_tools_dir}/bin/upload.jpif")
  builtin_marker_file = "#{new_resource.content_dir}/#{BUILTIN_WORKFLOWS_UPLOADED}"
  # builtin workflows
  workaround_log "BUILTIN WORKFLOWS ALREADY UPLOADED, DELETE #{new_resource.content_dir}/#{BUILTIN_WORKFLOWS_UPLOADED} TO REUPLOAD" if ::File.exist?(builtin_marker_file)
  workflows_builtin = execute 'import builtin workflows' do
    command "#{new_resource.wfs_tools_dir}/bin/cm upload -u admin -p admin  -n #{new_resource.builtin_workflows.join(' ')}"
    timeout new_resource.timeout
    user new_resource.user
    group new_resource.group
    not_if { ::File.exist?(builtin_marker_file) }
  end

  file builtin_marker_file do
    owner new_resource.user
    group new_resource.group
    subscribes :create, workflows_builtin, :immediately
  end

  # custom workflows
  new_resource.custom_workflows.each do |workflow_definition|
    raise "Cannot upload workflow from #{workflow_definition}, file does not exist" unless ::File.exist?(workflow_definition)
    custom_marker_file = "#{new_resource.content_dir}/#{::File.basename(workflow_definition)}_#{CUSTOM_WORKFLOW_UPLOADED}"
    workaround_log "WORKFLOW FROM #{workflow_definition} ALREADY UPLOADED, DELETE #{workflow_definition}_#{CUSTOM_WORKFLOW_UPLOADED} TO REUPLOAD" if ::File.exist?(custom_marker_file)

    workflow_custom = execute "import workflow from #{workflow_definition}" do
      command "#{new_resource.wfs_tools_dir}/bin/cm upload -u admin -p admin -f #{workflow_definition}"
      timeout new_resource.timeout
      user new_resource.user
      group new_resource.group
      not_if { ::File.exist?(custom_marker_file) }
    end

    file custom_marker_file do
      owner new_resource.user
      group new_resource.group
      subscribes :create, workflow_custom, :immediately
    end
  end
end

def workaround_log(message)
  # this is a workaround to also print the log message using kitchen. By default kitchen log level is warn and cannot be changed, see https://github.com/test-kitchen/test-kitchen/issues/529.
  if Chef::Config[:file_cache_path].include?('kitchen')
    puts message
  else
    Chef::Log.info(message)
  end
end
