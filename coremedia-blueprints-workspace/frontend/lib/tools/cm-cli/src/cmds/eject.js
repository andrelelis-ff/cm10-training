"use strict";

const fs = require("fs");
const inquirer = require("inquirer");
const path = require("path");
const cmLogger = require("@coremedia/cm-logger");
const { convertModuleName, ejectBrick } = require("@coremedia/module-creator");
const {
  dependencies: { getFlattenedDependencies },
  workspace: {
    getWorkspaceConfig,
    getAvailableBricks,
    getInstallationPath,
    isExampleBrickModule,
    isExampleModuleName,
  },
} = require("@coremedia/tool-utils");
const { docs } = require("../lib/args");
const { PKG_NAME } = require("../lib/constants");
const { getBrickChoice, sortChoices } = require("../lib/output");

const wsConfig = getWorkspaceConfig();
const BRICKS_PATH = path.join(wsConfig.path, "bricks");

const handler = ({ verbose }) => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: verbose ? "debug" : "info",
  });

  const installationPathByBrickName = Object.keys(getAvailableBricks()).reduce(
    (aggregator, availableBrick) => {
      try {
        aggregator[availableBrick] = getInstallationPath(
          availableBrick,
          wsConfig.path
        );
      } catch (e) {
        log.warn(
          `Ignoring brick "${availableBrick}" as it was not installed. Consider using "yarn install".`
        );
      }
      return aggregator;
    },
    {}
  );

  function getAvailableBrickNames() {
    return Object.keys(installationPathByBrickName);
  }

  function getDependenciesFor(brickName) {
    return getFlattenedDependencies(
      path.join(installationPathByBrickName[brickName], "package.json"),
      nodeModule => getAvailableBrickNames().includes(nodeModule.getName())
    )
      .filter(isExampleBrickModule)
      .map(dependency => dependency.getName());
  }

  /**
   * The flattened list of dependencies by brick name.
   * @type {Object.<String, Array>}
   */
  const flattenedExampleBrickDependenciesByBrickName = getAvailableBrickNames().reduce(
    (aggregator, availableBrick) => ({
      ...aggregator,
      [availableBrick]: getDependenciesFor(availableBrick),
    }),
    {}
  );

  const dependentsByExampleBrickName = {};
  Object.keys(flattenedExampleBrickDependenciesByBrickName).forEach(name => {
    const dependencies = flattenedExampleBrickDependenciesByBrickName[name];
    dependencies.forEach(dependency => {
      dependentsByExampleBrickName[dependency] =
        dependentsByExampleBrickName[dependency] || [];
      dependentsByExampleBrickName[dependency].push(name);
    });
  });

  /**
   * @type {Object.<String, String>}
   * contains
   * bricks to be ejected -> new name is a brick that does not exist in available bricks
   * bricks that have already been ejected -> new name is a brick that does exist in available bricks
   * bricks that should be ignored -> old name = new name (= already been ejected = nothing to do)
   */
  const newNameByOldName = {};

  function doEject() {
    const bricksToEject = Object.keys(newNameByOldName).filter(oldName => {
      const newName = newNameByOldName[oldName];
      return (
        newName !== undefined && !getAvailableBrickNames().includes(newName)
      );
    });

    bricksToEject.forEach(oldName => {
      log.debug(`Ejecting brick "${oldName}"`);
      const newName = newNameByOldName[oldName];
      const oldPath = installationPathByBrickName[oldName];
      const newPath = path.join(BRICKS_PATH, newName);
      ejectBrick(oldPath, newPath, newNameByOldName, log);
      log.info(`Ejected brick "${oldName}" under new name "${newName}"`);
    });

    log.info(
      `Use 'yarn install' to install the new bricks.`,
      `Please note that module imports within JavaScript, SCSS and Freemarker Files have not been transformed and might need to be adjusted accordingly.`
    );
  }

  function askForEjectedName(ejectedBrick) {
    inquirer
      .prompt({
        type: "list",
        pageSize: 20,
        name: `existingBrickName`,
        message: `Please pick the ejected brick ${ejectedBrick} from the list:`,
        choices: sortChoices(
          getAvailableBrickNames()
            .filter(availableBrick => !isExampleModuleName(availableBrick))
            .map(brickName => getBrickChoice(brickName))
        ),
        validate: input => {
          if (!input || input.length === 0) {
            return "You need to select a brick from the list";
          }
          return true;
        },
      })
      .then(({ existingBrickName }) => {
        newNameByOldName[ejectedBrick] = existingBrickName;
        checkInformation();
      });
  }

  function askForDependencyHandling(unhandledDependency) {
    const trackedDependencies = Object.keys(newNameByOldName);
    const dependents = (
      dependentsByExampleBrickName[unhandledDependency] || []
    ).filter(dependent => trackedDependencies.includes(dependent));
    inquirer
      .prompt({
        type: "list",
        name: `howToHandle`,
        message: `The example brick(s): ${dependents.join(
          ", "
        )} have a dependency on the example brick ${unhandledDependency}. How to proceed?`,
        choices: [
          {
            name: "Also eject this brick.",
            value: 1,
          },
          {
            name: "The brick was already ejected (pick from list).",
            value: 2,
          },
          {
            name: "Ignore this brick (dependency will not be changed).",
            value: 3,
          },
        ],
        default: 3,
      })
      .then(({ howToHandle }) => {
        switch (howToHandle) {
          case 1:
            newNameByOldName[unhandledDependency] = undefined;
            checkInformation();
            break;
          case 2:
            askForEjectedName(unhandledDependency);
            break;
          case 3:
          default:
            newNameByOldName[unhandledDependency] = unhandledDependency;
            checkInformation();
            break;
        }
      });
  }

  function askForNewName(brick) {
    inquirer
      .prompt({
        type: "question",
        name: `newName`,
        message: `Please enter a new name for the example brick ${brick} (only lowercase characters (a-z), numbers (0-9) and hyphens (-) are allowed):`,
        validate: newName => {
          if (!newName) {
            return "Name must not be empty";
          }
          if (convertModuleName(newName) !== newName) {
            return "Please enter a valid brick name.";
          }
          if (getAvailableBrickNames().includes(newName)) {
            return "A brick with that name does already exist.";
          }
          if (Object.values(newNameByOldName).includes(newName)) {
            return "You have already used that name for another brick that will be ejected.";
          }
          const newPath = path.join(BRICKS_PATH, newName);
          if (fs.existsSync(newPath)) {
            return `The path "${newPath}" does already exist. Please use a different brick name.`;
          }
          return true;
        },
      })
      .then(({ newName }) => {
        newNameByOldName[brick] = newName;
        checkInformation();
      });
  }

  function checkInformation() {
    const trackedDependencies = Object.keys(newNameByOldName);
    const unhandledEjections = trackedDependencies.filter(
      key => newNameByOldName[key] === undefined
    );

    if (unhandledEjections.length > 0) {
      askForNewName(unhandledEjections.shift());
      return;
    }

    const unhandledDependencies = Object.keys(
      flattenedExampleBrickDependenciesByBrickName
    )
      .filter(dependencyName => trackedDependencies.includes(dependencyName))
      .reduce((aggregator, next) => {
        return [
          ...aggregator,
          ...flattenedExampleBrickDependenciesByBrickName[next],
        ];
      }, [])
      .filter(dependencyName => !trackedDependencies.includes(dependencyName));

    if (unhandledDependencies.length > 0) {
      askForDependencyHandling(unhandledDependencies.shift());
      return;
    }

    // we have all information, performing eject
    doEject();
  }

  function askForBricks() {
    inquirer
      .prompt([
        {
          type: "checkbox",
          pageSize: 20,
          name: "chosenBricks",
          message: "Which example bricks should be ejected:",
          choices: sortChoices(
            getAvailableBrickNames()
              .filter(isExampleModuleName)
              .map(brickName => getBrickChoice(brickName))
          ),
          default: [],
        },
      ])
      .then(({ chosenBricks }) => {
        if (chosenBricks.length === 0) {
          log.success("Nothing to do.");
        } else {
          chosenBricks.forEach(chosenBrick => {
            newNameByOldName[chosenBrick] = undefined;
          });
          checkInformation();
        }
      });
  }

  askForBricks();
};

module.exports = {
  command: "eject",
  desc: "Eject a brick",
  builder: yargs =>
    yargs
      .option("verbose", {
        alias: "V",
        default: false,
        describe: "Enable verbose mode for more information output.",
        type: "boolean",
      })
      .epilogue(docs),
  handler,
};
