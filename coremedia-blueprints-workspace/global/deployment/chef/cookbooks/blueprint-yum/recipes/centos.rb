=begin
#<
This recipe configures yum repos for centos. This is a wrapper recipe for the official recipe from `yum-centos` cookbook.
#>
=end

include_recipe 'yum-centos::default' if node['platform'] == 'centos'
