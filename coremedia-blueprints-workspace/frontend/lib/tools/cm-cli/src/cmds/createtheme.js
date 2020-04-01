"use strict";

const inquirer = require("inquirer");
const path = require("path");
const cmLogger = require("@coremedia/cm-logger");
const {
  workspace: { getWorkspaceConfig, getAvailableBricks, getAvailableThemes },
} = require("@coremedia/tool-utils");
const {
  convertModuleName,
  isModuleNameInUse,
  createTheme,
} = require("@coremedia/module-creator");
const args = require("../lib/args");
const { PKG_NAME } = require("../lib/constants");
const {
  getBrickChoice,
  getThemeChoice,
  sortChoices,
} = require("../lib/output");

const command = "create-theme <name>";
const desc = "Create a new CoreMedia theme";
const builder = yargs =>
  yargs
    .option("verbose", {
      alias: "V",
      default: false,
      describe: "Enable verbose mode for more information output.",
      type: "boolean",
    })
    .epilogue(args.docs);

const handler = ({ name, verbose }) => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: verbose ? "debug" : "info",
  });

  let wsConfig;
  try {
    wsConfig = getWorkspaceConfig();
  } catch (error) {
    log.error(error.message);
    process.exit(1);
  }

  const themeName = convertModuleName(name);
  if (!themeName) {
    log.error(
      "No valid theme name was provided. Only lowercase characters (a-z), numbers (0-9) and hyphens (-) are allowed. Please try again."
    );
    process.exit(1);
  }

  const themePath = path.join(wsConfig.themesPath, `${themeName}-theme`);
  if (isModuleNameInUse(themePath)) {
    log.error(
      `The theme "${themeName}" already exists. Please choose another name.`
    );
    process.exit(1);
  }

  const availableBricks = getAvailableBricks();
  const availableThemes = getAvailableThemes();
  let themeToDeriveFrom = "";
  let dependenciesToActivate = {};
  let dependenciesToCommentOut = availableBricks;

  function doIt() {
    log.info(`Generating new theme "${themeName}".`);
    try {
      createTheme(
        wsConfig,
        themePath,
        themeName,
        dependenciesToActivate,
        dependenciesToCommentOut,
        themeToDeriveFrom,
        log
      );
      log.success(`Done.`);
    } catch (e) {
      log.error(
        `An error occured while trying to create theme "${themeName}": ${e.message}`
      );
    }
  }

  const brickChoices = sortChoices(
    Object.keys(availableBricks).map(brickName =>
      getBrickChoice(brickName, { [brickName]: availableBricks[brickName] })
    )
  );

  const themeChoices = sortChoices(
    Object.keys(availableThemes).map(themeName =>
      getThemeChoice(themeName, { [themeName]: availableThemes[themeName] })
    )
  );

  // Ask for a theme to derive from and bricks to auto activate

  const askForBricks = () => {
    inquirer
      .prompt([
        {
          type: "checkbox",
          pageSize: 20,
          name: "chosenBricks",
          message: "Which bricks should be activated:",
          choices: brickChoices,
          default: [],
        },
        {
          type: "confirm",
          name: "commentOutBricks",
          message:
            "Should non-activated bricks be passed as commented out dependencies?",
          default: false,
        },
      ])
      .then(({ chosenBricks, commentOutBricks }) => {
        let bricksList = chosenBricks.reduce(
          (aggregator, newValue) => ({
            ...aggregator,
            ...newValue,
          }),
          {}
        );
        dependenciesToActivate = Object.assign(
          dependenciesToActivate,
          bricksList
        );
        if (commentOutBricks) {
          dependenciesToCommentOut = Object.keys(availableBricks)
            .filter(brickName => !(brickName in dependenciesToActivate))
            .reduce(
              (aggregator, brickName) => ({
                ...aggregator,
                [brickName]: availableBricks[brickName],
              }),
              {}
            );
        } else {
          dependenciesToCommentOut = {};
        }
        doIt();
      });
  };

  const askForThemes = () => {
    inquirer
      .prompt({
        type: "list",
        pageSize: 20,
        name: "chosenTheme",
        message: "Which theme should be derived from (as parent theme)?",
        choices: themeChoices,
        default: themeChoices[0],
      })
      .then(({ chosenTheme }) => {
        themeToDeriveFrom = Object.keys(chosenTheme)[0];
        dependenciesToActivate = chosenTheme;
        askForBricks();
      });
  };

  inquirer
    .prompt({
      type: "confirm",
      name: "deriveFromTheme",
      message: "Do you want to derive the theme from another theme?",
      default: false,
    })
    .then(({ deriveFromTheme }) => {
      if (deriveFromTheme) {
        askForThemes();
      } else {
        askForBricks();
      }
    });
};

module.exports = {
  command,
  desc,
  builder,
  handler,
};
