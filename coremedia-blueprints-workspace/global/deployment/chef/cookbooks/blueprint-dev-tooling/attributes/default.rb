#<> The directory to extract the test data to
default['blueprint']['dev']['content']['dir'] = "#{node['blueprint']['temp_dir']}/test-data"
#<> The url to the content zip. Supported protocols are file and http(s).
default['blueprint']['dev']['content']['content_zip'] = 'file://localhost/test-data/content-users.zip'
#<> The url to the frontend zip. Supported protocols are file and http(s).
default['blueprint']['dev']['content']['frontend_zip'] = 'file:///test-data/frontend.zip'
#<> Set this to skip to skip import completely or force to force reimport.
default['blueprint']['dev']['content']['mode'] = 'default'
#<> Extra arguments to be set on the serverimport call.
default['blueprint']['dev']['content']['serverimport_extra_args'] = []
#<> An array of builtin workflow names to be uploaded during content import.
default['blueprint']['dev']['content']['workflow_definitions']['builtin'] = %w(studio-simple-publication.xml immediate-publication.xml studio-two-step-publication.xml three-step-publication.xml global-search-replace.xml /com/coremedia/translate/workflow/derive-site.xml /com/coremedia/translate/workflow/synchronization.xml)
#<> An array of custom workflow definitions paths(absolute or classpath) to be uploaded during content import.
default['blueprint']['dev']['content']['workflow_definitions']['custom'] = ['/opt/coremedia/workflow-server-tools/properties/corem/workflows/translation.xml']
#<> The contentquery for the publishall content action.
default['blueprint']['dev']['content']["publishall_contentquery"] = 'NOT BELOW PATH \'/Home\''
#<> The number of concurrent threads to replicate the content
default['blueprint']['dev']['content']['publishall_threads'] = 1
#<> The database to install (mysql | postgresql)
default['blueprint']['dev']['db']['type'] = 'mysql'
#<> The database host to create schemas on
default['blueprint']['dev']['db']['host'] = 'localhost'
#<> The schema, user and password for the content-management-server
default['blueprint']['dev']['db']['schemas']['content-management-server'] = 'cm_management'
#<> The schema, user and password for the master-live-server
default['blueprint']['dev']['db']['schemas']['master-live-server'] = 'cm_master'
#<> The schema, user and password for the replication-live-server
default['blueprint']['dev']['db']['schemas']['replication-live-server'] = 'cm_replication'
#<> The schema, user and password for the caefeeder preview
default['blueprint']['dev']['db']['schemas']['caefeeder-preview'] = 'cm_mcaefeeder'
#<> The schema, user and password for the caefeeder live
default['blueprint']['dev']['db']['schemas']['caefeeder-live'] = 'cm_caefeeder'
