include_recipe 'blueprint-base::default'
chef_reports_dir = remote_directory "#{node['blueprint']['temp_dir']}/chef-reports" do
  source 'handlers'
  mode '0755'
  recursive true
  action :create
  sensitive true
end

chef_handler 'CoreMedia::ElapsedTimeReport' do
  source "#{chef_reports_dir.path}/elapsed_time_report.rb"
  action :enable
end

chef_handler 'CoreMedia::UpdatedResources' do
  source "#{chef_reports_dir.path}/updated_resources_report.rb"
  action :enable
end

chef_handler 'Coremedia::CookbookVersions' do
  source "#{chef_reports_dir.path}/cookbook_versions.rb"
  action :enable
end

if %w(kitchen development).include?(node.chef_environment)
  chef_handler 'Coremedia::DumpNode' do
    source "#{chef_reports_dir.path}/dump_node.rb"
    action :enable
  end
end
