=begin
#<
This recipe includes the recipe named like the environment. Never include an environment recipe directly.
#>
=end
include_recipe "blueprint::_#{node.chef_environment}"
