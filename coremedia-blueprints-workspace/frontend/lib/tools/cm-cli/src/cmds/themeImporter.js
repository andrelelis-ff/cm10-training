"use strict";

const args = require("../lib/args");

const command = "theme-importer <command>";
const desc = "Remote file operations of themes";
const builder = yargs =>
  yargs
    .commandDir("themeImporter_cmds")
    .demandCommand(1, "You need at least one command before moving on.")
    .epilogue(args.docs);

const handler = () => {};

module.exports = {
  command,
  desc,
  builder,
  handler,
};
