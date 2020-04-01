coremedia-maven Cookbook CHANGELOG
======================
This file is used to list changes made in each version of the coremdia-maven cookbook.

v3.0.2
------
- add retry/retry delay to `coremedia_maven` resource with with 3 retries and 3 seconds delay between.

v3.0.1
------
- fix support for Chef `13` and `14` when `extract_to` is used.

v3.0.0
-------
- the `coremedia_maven` resource has been migrated to Chef 13 resource DSL
- support for Chef `13`, `14`
- Chef < `12.5` is no longer supported

v.2.0.4
------
- create parent dir of artifact only if it does not exist.

v.2.0.3
------
- make `nexus_url` robust when trailing slash is missing.

v.2.0.2
------
- by using the attributes `nexus_url` and `nexus_repo` artifacts can now be resolved using metaversions like `RELEASE`, `LATEST` and
the standard `-SNAPSHOT` semantic. If both `nexus_repo` and `repository_url` are defined, nexus rest will be preferred.

v2.0.0
-------
- artifacts are no longer cached twice, if its a remote source a HEAD request is made and`If-Modified-Since` header is checked.
If its a local `file://` url, file checked locally and copied directly.
- attribute `extract_force_clean` added. Set this to true to delete the target dir before the extraction. Defaults to `false`.

### Breaking Changes
- Attribute `force_download` has been removed.

v1.0.3
-------
- fix eviction of versions cache and disable it by default.
- add parameter `backup` to set the number of chef cache backups to keep.
- use powershell_script resource to unzip on windows. This removes the windows cookbook dependency.

v1.0.2
-------
- add trailing slash to repository url if missing
- change cache path to `#{Chef::Config[:file_cache_path]}/maven-repo` for easier vagrant-cachier integration

v1.0.1
-------
- fix cache dir

v1.0.0
-------
- refactor cookbook into a pure library cookbook
- LWRP attribute `packaging` defaults now automatically to the file extension of the target path.
- add checksum attribute to LWRP
- add attribute `keep_versions` to keep downloaded versions in chef cache. Default is to evict all unused versions.
- allow downgrade to a previous installed version

#### Breaking Changes
- default recipe has been removed
- LWRP attribute `force_download` defaults to true if version ends with `-SNAPSHOT`

v.0.1.8
-------
- inital version to be published to supermarket
- added infrastrucutre to test with testkitchen and vagrant, ec2, docker or ssh remotely

v.0.1.7
--------
- improved documentation using `knife-cookbook-doc` gem
- set `jar` as default packaging type
- remove Vagrantfile and Strainerfile

v.0.1.6
---------
- refactored action `:extract` to be done during `:install` if `extract_to` attribute is set.

v.0.1.5
--------
- added extract action with attribute 'extract_to'
