"use strict";

const path = require("path");
const cmLogger = require("@coremedia/cm-logger");
const {
  workspace: { getWorkspaceConfig },
} = require("@coremedia/tool-utils");

const args = require("../lib/args");
const {
  convertModuleName,
  isModuleNameInUse,
  createBrick,
} = require("@coremedia/module-creator");
const { PKG_NAME } = require("../lib/constants");

const command = "create-brick <name>";
const desc = "Create a blank, minimal brick";
const builder = yargs =>
  yargs
    .option("verbose", {
      alias: "V",
      default: false,
      describe: "Enable verbose mode for more information output.",
      type: "boolean",
    })
    .epilogue(args.docs);

const handler = argv => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: argv.verbose ? "debug" : "info",
  });

  let wsConfig;
  try {
    wsConfig = getWorkspaceConfig();
  } catch (error) {
    log.error(error.message);
    process.exit(1);
  }

  const brickName = convertModuleName(argv.name);
  if (!brickName) {
    log.error(
      "No valid brick name was provided. Only characters (A-Z, a-z), numbers (0-9) and hyphens (-) are allowed. Please try again."
    );
    process.exit(1);
  }

  const brickPath = path.join(wsConfig.bricksPath, `${brickName}`);
  if (isModuleNameInUse(brickPath)) {
    log.error(
      `The brick "${brickName}" already exists. Please choose another name.`
    );
    process.exit(1);
  }

  function doIt() {
    log.info(`Generating new brick "${brickName}".`);
    try {
      createBrick(wsConfig, brickPath, brickName, log);
      log.success(`Done.`);
    } catch (e) {
      log.error(
        `An error occured while trying to create brick "${brickName}": ${e.message}`
      );
    }
  }
  doIt();
};

module.exports = {
  command,
  desc,
  builder,
  handler,
};
