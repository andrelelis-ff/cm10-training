tools container
===============

This tools container does:
* restore users - entrypoint `import-user`
* import content - entrypoint `import-content`
* upload default workflows - entrypoint `import-default-workflows`
* upload custom workflow, next arg is the path to a custom workflow definitions. To upload multiple workflows execute this step multiple times.
* publish content - entrypoint `publish-content`.
* export content - entrypoint `export-content`, next arg is the list of exported paths, quote it as one arg!.

To achieve this, we make use of some entrypoint script logic and volumes. Importing, restoring and uploading
will be done only once. To ensure this, we query the content- and workflow-repository using `cm` tools.
If you want to reimport everything, you can use the environment variable toggle described
below.

Directories that can be mounted as Volumes
------------------------------------------

`/coremedia/import/content` - all content mounted below this path will be imported.
`/coremedia/import/users` - all user files mounted below this path will be restored.
`/coremedia/export` - the dir to export the content to.

Environment Variables
---------------------

* `SKIP_CONTENT` - if set to `true`, the whole import chain will be skipped.
* `FORCE_REIMPORT_CONTENT` - if set to `true`, marker files will be ignored and content reimported.
* `BLOB_STORAGE_URL` - if set to a http url, the process expects the blobs to be provided there for import.
* `CONTENT_ARCHIVE_URL` - if set to a http url pointing to a `content-users.zip`
as produced by the `test-data` maven module from the Blueprint workspace.

Configuration
-------------

To configure the tools for arbitrary environments, [confd](https://github.com/kelseyhightower/confd/tree/v0.16.0) has 
been added to the [java-application-base](https://hub.docker.com/repository/docker/coremedia/java-application-base) image.

By default the following environment variables can be used to configure the tools.

#### repository connection
* `CAP_CLIENT_SERVER_IOR_URL`  
* `CAP_CLIENT_TIMEZONE_DEFAULT`

#### resetcaefeeder
* `JDBC_DRIVER`
* `JDBC_URL`
* `JDBC_USER`
* `JDBC_PASSWORD`

#### repository sql access
* `SQL_STORE_DRIVER`
* `SQL_STORE_URL`
* `SQL_STORE_USER`
* `SQL_STORE_PASSWORD`

#### workflowconverter
* `WORKFLOW_MAP_ROLE_TRANSLATION_MANAGER_ROLE` 
* `WORKFLOW_MAP_ROLE_ADMINISTRATOREN`
* `WORKFLOW_MAP_ROLE_APPROVER_ROLE`
* `WORKFLOW_MAP_ROLE_COMPOSER_ROLE`
* `WORKFLOW_MAP_ROLE_PUBLISHER_ROLE`

Alternatively to environment variables, a yaml file can be used for the configuration keys. This can be configured using
the environment variable `CONFD_BACKEND` set to `file` and an environment variable `CONFD_ARGS` set to `-file <config.yml>`.

By specifying `CONFD_PREFIX`, all keys can be prefixed to separate deployment environments. I.e. by setting `CONFD_PREFIX=foo/bar`
environment keys are prefixed with `FOO_BAR` and yaml keys are expected to be placed below:
```yaml
foo:
  bar:
```

##### Example:
If you want to define multiple environments i.e. for uat and prod and content-management-server, master-live-server and
two replication-live-servers, you can define an env file like:
```bash
# UAT
UAT_MANAGEMENT_CAP_CLIENT_SERVER_IOR_URL=http://uat-content-management-server:8080/ior
UAT_MASTER_CAP_CLIENT_SERVER_IOR_URL=http://uat-master-live-server:8080/ior
UAT_REPLICATION_1_CAP_CLIENT_SERVER_IOR_URL=http://uat-master-live-server:8080/ior
UAT_REPLICATION_2_CAP_CLIENT_SERVER_IOR_URL=http://uat-master-live-server:8080/ior
# PROD
PROD_MANAGEMENT_CAP_CLIENT_SERVER_IOR_URL=http://prod-content-management-server:8080/ior
PROD_MASTER_CAP_CLIENT_SERVER_IOR_URL=http://prod-master-live-server:8080/ior
PROD_REPLICATION_1_CAP_CLIENT_SERVER_IOR_URL=http://prod-master-live-server:8080/ior
PROD_REPLICATION_2_CAP_CLIENT_SERVER_IOR_URL=http://prod-master-live-server:8080/ior
```

Now you can use the `management-tools` image. Remember these examples do not work in the docker-compose setup unless
you add `--network compose_backend` to the command-line. 

* Connect against UAT content-management-server
```bash
docker run --rm \
  --env-file=.env -e CONFD_PREFIX=uat/management \
  coremedia/management-tools \
  confd tools/bin/cm dump -u admin -p admin 3
```
* Connect against PROD master-live-server
```bash
docker run --rm \
  --env-file=.env -e CONFD_PREFIX=prod/master \
  coremedia/management-tools \
  confd tools/bin/cm dump -u admin -p admin 3
```

General purpose tools
---------------------

Using the default entrypoint all default tools can be used, i.e.

```
docker run --rm --entrypoint="/coremedia/tools/bin/cm" --user="coremedia" --network="coremedia" <REGISTRY>/management-tools:<VERSION> dump
```

Customize the entrypoint chain in docker-compose
------------------------------------------------

Just add entrypoint list to your `management-tools` service, i.e.

```yaml

version: "3"

services:
  management-tools:
    ...
    command:
      - import-content
      - import-user
      - export-content
      - "PATH1 PATH2 PATH3"
```
