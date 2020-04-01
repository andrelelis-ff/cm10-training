# CoreMedia Blueprint

## Documentation

Refer to the Getting Started chapter of the product's Developer Manual for a detailed description of the project workspace.

## Structure

The Blueprint is separated into several (sub-)workspaces, which are grouped into five categories:

* `apps` - containes a subfolder for each CoreMedia application workspace
* `content` - contains `test-data` folders with test content and test user definitions
* `frontend` - contains the CoreMedia frontend workspace
* `shared` - contains two workspaces with shared code: `common` and `middle`
* `global` - contains `deployment`, `examples` and `management-tools`

See Blueprint Developer Manual, section "Structure of the Workspace", for details.

For information on managing extensions, refer to [workspace-configuration/extensions/README.md](./workspace-configuration/extensions/README.md).

## Deployment

The `deployment` folder contains

* an out-of-the-box deployment example using the configuration management framework _Chef_. Refer to [global/deployment/chef/README.adoc](./global/deployment/chef/README.adoc) for details.
* a Docker Compose setup. Refer to [global/deployment/docker/README.adoc](./global/deployment/docker/README.adoc) for details.

## Updating the Workspace

CoreMedia provides this dedicated [CoreMedia Blueprint GitHub mirror repository](https://github.com/coremedia-contributions/coremedia-blueprints-workspace) for customers and partners.

Simply use GitHubs web frontend to visually compare changes between release versions. Each release is aggregated in a [single git commit](https://github.com/coremedia-contributions/coremedia-blueprints-workspace/commits/master).

CoreMedia heavily encourages you to use one of the following approaches:

### Updating via Git

Instead of extracting the ZIP archive from the CoreMedia download site, you can simply use Git to fetch updates and merge them with your own customizations.

### Updating via Patch files

Although CoreMedia recommends to use Git, you can keep using your favorite source code management system by applying release changes patch by patch.

For example, CoreMedia release changes are mirrored in https://github.com/coremedia-contributions/coremedia-blueprints-workspace

Simply add ``.patch`` to the commit URL to be able to download in patch format (hidden GitHub feature).

In some cases GitHub won't generate the patch (e.g. `error: too big or took too long to generate` or `Content containing PDF or PS header bytes cannot be rendered from this domain for security reasons.`).

Use ``git format-patch -1 <commit>`` on the command-line as a workaround (<http://git-scm.com/docs/git-format-patch>).

Please contact [support@coremedia.com](mailto:support@coremedia.com) if you need further assistance!
