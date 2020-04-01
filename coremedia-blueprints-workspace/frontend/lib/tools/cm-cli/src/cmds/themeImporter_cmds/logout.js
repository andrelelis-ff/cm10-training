"use strict";

const cmLogger = require("@coremedia/cm-logger");
const themeImporter = require("@coremedia/theme-importer");

const args = require("../../lib/args");
const { PKG_NAME } = require("../../lib/constants");

const command = "logout";
const desc = "Logout user and delete API key";
const builder = yargs => yargs.epilogue(args.docs);

const handler = () => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: "info",
  });

  themeImporter
    .logout()
    .then(msg => {
      log.success(msg);
    })
    .catch(e => {
      log.error(e);
    });
};

module.exports = {
  command,
  desc,
  builder,
  handler,
};
