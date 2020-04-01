"use strict";

const path = require("path");
const { spawnSync } = require("child_process");
const cmLogger = require("@coremedia/cm-logger");
const themeImporter = require("@coremedia/theme-importer");
const {
  workspace: { getThemeConfig },
} = require("@coremedia/tool-utils");

const args = require("../../lib/args");
const { PKG_NAME } = require("../../lib/constants");
const { PACKAGE_MANAGER_EXECUTABLE } = require("../../lib/paths");

const command = "deploy-theme";
const desc = "Upload and deploy theme on server";
const builder = yargs => yargs.epilogue(args.docs);

const handler = () => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: "info",
  });

  const runLoginCmd = () => {
    const args = ["run", "theme-importer", "login", "--deploy"];
    const result = spawnSync(PACKAGE_MANAGER_EXECUTABLE, args, {
      cwd: process.cwd(),
      env: Object.assign(process.env, { NODE_ENV: "production" }),
      stdio: "inherit",
    });
    if (result.status !== 0) {
      process.exit(1);
    }
  };

  try {
    const themeConfig = getThemeConfig();

    log.info("Creating a fresh production build of the current theme.");
    const result = spawnSync(PACKAGE_MANAGER_EXECUTABLE, ["run", "build"], {
      cwd: process.cwd(),
      env: Object.assign(process.env, { NODE_ENV: "production" }),
      stdio: "inherit",
    });
    if (result.status !== 0) {
      log.error(
        "\n" + `Theme build failed due to listed errors.\n${result.error}`
      );
      process.exit(1);
    }

    themeImporter
      .whoami()
      .then(user => {
        log.info(`You are logged in as user '${user.name}' (id=${user.id}).`);
        themeImporter
          .deployTheme(themeConfig)
          .then(zipFile => {
            log.success(
              `Theme has successfully been uploaded and deployed (file ${path.basename(
                zipFile
              )}).`
            );
          })
          .catch(e => {
            log.error(e.message);
            process.exit(1);
          });
      })
      .catch(e => {
        log.error(e.message);
        runLoginCmd();
        themeImporter
          .deployTheme(themeConfig)
          .then(zipFile => {
            log.success(
              `Theme has successfully been uploaded and deployed (file ${path.basename(
                zipFile
              )}).`
            );
          })
          .catch(e => {
            log.error(e.message);
            process.exit(1);
          });
      });
  } catch (e) {
    log.error(e.message);
    process.exit(1);
  }
};

module.exports = {
  command,
  desc,
  builder,
  handler,
};
