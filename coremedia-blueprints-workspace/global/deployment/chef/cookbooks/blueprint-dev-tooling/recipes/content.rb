#
# Cookbook Name:: blueprint-dev-tooling
# Recipe:: content
#
# Copyright (c) 2015 The Authors, All Rights Reserved.

=begin
#<
This recipe imports and publishes content, uploads workflows and restore users. This recipe is intended to be used only in
development or test environments. Do not use it in production.
#>
=end

content_zip_source = URI(node['blueprint']['dev']['content']['content_zip'])
content_dir = node['blueprint']['dev']['content']['dir']

unless node['blueprint']['dev']['content']['content_zip'].empty?
  if content_zip_source.scheme == 'file'
    unzip_from = content_zip_source.path
  else
    content_zip = remote_file '/var/tmp/content.zip' do
      backup false
      group node['blueprint']['group']
      owner node['blueprint']['user']
      source content_zip_source.to_s
      not_if { node['blueprint']['dev']['content']['mode'] == 'skip' }
    end
    unzip_from = content_zip.path
  end

  directory content_dir do
    action :delete
    recursive true
    only_if { node['blueprint']['dev']['content']['mode'] == 'force' }
  end

  execute 'unzip content' do
    command "unzip -oqq #{unzip_from} -d #{content_dir}"
    user node['blueprint']['user']
    group node['blueprint']['group']
    not_if { ::File.exist?(content_dir) || node['blueprint']['dev']['content']['mode'] == 'skip' }
  end

  log 'Importing Content during delayed phase' do
    notifies :import_users, "blueprint_dev_tooling_content[#{content_dir}]", :delayed
    notifies :upload_workflows, "blueprint_dev_tooling_content[#{content_dir}]", :delayed
    notifies :import_themes, "blueprint_dev_tooling_content[#{content_dir}]", :delayed if node['blueprint']['dev']['content']['frontend_zip']
    notifies :import_content, "blueprint_dev_tooling_content[#{content_dir}]", :delayed
    # make sure that if force is set, we do not use publishall but bulkpublish
    notifies :publishall_content, "blueprint_dev_tooling_content[#{content_dir}]", :delayed if node['blueprint']['dev']['content']['mode'] != 'force'
    notifies :bulkpublish_content, "blueprint_dev_tooling_content[#{content_dir}]", :delayed if node['blueprint']['dev']['content']['mode'] == 'force'
    not_if { node['blueprint']['dev']['content']['mode'] == 'skip' }
  end
end

node.default['blueprint']['dev']['content']['cms_ior_url'] = "http://localhost:40180/ior"
node.default['blueprint']['dev']['content']['mls_ior_url'] = "http://localhost:40280/ior"
blueprint_dev_tooling_content content_dir do
  themes_zip_url node['blueprint']['dev']['content']['frontend_zip']
  serverimport_extra_args node['blueprint']['dev']['content']['serverimport_extra_args']
  builtin_workflows node['blueprint']['dev']['content']['workflow_definitions']['builtin']
  custom_workflows node['blueprint']['dev']['content']['workflow_definitions']['custom']
  cms_ior node['blueprint']['dev']['content']['cms_ior_url']
  mls_ior node['blueprint']['dev']['content']['mls_ior_url']
  publishall_contentquery node['blueprint']['dev']['content']['publishall_contentquery']
  publishall_threads node['blueprint']['dev']['content']['publishall_threads']
  action :nothing
end
