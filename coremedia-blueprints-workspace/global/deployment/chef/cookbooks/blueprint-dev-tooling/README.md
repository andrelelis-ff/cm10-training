# Description

This is the development cookbook for the CoreMedia Blueprint. It contains recipes to:

* ease test system setup by composing sets of blueprint-* recipes, i.e. the `databases` recipe aggregates mongodb and mysql.
* build rpms for blueprint apps and tools.
* import and publish content


> **NOTE THAT THIS COOKBOOK IS NOT INTENTED TO BE USED IN A PRODUCTION SYSTEM**

# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* blueprint-base
* blueprint-yum
* blueprint-mysql
* blueprint-postgresql
* blueprint-mongodb
* blueprint-tools
* chef-sugar (~> 5.0.1)

# Attributes

* `node['blueprint']['dev']['content']['dir']` - The directory to extract the test data to. Defaults to `#{node['blueprint']['temp_dir']}/test-data`.
* `node['blueprint']['dev']['content']['content_zip']` - The url to the content zip. Supported protocols are file and http(s). Defaults to `file://localhost/test-data/content-users.zip`.
* `node['blueprint']['dev']['content']['frontend_zip']` - The url to the frontend zip. Supported protocols are file and http(s). Defaults to `file:///test-data/frontend.zip`.
* `node['blueprint']['dev']['content']['mode']` - Set this to skip to skip import completely or force to force reimport. Defaults to `default`.
* `node['blueprint']['dev']['content']['serverimport_extra_args']` - Extra arguments to be set on the serverimport call. Defaults to `[ ... ]`.
* `node['blueprint']['dev']['content']['workflow_definitions']['builtin']` - An array of builtin workflow names to be uploaded during content import. Defaults to `%w(studio-simple-publication.xml immediate-publication.xml studio-two-step-publication.xml three-step-publication.xml global-search-replace.xml /com/coremedia/translate/workflow/derive-site.xml /com/coremedia/translate/workflow/synchronization.xml)`.
* `node['blueprint']['dev']['content']['workflow_definitions']['custom']` - An array of custom workflow definitions paths(absolute or classpath) to be uploaded during content import. Defaults to `[ ... ]`.
* `node['blueprint']['dev']['content']["publishall_contentquery"]` - The contentquery for the publishall content action. Defaults to `NOT BELOW PATH \'/Home\'`.
* `node['blueprint']['dev']['content']['publishall_threads']` - The number of concurrent threads to replicate the content. Defaults to `1`.
* `node['blueprint']['dev']['db']['type']` - The database to install (mysql | postgresql). Defaults to `mysql`.
* `node['blueprint']['dev']['db']['host']` - The database host to create schemas on. Defaults to `localhost`.
* `node['blueprint']['dev']['db']['schemas']['content-management-server']` - The schema, user and password for the content-management-server. Defaults to `cm_management`.
* `node['blueprint']['dev']['db']['schemas']['master-live-server']` - The schema, user and password for the master-live-server. Defaults to `cm_master`.
* `node['blueprint']['dev']['db']['schemas']['replication-live-server']` - The schema, user and password for the replication-live-server. Defaults to `cm_replication`.
* `node['blueprint']['dev']['db']['schemas']['caefeeder-preview']` - The schema, user and password for the caefeeder preview. Defaults to `cm_mcaefeeder`.
* `node['blueprint']['dev']['db']['schemas']['caefeeder-live']` - The schema, user and password for the caefeeder live. Defaults to `cm_caefeeder`.

# Recipes

