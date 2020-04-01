"use strict";

const path = require("path");
const cmLogger = require("@coremedia/cm-logger");
const zipper = require("@coremedia/zipper");

const NAME = "CoreMedia Zipper Webpack Plugin";

const compilerEvents = {
  AFTER_EMIT: "after-emit",
  DONE: "done",
};

const hookByCompilerEvent = {
  [compilerEvents.AFTER_EMIT]: "afterEmit",
  [compilerEvents.DONE]: "done",
};

class ZipperWebpackPlugin {
  constructor(patterns, options) {
    this._pkgName = "@coremedia/zipper-webpack-plugin";
    this.patterns = patterns;
    this.options = typeof options === "object" ? options : {};

    if (!Array.isArray(this.patterns)) {
      throw new Error(`[${this._pkgName}] patterns must be an array`);
    }
    if (
      !(
        Object.values(compilerEvents).includes(this.options.compilerEvent) &&
        typeof this.options.filepath === "string"
      )
    ) {
      throw new Error(
        `[${
          this._pkgName
        }] options must be an object which at least contains properties "filepath" and "compilerEvent" (valid values: ${Object.values(
          compilerEvents
        )
          .map(event => `"${event}"`)
          .join(", ")})`
      );
    }

    this._log = null;
    this._logLevel = null;
  }

  _getEventHandler(compilerEvent) {
    switch (compilerEvent) {
      case compilerEvents.AFTER_EMIT:
        return (compilation, cb) => this._handleEvent(compilation, cb);
      case compilerEvents.DONE:
        return stats => this._handleEvent(stats.compilation);
      default:
        return () => {};
    }
  }

  _handleEvent(compilation, cb) {
    this._log.debug(`Starting  ${this.options.compilerEvent}`);
    const callback = () => {
      this._log.debug(`Finishing  ${this.options.compilerEvent}`);
      cb && cb();
    };

    this._log.debug(`Creating archive ${this.options.filepath}`);
    zipper(this.patterns, {
      filepath: this.options.filepath,
      context: this.context,
      debug: this._logLevel,
    })
      .then(count => {
        this._log.debug(`Compressed ${count} files`);
        this._log.info(`Created archive ${this.options.filepath}`);
        callback();
      })
      .catch(error => {
        compilation.errors.push(
          new Error(`[${this._pkgName}] ${error.message}`)
        );
      });
  }

  apply(compiler) {
    this._logLevel =
      this.options.logLevel ||
      cmLogger.getLevelFromWebpackStats(compiler.options.stats);
    this._log = cmLogger.getLogger({
      name: this._pkgName,
      level: this._logLevel,
    });

    this.context = this.options.context || compiler.options.output.path;

    if (!path.isAbsolute(this.context)) {
      this.context = path.resolve(this.context);
    }

    if (!path.isAbsolute(this.options.filepath)) {
      this.options.filepath = path.join(this.context, this.options.filepath);
    }

    compiler.hooks[hookByCompilerEvent[this.options.compilerEvent]].tapAsync(
      NAME,
      this._getEventHandler(this.options.compilerEvent)
    );
  }
}

module.exports = { ZipperWebpackPlugin };
