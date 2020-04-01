"use strict";

const chalk = require("chalk");
const open = require("open");
const log = require("@coremedia/cm-logger");
const {
  workspace: { getEnv },
} = require("@coremedia/tool-utils");
const livereload = require("@coremedia/livereload");

const clearConsole = () => {
  process.stdout.write(
    process.platform === "win32" ? "\x1Bc" : "\x1B[2J\x1B[3J\x1B[H"
  );
};

const printInfo = () => {
  const livereloadURL = livereload.getHost();

  log.info(chalk.green.bold("Theme has been synchronized with remote CAE!"));
  log.info();
  log.info(
    chalk.bold(
      "Subsequent changes will automatically be transferred to the remote CAE"
    )
  );
  log.info(chalk.bold("and be available in preview using the developer mode."));
  log.info(
    "To instantly reload your changes in the browser, you may need to accept"
  );
  log.info(
    `the certificate for the local LiveReload server (https://${livereloadURL}) first.`
  );
  log.info();

  try {
    const { studioUrl, previewUrl } = getEnv();

    log.info(`Studio URL: ${studioUrl}`);
    if (previewUrl) {
      log.info(`Preview URL: ${previewUrl}`);
    }
    log.info();
    log.info("Watching...");
  } catch (e) {
    // env file does not exist yet.
  }
};

const openBrowser = () => {
  try {
    const { studioClientUrl, previewUrl, openBrowser } = getEnv();
    if (openBrowser) {
      const url = previewUrl || studioClientUrl;
      open(url).catch(() => {}); // Prevent `unhandledRejection` error.
    }
  } catch (err) {
    // do nothing
  }
};

const printErrorStack = error => {
  log.error(chalk.red(error));
};

module.exports = { clearConsole, printInfo, openBrowser, printErrorStack };
