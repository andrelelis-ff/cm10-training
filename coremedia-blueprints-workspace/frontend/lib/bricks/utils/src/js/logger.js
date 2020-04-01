import "console-polyfill";

/**
 * Logging Levels
 * The logging levels are cumulative. If you for example set the logging level to WARN all warnings, errors and fatals are logged.
 * OFF - nothing is logged
 * ERROR - errors are logged
 * WARN - warnings are logged
 * INFO - infos are logged
 * LOG - log messages are logged
 * ALL - everything is logged
 * @readonly
 * @enum {number}
 */
export const LEVEL = {
  OFF: Number.MAX_VALUE,
  ERROR: 500,
  WARN: 400,
  INFO: 300,
  LOG: 200,
  ALL: Number.MIN_VALUE,
};

/**
 * Default settings for logger
 * @type {{level: LEVEL, prefix: string}}
 * @private
 */
let settings = {
  level: LEVEL.OFF,
  prefix: "[CoreMedia]",
};

/**
 * Prints a log to the JavaScript console.
 * @function
 * @param {string} type - type of method to be called on the console object (debug, info, warn, error)
 * @param {object[]} args - a) An array of JavaScript objects which are appended together in the order listed and output.
 * b) A JavaScript string containing zero or more substitution strings followed by JavaScript objects with which
 * to replace substitution strings within the first parameter. This gives you additional control over the format of the output.
 * @private
 */
const print = (type, args) => {
  if (
    type !== "log" &&
    type !== "info" &&
    type !== "warn" &&
    type !== "error"
  ) {
    throw new RangeError(
      'Parameter type must be one of "log", "info", "warn" or "error".'
    );
  }
  // add prefix as first argument to arguments
  args.unshift(settings.prefix);
  try {
    // apply log function to arguments
    console[type](...args);
  } catch (e) {
    // fallback if anything goes wrong
    console[type](args.join(" "));
  }
};

/**
 * Prints a log to the JavaScript console.
 * @function
 * @param {...object} args - a) A list of JavaScript objects which are appended together in the order listed and output.
 * b) A JavaScript string containing zero or more substitution strings followed by JavaScript objects with which
 * to replace substitution strings within the first parameter. This gives you additional control over the format of the output.
 */
export const log = (...args) => {
  if (settings.level <= LEVEL.LOG) {
    print("log", args);
  }
};

/**
 * Prints an informative logging information to the JavaScript console.
 * @function
 * @param {...object} args - a) A list of JavaScript objects which are appended together in the order listed and output.
 * b) A JavaScript string containing zero or more substitution strings followed by JavaScript objects with which
 * to replace substitution strings within the first parameter. This gives you additional control over the format of the output.
 */
export const info = (...args) => {
  if (settings.level <= LEVEL.INFO) {
    print("info", args);
  }
};

/**
 * Prints an warning to the JavaScript console.
 * @function
 * @param {...object} args - a) A list of JavaScript objects which are appended together in the order listed and output.
 * b) A JavaScript string containing zero or more substitution strings followed by JavaScript objects with which
 * to replace substitution strings within the first parameter. This gives you additional control over the format of the output.
 */
export const warn = (...args) => {
  if (settings.level <= LEVEL.WARN) {
    print("warn", args);
  }
};

/**
 * Prints an error to the JavaScript console.
 * @function
 * @param {...object} args - a) A list of JavaScript objects which are appended together in the order listed and output.
 * b) A JavaScript string containing zero or more substitution strings followed by JavaScript objects with which
 * to replace substitution strings within the first parameter. This gives you additional control over the format of the output.
 */
export const error = (...args) => {
  if (settings.level <= LEVEL.ERROR) {
    print("error", args);
  }
};

/**
 * Return name of the current logging level.
 * @function
 * @return {string}
 * @private
 */
export const getCurrentLevelName = () => {
  for (let level in LEVEL) {
    if (LEVEL[level] === settings.level) {
      return `LEVEL.${level}`;
    }
  }
};

/**
 * Sets the logging level.
 * @function
 * @param {LEVEL} level - Level to be set.
 * @return {LEVEL}
 */
export const setLevel = level => {
  if (typeof level !== "number") {
    throw new TypeError("Value of parameter level must be of type number.");
  }
  settings.level = level;
  if (settings.level !== LEVEL.OFF) {
    // call print() instead of log() to print to console even if logging is set to a high level
    print("log", [`Logging level has been set to ${getCurrentLevelName()}`]);
  }

  return settings.level;
};

/**
 * Sets the prefix for all console outputs.
 * @function
 * @param {string} prefix
 * @return {string}
 */
export const setPrefix = prefix => {
  if (typeof prefix !== "string") {
    throw new TypeError("Value of parameter prefix must be of type string.");
  }
  settings.prefix = prefix;
  return settings.prefix;
};

/**
 * Returns the prefix for all console outputs.
 * @function
 * @return {string}
 */
export const getPrefix = () => {
  return settings.prefix;
};
