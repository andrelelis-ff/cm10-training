"use strict";

const loggerFactory = require("./loggerFactory");

const defaultLogger = loggerFactory({
  name: "default",
  level: "info",
  prefix: null,
});
const cache = { default: defaultLogger };

const getCachedLogger = name => cache[name];

const updateCache = (name, logger) => {
  cache[name] = logger;
};

const cmLogger = Object.assign(defaultLogger, {
  getLogger: options => {
    if (typeof options === "string") {
      options = { name: options };
    }

    if (!options || typeof options.name !== "string" || !options.name.length) {
      throw new TypeError("You must supply a name when creating a logger.");
    }

    if (!options.id) {
      options.id = options.name;
    }

    let logger = getCachedLogger(options.id);
    if (!logger) {
      logger = loggerFactory(options);
      updateCache(options.id, logger);
    }
    return logger;
  },
  getLevelFromWebpackStats: stats => {
    const LEVEL = {
      verbose: "debug",
      normal: "info",
      minimal: "warn",
      "errors-only": "error",
      none: "silent",
    };
    if (typeof stats === "string" && Object.keys(LEVEL).includes(stats)) {
      return LEVEL[stats];
    }
  },
});

module.exports = cmLogger;