* [blueprint-dev-tooling::content](#blueprint-dev-toolingcontent) - This recipe imports and publishes content, uploads workflows and restore users.
* [blueprint-dev-tooling::databases](#blueprint-dev-toolingdatabases) - This recipe sets the db properties of all applications depending on the database type selected.

## blueprint-dev-tooling::content

This recipe imports and publishes content, uploads workflows and restore users. This recipe is intended to be used only in
development or test environments. Do not use it in production.

## blueprint-dev-tooling::databases

This recipe sets the db properties of all applications depending on the database type selected. Use this recipe only in
development environments. If the database host property is set to localhost, the recipe will also create the schemas for
the database.


### Example

```yaml
suites:
- name: default
  run_list:
    - recipe[blueprint-dev-tooling::databases]
  attributes:
    blueprint:
      dev:
        db:
          host: localhost
          type: postgresql
```

# Resources

* [blueprint_dev_tooling_content](#blueprint_dev_tooling_content) - This resource encapsulates some repository actions often used in test systems.

## blueprint_dev_tooling_content

This resource encapsulates some repository actions often used in test systems. Do not use this resource in production, it
is not a resource as chef intended to. It does not care about idempotency.

You should also be aware, that the publish actions always publish the whole repository. There is no way to publish only changed
resources.

### Actions

- import_themes:  Default action.
- bulkpublish_content: Publishes all content from the content management server repository using the `bulkpublish` server tool.
- import_content: Imports all content from the given `content_dir` parameter or the path given as the resource name.
- import_users: Imports all users from the given array of user files filenames.
- publishall_content: Publishes all content from the content management server repository using the `publishall` server tool. This action only works with an empty master live server repository. This action consumes considerably less memory than the `bulkpublish_content` action.
- upload_workflows: Upload all workflows given by the arguments `builtin_workflows` and `custom_workflows`

### Attribute Parameters

- content_dir:
- serverimport_extra_args:  Defaults to <code>[]</code>.
- builtin_workflows:  Defaults to <code>[]</code>.
- custom_workflows:  Defaults to <code>[]</code>.
- cms_ior:  Defaults to <code>"http://localhost:40180/ior"</code>.
- cms_tools_dir:  Defaults to <code>lazy { ... }</code>.
- mls_ior:  Defaults to <code>"http://localhost:40280/ior"</code>.
- mls_tools_dir:  Defaults to <code>lazy { ... }</code>.
- wfs_tools_dir:  Defaults to <code>lazy { ... }</code>.
- theme_importer_dir:  Defaults to <code>lazy { ... }</code>.
- themes_zip_url:
- user:  Defaults to <code>lazy { ... }</code>.
- group:  Defaults to <code>lazy { ... }</code>.
- timeout:  Defaults to <code>2400</code>.
- publishall_contentquery:  Defaults to <code>"NOT BELOW PATH '/Home'"</code>.
- publishall_threads:  Defaults to <code>1</code>.

### Content User Directory Layoyt

This resource expects a content_dir directory to have the following layout

```
|- content
|     |- some-content.xml
|     `- some-blob.jpg
`- users
     `- some-users.xml
```

The content below `content` may be structured arbitrary but must be a valid serverexport dump. User files should be placed
directly below the `users` directory.

### Guards

All actions are guarded so content is only imported once. The guards are achieved by using marker files.

* If content has already been imported, a marker file `CONTENT_IMPORTED` is placed at the root of the content dir.
* If content has been published, a marker file `CONTENT_PUBLISHED` is placed at the root of the content dir.
* If builtin workflows have been uploaded, a marker file `BUILTIN_WORKFLOWS_UPLOADED` is placed at the root of the content dir. If this resource is used multiple times, this may lead to multiple imports so take care using the upload actions.
* If custom workflows have been uploaded, a marker file `<workflow-defintion file>_WORKFLOW_UPLOADED` is being placed aside the workflow defintion file.
* If a users file has been imported, a marker file `<users file>_USERS_IMPORTED` is being placed beside the users file.

### Examples

```ruby
blueprint_dev_tooling_content '/some/path/to/a/content/dir' do
 builtin_workflows ['two-step-publication.xml']
 custom_workflows ['/some/path/to/a/workflow/definition.xml']
 action [:import_content, :import_user]
end
```

# Author

Author:: Your Name (<your_name@domain.com>)
