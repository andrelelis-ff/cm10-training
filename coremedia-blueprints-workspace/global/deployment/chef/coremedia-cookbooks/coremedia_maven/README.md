# Description

This is a library cookbook. It provides a custom resource to download and optionally extract a Maven artifacts transparently either
from a remote repository via HTTP or from file system. It handles authentication, and knows about the standard maven repo layout.

It differs from the community maven cookbook in the way it downloads the artifacts. The community cookbook installs Maven
and uses the dependency plugin to download the artifact. This has the major drawback, that:

* you need java on the system

* you need to install maven

* you download transitively a lot of unnecessary dependencies


# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

* redhat
* centos
* ubuntu
* debian
* windows

## Cookbooks:

*No dependencies defined*

# Attributes

*No attributes defined*

# Recipes

*No recipes defined*

# Resources

* [coremedia_maven](#coremedia_maven) - Downloads a Maven artifact from a local or remote Maven repository.

## coremedia_maven


Downloads a Maven artifact from a local or remote Maven repository.

If attribute `nexus_url` and `nexus_repo` is set, the artifact will be retrieved using the rest API of nexus and the version
of the artifact may be set to `RELEASE`, `LATEST` or `X-SNAPSHOT` and the versions will be resolved accordingly.

### Actions

- install: Install an artifact Default action.

### Attribute Parameters

- path: The path to download the artifact to. Defaults to the name of the resource.
- group_id: Maven groupId.
- artifact_id: Maven artifactId.
- version: Maven version.
- packaging: Maven packaging type, defaults to the file extension of the path attribute. Defaults to <code>lazy { ... }</code>.
- classifier: Maven classifier, defaults to not set. Defaults to <code>nil</code>.
- checksum: The SHA-256 checksum of the artifact Defaults to <code>nil</code>.
- repository_url: The Url of the maven repository, supports all protocols of the remote_file resource. Defaults to Maven Central. Defaults to <code>"http://repo1.maven.org/maven2/"</code>.
- username: The username for the Maven repository, defaults to not set. Defaults to <code>nil</code>.
- password: The password for the Maven repository, defaults to not set. Defaults to <code>nil</code>.
- owner: User ownership (linux only), defaults to "root". Defaults to <code>"root"</code>.
- group: Group membership (linux only), defaults to "root". Defaults to <code>"root"</code>.
- mode: File permissions, defaults to "00755". Defaults to <code>493</code>.
- extract_to: Extract to folder path if downloaded artifact has changed. Extraction is skipped if no folder path is set. Defaults to <code>nil</code>.
- extract_force_clean: Set this to true to force deletion of target folder upfront. Defaults to <code>false</code>.
- backup: Number of version to backup in the Chef cache. This works only with non file:// urls. Set to false to disable backup. Defaults to <code>false</code>.
- nexus_url: Set this to the base url of your nexus to use the REST API for artifact resolution. If set this method has precedence to the repository_url attribute.
- nexus_repo: The repo name from which to resolve artifacts. Defaults to <code>"releases"</code>.

### Examples

```ruby
coremedia_maven '/var/tmp/solr.war' do
  group_id 'org.apache.solr'
  artifact_id 'solr'
  version '4.5.0'
  owner 'coremedia'
  group 'coremedia'
  mode 0644
  extract_to '/usr/local/tomcat/webapps/solr'
  notify :restart, 'tomcat[solr]', :immediately
end
```

# Author

Author:: Felix Simmendinger (<felix.simmendinger@coremedia.com>)
