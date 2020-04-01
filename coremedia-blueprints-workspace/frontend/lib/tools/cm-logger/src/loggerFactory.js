"use strict";

const chalk = require("chalk");
const format = require("date-fns/format");
const logSymbols = require("log-symbols");

const LEVELS = {
  DEBUG: 1,
  INFO: 2,
  WARN: 3,
  ERROR: 4,
  SILENT: 5,
};

const SYMBOLS = {
  debug: chalk.cyan("▶"),
  info: logSymbols.info,
  warn: logSymbols.warning,
  error: logSymbols.error,
  success: logSymbols.success,
};

const DEFAULTS = {
  id: +new Date(),
  name: "<unknown>",
  level: "warn",
  prefix: {
    level: opts => SYMBOLS[opts.level],
    name: opts => opts.name,
    template: `[{{time}}] {{level}} ${chalk.gray("｢{{name}}｣")} `,
    time: () => format(new Date(), "HH:mm:ss.SSS"),
  },
};

const buildPrefix = (level, options) => {
  const prefix = Object.assign({}, DEFAULTS.prefix, options.prefix);

  // use short template, if just info level (like in theme-importer)
  if (level === "info") {
    prefix.template = `[{{time}}] {{level}} `;
  }

  return prefix.template.replace(/{{([^{}]*)}}/g, (match, prop) => {
    const fn = prefix[prop];
    if (fn) {
      const { name } = options;
      return fn({ level, name });
    }
    return match;
  });
};

const loggerFactory = options => {
  const opts = Object.assign({}, DEFAULTS, options);
  opts.level =
    opts.level && Object.keys(LEVELS).includes(opts.level.toUpperCase())
      ? opts.level
      : DEFAULTS.level;
  const { [opts.level.toUpperCase()]: level } = LEVELS;

  const print = (level, args) => {
    let methodName;
    if (level === "debug") {
      methodName = "log";
    } else if (level === "success") {
      methodName = "info";
    } else {
      methodName = level;
    }

    if (opts.prefix) {
      const prefix = buildPrefix(level, opts);
      args.unshift(prefix);
    }

    try {
      // apply log function to arguments
      console[methodName](...args);
    } catch (e) {
      // fallback if anything goes wrong
      console[methodName](args.join(" "));
    }
  };

  return {
    get id() {
      return opts.id;
    },
    debug: (...args) => {
      if (level <= LEVELS.DEBUG) {
        print("debug", args);
      }
    },
    info: (...args) => {
      if (level <= LEVELS.INFO) {
        print("info", args);
      }
    },
    warn: (...args) => {
      if (level <= LEVELS.WARN) {
        print("warn", args);
      }
    },
    error: (...args) => {
      if (level <= LEVELS.ERROR) {
        print("error", args);
      }
    },
    success: (...args) => {
      if (level < LEVELS.SILENT) {
        print("success", args);
      }
    },
  };
};

module.exports = Object.assign(loggerFactory, { buildPrefix });
