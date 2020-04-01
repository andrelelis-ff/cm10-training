"use strict";

const path = require("path");
const cmLogger = require("@coremedia/cm-logger");
const {
  workspace: { getWorkspaceConfig },
} = require("@coremedia/tool-utils");
const zipper = require("@coremedia/zipper");
const { docs } = require("../lib/args");
const { PKG_NAME } = require("../lib/constants");

const wsConfig = getWorkspaceConfig();

const handler = () => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: "info",
  });

  let themesZipTargetPath = path.join(wsConfig.targetPath, "frontend.zip");
  zipper(
    [
      {
        context: wsConfig.descriptorsTargetPath,
        source: "*.xml",
        prefix: path.relative(
          wsConfig.resourcesTargetPath,
          wsConfig.descriptorsTargetPath
        ),
      },
      {
        context: wsConfig.themesTargetPath,
        source: "**/*",
      },
    ],
    {
      filepath: themesZipTargetPath,
    }
  )
    .then(count => {
      log.info(
        `Success, zipped ${count} files to location "${themesZipTargetPath}".`
      );
    })
    .catch(e => {
      throw new Error(
        `[${PKG_NAME}] An error occurred while preparing zip file for themes: ${e.message}`
      );
    });
};

module.exports = {
  command: "build-frontend-zip",
  desc: "Build a single zip file containing all prebuilt themes.",
  builder: yargs => yargs.epilogue(docs),
  handler,
};
