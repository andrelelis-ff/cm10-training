# CoreMedia Blueprint

## Zipper

Zipper creates a zip archive from a list of sources which can be glob patterns, directories or distinct files.

### Syntax

Add the following code to your webpack configuration file:

```javascript
zipper(filepath, context, patterns[, verbose])
```

#### Parameters

`filepath`
The absolute filepath of the archive.

`context`
The working directory that determines how to interpret the `source` path, shared for all patterns.

`patterns`
An Array of Objects containing `source` (a glob, a directory or a file) to be included in the archive and optionally `prefix` for the destination path inside the archive.

`verbose`
A flag whether to log verbose or not.

#### Return value

A `Promise` which will be resolved on success or rejected on error.

### Example

```javascript
const zipper = require('@coremedia/zipper');

zipper('/path/to/archive.zip', '/path/to/sources', [
  { source: 'js/index.js' },
  { source: 'README.md', prefix: 'doc' }
])
  .then(count => {
    console.log(`Compressed ${count} files`);
  })
  .catch(error => {
    console.log(`Creating archive failed: ${error.message}`);
  });
```

