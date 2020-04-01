"use strict";

const cmLogger = require("@coremedia/cm-logger");
const themeImporter = require("@coremedia/theme-importer");

const args = require("../../lib/args");
const { PKG_NAME } = require("../../lib/constants");

const command = "whoami";
const desc = "Output info about the logged in user.";
const builder = yargs => yargs.epilogue(args.docs);

const handler = () => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: "info",
  });

  themeImporter
    .whoami()
    .then(user => {
      log.success(`You are logged in as user '${user.name}' (id=${user.id}).`);
    })
    .catch(e => {
      log.error(e.message);
    });
};

module.exports = {
  command,
  desc,
  builder,
  handler,
};
