#<> An array of schemas create when using the schema recipe
default['blueprint']['postgresql']['schemas'] = []
default['blueprint']['postgresql']['version'] = '9.6'
#<> inital root password for postgresql db
default['blueprint']['postgresql']['initial_root_password'] = 'coremedia'
#<> enable huge pages valid values are: (try | on | off)
default['blueprint']['postgresql']['config']['huge_pages'] = 'off'
default['blueprint']['postgresql']['config']['max_connections'] = '100'
default['blueprint']['postgresql']['config']['shared_buffers'] = '512MB'
default['blueprint']['postgresql']['config']['effective_cache_size'] = '512MB'
default['blueprint']['postgresql']['config']['work_mem'] = '16MB'
default['blueprint']['postgresql']['config']['maintenance_work_mem'] = '64MB'
default['blueprint']['postgresql']['config']['checkpoint_segments'] = '384MB'
default['blueprint']['postgresql']['config']['checkpoint_completion_target'] = '0.9'
default['blueprint']['postgresql']['config']['default_statistics_target'] = '100'
default['blueprint']['postgresql']['config']['autovacuum'] = false
