"use strict";

const fs = require("fs");
const md5File = require("md5-file");
const path = require("path");
const cmLogger = require("@coremedia/cm-logger");
const { uploadTheme, uploadFiles } = require("@coremedia/theme-importer");
const livereload = require("@coremedia/livereload");
const { packages } = require("@coremedia/tool-utils");

const {
  clearConsole,
  printInfo,
  openBrowser,
  printErrorStack,
} = require("./utils");

const NAME = "CoreMedia Watch Plugin";

class CoreMediaWatchPlugin {
  constructor(options) {
    this._pkgName = "@coremedia/watch-webpack-plugin";

    this.options = typeof options === "object" ? options : {};

    if (
      typeof this.options.themeConfig !== "object" ||
      typeof this.options.themeConfig.name !== "string" ||
      typeof this.options.themeConfig.targetPath !== "string"
    ) {
      throw new Error(
        `[${this._pkgName}] options must be an object which at least contains an object named "themeConfig" with properties "name" and "targetPath"`
      );
    }
    if (
      typeof this.options.monitorConfig !== "object" ||
      typeof this.options.monitorConfig.target !== "string"
    ) {
      throw new Error(
        `[${this._pkgName}] options must be an object which at least contains an object named "monitorConfig" with property "target"`
      );
    }

    this._log = null;
    this._isWatching = false;
    this._cache = {};
  }

  _isCacheEmpty() {
    return Object.keys(this._cache).length === 0;
  }

  _initializeCache(files) {
    this._log.debug("Initializing file cache");
    files.forEach(this._updateCache, this);
  }

  _getCacheEntry(file) {
    return this._cache[file];
  }

  _getAllCacheEntries() {
    return Object.keys(this._cache);
  }

  _updateCache(file) {
    const newMd5Hash = md5File.sync(file);
    this._cache[file] = newMd5Hash;
    return newMd5Hash;
  }

  _removeFromCache(file) {
    delete this._cache[file];
  }

  _wasRemoved(changedFiles = []) {
    const removedFiles = this._getAllCacheEntries()
      .filter(file => !changedFiles.includes(file))
      .filter(file => !fs.existsSync(file));
    removedFiles.forEach(this._removeFromCache, this);
    return removedFiles;
  }

  _hasChanged(file) {
    const oldMd5Hash = this._getCacheEntry(file);
    const newMd5Hash = this._updateCache(file);

    return oldMd5Hash !== newMd5Hash;
  }

  _mapTemplatesToArchive(file, fallback) {
    if (file.includes(this.options.themeConfig.themeTemplatesTargetPath)) {
      return this.options.themeConfig.themeTemplatesJarTargetPath;
    }
    if (file.includes(this.options.themeConfig.brickTemplatesTargetPath)) {
      return this.options.themeConfig.brickTemplatesJarTargetPath;
    }
    return fallback;
  }

  _relativeToThemeFolder(filename) {
    return path.relative(this.options.themeConfig.path, filename);
  }

  _isRemoteWorkflow() {
    return this.options.monitorConfig.target === "remote";
  }

  apply(compiler) {
    const logLevel =
      this.options.logLevel ||
      cmLogger.getLevelFromWebpackStats(compiler.options.stats);
    const logger = cmLogger.getLogger({
      name: this._pkgName,
      level: logLevel,
    });
    this._log = {
      debug: logger.debug,
      info: logger.info,
      error: logger.error,
      finalInfo: (...args) => {
        logger.info(...args);
        logger.info();
        logger.info("Watching...");
      },
      finalError: (...args) => {
        logger.error(...args);
        logger.info();
        logger.info("Watching...");
      },
    };

    let init = true;

    compiler.hooks.watchRun.tapAsync(NAME, (compiler, callback) => {
      this._isWatching = true;
      callback();
    });

    // invalidations
    compiler.hooks.invalid.tap(NAME, () => {
      packages.clearCache();
      if (!init) {
        this._log.info("Detected file changes.");
      }
    });

    compiler.hooks.done.tap(NAME, stats => {
      if (this._isWatching) {
        const assets = stats.compilation.assets;

        // Error handling
        if (stats.compilation.errors.length > 0) {
          const errors = [...new Set(stats.compilation.errors)];
          printErrorStack(errors.join("\n"));
          if (init) {
            // initialization of watch mode: don´t upload theme build and abort watch mode, if there are compilation errors
            this._log.error(
              "Watch mode aborted due to compilation errors during initial theme build. Please fix the errors first and start the watch mode again."
            );
            process.exit(1);
          }
          if (this._isRemoteWorkflow()) {
            // remote workflow: don´t upload file changes, if there are compilation errors
            this._log.finalError(
              "Processing file changes aborted due to compilation errors."
            );
          } else {
            // local workflow: file changes have been processed in target directory, but may contain errors.
            this._log.finalError(
              "The preview may not work properly due to compilation errors."
            );
          }
          return;
        }

        const emittedFiles = Object.keys(assets).map(
          asset => assets[asset].existsAt
        );
        this._log.debug("Emitted files: ", emittedFiles);

        if (this._isCacheEmpty()) {
          this._initializeCache(emittedFiles);
          livereload.init(logLevel);
          if (this._isRemoteWorkflow()) {
            uploadTheme(this.options.themeConfig)
              .then(() => {
                clearConsole();
                printInfo();
                openBrowser();
              })
              .catch(e => {
                this._log.error("Error during theme upload: ", e);
              });
          }
        }

        // determine all files that have really changed
        const changedFiles = emittedFiles.filter(emittedFile =>
          this._hasChanged(emittedFile)
        );

        // determine all files that have been removed
        const removedFiles = this._wasRemoved(changedFiles);

        // split up files that need to be deleted via the theme importer and those which deletion actually causes an
        // update of the template archives (which then is updated instead)
        // noinspection JSUnusedLocalSymbols
        const [
          filesToDelete,
          changedTemplateArchivesFromDeletions,
        ] = removedFiles.reduce(
          (aggregation, next) => {
            const mapping = this._mapTemplatesToArchive(next, null);
            if (mapping) {
              aggregation[1].push(mapping);
            } else {
              aggregation[0].push(next);
            }
            return aggregation;
          },
          [[], []]
        );

        const filesToUpdate = [
          // make sure that every file is only listed once
          ...new Set(
            // map all template files to their template jars but keep other files as they are
            changedFiles
              .map(changedFile =>
                this._mapTemplatesToArchive(changedFile, changedFile)
              )
              // mix in all template archives in which a template has been removed
              .concat(changedTemplateArchivesFromDeletions)
          ),
        ];

        if (filesToUpdate.length > 0) {
          const changedFilesForOutput = filesToUpdate.map(
            this._relativeToThemeFolder,
            this
          );
          if (this._isRemoteWorkflow()) {
            this._log.info("Preparing: ", changedFilesForOutput);
            uploadFiles(this.options.themeConfig, filesToUpdate, logLevel)
              .then(count => {
                this._log.finalInfo(`Uploaded ${count} file(s) to server.`);
                livereload.trigger(filesToUpdate);
              })
              .catch(e => {
                this._log.error("Error during upload of changed files: ", e);
              });
          } else {
            this._log.finalInfo(
              "Added/Changed files processed: ",
              changedFilesForOutput
            );
            livereload.trigger(filesToUpdate);
          }
        } else if (!init) {
          if (this._isRemoteWorkflow()) {
            this._log.finalInfo("No changes, skipping upload.");
          } else {
            this._log.finalInfo("No changes.");
          }
        }

        init = false;
      }
    });
  }
}

module.exports = CoreMediaWatchPlugin;
