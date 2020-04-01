# cm-logger

> The CoreMedia logger for Node.js.

Install this into your workspace and you'll have access to the `cm-logger`.

```shell
yarn add @coremedia/cm-logger
```

## Usage

```
const log = require("@coremedia/cm-logger");
log.info('unicorns!')
```

## Log LEVELS

By default `cm-logger` supports the following log level name-value pairs:

```json
{
  DEBUG: 1,
  INFO: 2,
  WARN: 3,
  ERROR: 4,
  SILENT: 5
}
```

## Default Logger

When requiring `cm-logger` in Node.js the default export will be a logger object.

### Methods

#### `debug`, `info`, `success`, `warn`, `error`

These methods correspond to the available log levels and accept parameters identical to their console counterparts:

```javascript
console.debug("...");
console.info("...");
console.warn("...");
console.error("...");
```

The `success` method uses `console.info`, but highlights the log message with a green checkmark.

#### `getLogger(options)`

Returns a new logger object. The options parameter should be an Object matching the options for the `loggerFactory`.

Note: The logger returned is cached, and subsequent requests for a logger of the same name will return the same logger object. If you require multiple unique loggers of the same name, pass an id property with a unique identifier and getLogger will use that over the name.
