=begin
#<
This definition creates the service user.

@param user
@param group
@param home
@param filehandle_limit
@param process_limit

@section Examples

```ruby
blueprint_service_user 'solr' do
  user  'solr'
  group 'tomcat'
  home '/opt/coremedia/solr'
  filehandle_limit 25_000
  process_limit 5000
end
```

#>
=end
define :blueprint_service_user, :filehandle_limit => 25_000, :process_limit => 5000 do
  params[:user] ||= params[:name]
  params[:group] ||= params[:user]
  raise("Required argument home is missing for user #{params[:user]}") unless params[:home]

  user "user #{params[:user]}" do
    username params[:user]
    shell '/sbin/nologin'
    home params[:home]
    comment 'CoreMedia Blueprint Service user'
    gid params[:group]
    system true
    not_if { params['user'] == 'root' }
    not_if { node['etc']['passwd'][params['user']] }
  end

  group "adding user #{params[:user]} to group #{params[:group]}" do
    group_name params[:group]
    action [:modify]
    members params[:user]
    append true
  end

  directory params[:home] do
    group params[:group]
    owner params[:user]
    recursive true
  end

  user_ulimit params[:user] do
    filehandle_limit params[:filehandle_limit]
    process_limit params[:filehandle_limit]
  end
end