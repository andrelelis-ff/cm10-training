# CoreMedia Blueprint

## Dependency Check

> Checks if a module has added all used dependencies to its package.json

Currently supports JavaScript modules bundled with webpack by providing a DependencyCheckWebpackPlugin and SCSS compiler with node-sass by providing a custom importer via getDependencyCheckNodeSassImporter.

Both integrations check if a file contains dependencies to a file that is not in the same package and will check the package.json for an explicit dependency to the other package. If the dependency is missing and the file is not filtered using the "include"/"exclude" options an error will be thrown to indicate that the dependency needs to be added to the package.json of its package.

### Using the DependencyCheckWebpackPlugin
 
Add the following code to your webpack configuration file:

```
const { DependencyCheckWebpackPlugin } = require("@coremedia/dependency-check");

...

module.exports = {
  ...
  plugins: [
    ...
    new DependencyCheckWebpackPlugin({
        // options...
    })
  ]
  ...
};
```

### Using the node-sass custom importer

Add the following code to your node-sass configuration file:

```
const { getDependencyCheckNodeSassImporter } = require("@coremedia/dependency-check");

...

module.exports = {
  ...
  importer: getDependencyCheckNodeSassImporter()
  ...
};
```

### Options (both integrations)

|Option|Type|Default|Description|
|---|---|---|---|
|include|String or Regex or Array<String|Regex>|Specifies (a) path(s) to be included to the dependency check (e.g. module paths or specific files). If no paths are provided all files will be included.|
|exclude|Array of String|[]|Specifies (a) path(s) to be excluded from the dependency check (e.g. module paths or specific files). If no paths are provided no file will be excluded.|
