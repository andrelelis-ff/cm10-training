# cm

> The CoreMedia command line interface for building themes.

The `cm` CLI provides a couple of commands which are helpful when working with the CoreMedia Frontend Workspace. You can run `cm --help` to view all available commands and options. To view the available options of each command run `cm <cmd> --help`.

## Installation

Install the CLI into your local workspace and you may use `cm` CLI in scripts:

```shell
yarn add @coremedia/cm-cli
```

Example of package.json:

```json
{
  "name": "example",
  "dependencies": {
    "@coremedia/cm-cli": "^2.0.0"
  },
  "scripts": {
    "test": "cm test"
  }
}
```

Run script:

```bash
yarn test
```

Install the CLI globally and you'll have access to `cm` CLI from the command line:

```shell
yarn global add @coremedia/cm-cli
```

Run `cm` CLI with `test` command from command line:

```bash
cm test
```

## Commands

The following commands may be used:

### create-theme

_Run this command by executing `cm create-theme <name>`._

This command creates a new empty theme skeleton as a development starting point in the themes folder of the frontend workspace.

### create-brick

_Run this command by executing `cm create-brick <name>`._

This command creates a new hello world brick as a development starting point in the bricks folder of the frontend workspace.

#### Options

##### --help

Alias: `-h`. Show the help information, similar to this page.

##### --version

Alias: `-v`. Print the version and exit.

##### --verbose

Display detailed information output.

### monitor

_Run this command by executing `cm monitor`._

This command starts the monitor mode to watch for file changes and update a theme on local or remote CAE.

#### Options

##### --help

Alias: `-h`. Show the help information, similar to this page.

##### --version

Alias: `-v`. Print the version and exit.

##### --verbose

Display detailed information output.

### theme-importer

_Run this command by executing `cm theme-importer <command>`._

This command executes the theme-importer for remote file operations of themes.

#### Options

##### --help

Alias: `-h`. Show the help information, similar to this page.

##### --version

Alias: `-v`. Print the version and exit.

#### Commands

##### login

_Run this command by executing `cm theme-importer login [options]`._

This command authenticates a user and creates an API key.

###### Options

####### --studioUrl (required)

Studio URL.

####### --previewUrl

Preview URL.

####### --proxyUrl

Proxy URL.

####### --username (required)

Alias: `-u`. Username.

####### --password (required)

Alias: `-p`. Password.

####### --help

Alias: `-h`. Show the help information, similar to this page.

####### --version

Alias: `-v`. Print the version and exit.

##### logout

_Run this command by executing `cm theme-importer logout`._

Logout a user and delete the API key.

###### Options

####### --help

Alias: `-h`. Show the help information, similar to this page.

####### --version

Alias: `-v`. Print the version and exit.

##### whoami

_Run this command by executing `cm theme-importer whoami`._

This command outputs info about the logged in user.

###### Options

####### --help

Alias: `-h`. Show the help information, similar to this page.

####### --version

Alias: `-v`. Print the version and exit.

##### upload-theme

_Run this command by executing `cm theme-importer upload-theme`._

This command uploads a theme to a remote server.

###### Options

####### --help

Alias: `-h`. Show the help information, similar to this page.

####### --version

Alias: `-v`. Print the version and exit.

##### deploy-theme

_Run this command by executing `cm theme-importer deploy-theme`._

This command uploads and deploys a theme to a remote server.

###### Options

####### --help

Alias: `-h`. Show the help information, similar to this page.

####### --version

Alias: `-v`. Print the version and exit.
