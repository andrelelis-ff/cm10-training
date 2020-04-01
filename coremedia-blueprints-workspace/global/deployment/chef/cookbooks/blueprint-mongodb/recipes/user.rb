if node['mongodb3']['config']['mongod']['security']['authorization'] == 'enabled'

  #create user coremedia
  execute "create user" do
    cmd = "db.createUser({user: 'coremedia', pwd: 'coremedia', roles: ['userAdminAnyDatabase', 'dbAdminAnyDatabase', 'readWriteAnyDatabase']});"
    command "mongo admin --host localhost --eval \"#{cmd}\""
    ignore_failure true
  end

end
