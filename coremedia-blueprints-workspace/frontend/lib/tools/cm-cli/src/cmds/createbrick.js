"use strict";

const inquirer = require("inquirer");
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

const command = "create-brick [name]";
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
  let brickName = "";
  let brickPath = "";

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

  const setPath = (brickName) => {
    brickPath = path.join(wsConfig.bricksPath, `${brickName}`);
    if (isModuleNameInUse(brickPath)) {
      log.error(
        `The brick "${brickName}" already exists. Please choose another name.`
      );
      getName();
    } else {
      doIt();
    }
  };

  const getName = () => {
    inquirer
      .prompt([
        {
          type: "string",
          name: "chosenName",
          message: "How should the brick be named?",
        },
      ])
      .then(({ chosenName }) => {
        if (!chosenName) {
          log.error(
            `The theme name must not be empty.`
          );
          getName();
        } else {
          brickName = chosenName;
          setPath(chosenName);
        }
      });
  };

  // starting cli prompts

  brickName = convertModuleName(argv.name);

  if (!brickName) {
    getName();
  } else {
    setPath(brickName);
  }
};

module.exports = {
  command,
  desc,
  builder,
  handler,
};
