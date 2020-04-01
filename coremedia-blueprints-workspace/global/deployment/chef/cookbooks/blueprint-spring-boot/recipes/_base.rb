include_recipe 'blueprint-base'
package 'systemd'

execute 'systemd fix' do
  command 'systemd-machine-id-setup'
end

service 'systemd-journald' do
  action [:restart]
end
