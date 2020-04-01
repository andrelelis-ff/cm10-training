
cookbook_file '/etc/init.d/disable-thp' do
  source 'disable-transparent-hugepages'
  owner 'root'
  group 'root'
  mode '0755'
  action :create

  subscribes :create, 'service[disable-thp]', :before
end

service 'disable-thp' do
  supports start: true, stop: false, restart: false, status: true
  action [:enable, :start]
end
