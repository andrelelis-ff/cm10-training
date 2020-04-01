"use strict";

const yargs = require("yargs");
const args = require("./lib/args");

const run = () => {
  yargs
    .usage(args.usage)
    .epilogue(args.docs)
    .locale("en")
    .commandDir("cmds")
    .demandCommand(1, "You need at least one command before moving on.")
    .help()
    .alias("help", "h")
    .version()
    .alias("version", "v").argv;
};

module.exports = {
  run,
};
