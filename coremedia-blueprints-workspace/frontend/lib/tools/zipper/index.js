"use strict";

const path = require("path");
const fs = require("fs");
const archiver = require("archiver");
const cmLogger = require("@coremedia/cm-logger");

const PKG_NAME = "@coremedia/zipper;";
/**
 * Extended Error Class for errors handling env.json or apikey.txt patterns.
 */
class ZipFileError extends Error {
  constructor(...args) {
    super(...args);
    Error.captureStackTrace(this, ZipFileError);
    this.code = "EZIPFILE";
  }
}

const isGlobPattern = path => /\*/.test(path);

const maybeStat = file => fs.existsSync(file) && fs.statSync(file);

/**
 * @typedef {Object} Pattern
 * @property {string} context - Context path of the source
 * @property {string} source - Sources to be included (a glob, a directory or a file)
 * @property {string} prefix - Prefix for the destination path inside the archive
 */
/**
 * @typedef {Object} Options
 * @property {string} filepath - The absolute filepath of the archive.
 * @property {string} context - A path that determines how to interpret the `source` path shared for all patterns. Defaults to process.cwd().
 * @property {string} logLevel - The level used for logging. One of ['log.debug','info','warn','error','silent'].
 */
/**
 * Returns a Promise for creating a zip archive.
 * @param {{Pattern}[]} patterns - An Array of Patterns
 * @param {Options} options
 * @returns {Promise}
 */
const zipper = (patterns, options = {}) => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: (typeof options === "object" && options.logLevel) || "warn",
  });

  return new Promise((resolve, reject) => {
    try {
      if (!Array.isArray(patterns)) {
        throw new Error(`[${PKG_NAME}] patterns must be an array`);
      }
      if (!(options && typeof options.filepath === "string")) {
        throw new Error(
          `[${PKG_NAME}] options must be an object which at least contains property "filepath"`
        );
      }
      if (options.context && typeof options.context !== "string") {
        throw new ZipFileError(
          `[${PKG_NAME}] options.context must be a string`
        );
      }

      // Defaults context tp process.cwd()
      options.context = options.context || process.cwd();

      let fileCount = 0;

      const filedir = path.dirname(options.filepath);
      if (!fs.existsSync(filedir)) {
        fs.mkdirSync(filedir, { recursive: true });
      }

      const output = fs.createWriteStream(options.filepath);
      const archive = archiver("zip");

      output.on("close", () => {
        log.debug(`Compressed ${fileCount} files.`);
        resolve(fileCount);
      });

      archive.on("entry", file => {
        log.debug(
          `Archived ${options.filepath}${path.sep}${path.normalize(file.name)}`
        );
        fileCount++;
      });

      archive.on("error", err => {
        log.debug("Unable to write zip archive:", err);
        throw new ZipFileError(err.message);
      });

      archive.pipe(output);

      patterns.forEach(pattern => {
        pattern.context = pattern.context || options.context;
        if (pattern.context && !path.isAbsolute(pattern.context)) {
          pattern.context = path.join(options.context, pattern.context);
        }
        if (isGlobPattern(pattern.source)) {
          log.debug(`Adding glob to archive: ${pattern.source}`);
          archive.glob(
            pattern.source,
            {
              cwd: pattern.context,
              cache: true,
              statCache: true,
            },
            {
              prefix: pattern.prefix,
            }
          );
        } else {
          const sourcePath = path.resolve(pattern.context, pattern.source);
          const stats = maybeStat(sourcePath);
          if (!stats) {
            log.debug(`Unable to stat path ${sourcePath}`);
            return;
          }
          if (stats.isDirectory()) {
            const name = path.normalize(
              (pattern.prefix ? pattern.prefix + path.sep : "") + pattern.source
            );
            log.debug(`Adding directory to archive: ${sourcePath}`);
            archive.directory(sourcePath, name, {
              stats,
            });
          } else if (stats.isFile()) {
            const name = path.normalize(
              (pattern.prefix ? pattern.prefix + path.sep : "") + pattern.source
            );
            log.debug(`Adding file to archive: ${sourcePath}`);
            archive.file(sourcePath, {
              name,
              stats,
            });
          }
        }
      });

      log.debug("Finalizing archive");
      archive.finalize();
    } catch (e) {
      log.debug("Error: ", e);
      reject(e);
    }
  });
};

module.exports = zipper;
