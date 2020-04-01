"use strict";

const fs = require("fs");
const glob = require("glob");
const path = require("path");
const { clean, valid } = require("semver");

const themeData = require("./themeData");
const brickData = require("./brickData");
const sharedData = require("./sharedData");

/**
 * @typedef cmLogger Declares the cmLogger pseudo type for code completion
 * @method debug
 * @method info
 * @method log
 * @method warn
 * @method error
 */

/**
 * converts the module name to match restrictions.
 * returns an empty string, if given name was not valid.
 * @param  {string} moduleName
 * @return {string}
 */
const convertModuleName = moduleName => {
  if (typeof moduleName !== "string") {
    return "";
  }
  return moduleName
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9-]/g, "");
};

/**
 * verifies, if the given module path is already in use
 * @param  {string}  modulePath
 * @return {Boolean}
 */
const isModuleNameInUse = modulePath => fs.existsSync(modulePath);

/**
 * Converts a given version to a caret version if it is valid according to semver.valid
 *
 * @param version the version to change to a caret version
 * @returns {string} if a valid version was given the caret version otherwise the given version
 */
const convertVersionToCaretVersion = version => {
  if (!version || !valid(version)) {
    return version;
  }
  version = clean(version);
  return version.indexOf("^") === 0 ? version : `^${version}`;
};

/**
 * Object containing a mapping of dependencyName => dependencyVersion
 * @param dependencies
 */
const convertDependencyVersionsToCaretVersions = dependencies => {
  return Object.keys(dependencies).reduce(function(aggregator, newValue) {
    aggregator[newValue] = convertVersionToCaretVersion(dependencies[newValue]);
    return aggregator;
  }, {});
};

/**
 * creates folder structure of a new theme
 * @param  {object} wsConfig the workspace configuration
 * @param  {string}  themePath
 * @param  {cmLogger} log cm-logger instance
 */
const createThemeFolderStructure = (wsConfig, themePath, log) => {
  const directories = [
    path.join(themePath, "src", "js"),
    path.join(themePath, "src", "sass"),
    path.join(themePath, "src", "sass", "partials"),
    path.join(themePath, "src", "sass", "variables"),
    path.join(themePath, "src", "img"),
    path.join(themePath, "src", "fonts"),
    path.join(themePath, "src", "l10n"),
    path.join(
      themePath,
      "src",
      "templates",
      "com.coremedia.blueprint.common.contentbeans"
    ),
  ];
  directories.forEach(dir => {
    log.debug(
      `Create directory "themes/${path.relative(wsConfig.themesPath, dir)}"`
    );
    fs.mkdirSync(dir, { recursive: true });
  });
};

/**
 * creates folder structure of a new brick
 * @param  {object} wsConfig the workspace configuration
 * @param  {string}  brickPath
 * @param  {cmLogger} log cm-logger instance
 */
const createBrickFolderStructure = (wsConfig, brickPath, log) => {
  const directories = [
    path.join(brickPath, "src", "js"),
    path.join(brickPath, "src", "sass"),
    path.join(brickPath, "src", "sass", "partials"),
    path.join(brickPath, "src", "sass", "variables"),
    path.join(brickPath, "src", "img"),
    path.join(brickPath, "src", "fonts"),
    path.join(brickPath, "src", "l10n"),
    path.join(
      brickPath,
      "src",
      "templates",
      "com.coremedia.blueprint.common.contentbeans"
    ),
  ];
  directories.forEach(dir => {
    log.debug(
      `Create directory "bricks/${path.relative(wsConfig.bricksPath, dir)}"`
    );
    fs.mkdirSync(dir, { recursive: true });
  });
};

/**
 * creates files of a new theme
 * @param  {string}  themePath
 * @param  {string}  themeName
 * @param  {Object} dependenciesToActivate
 * @param  {Object} dependenciesToCommentOut
 * @param  {string} themeToDeriveFrom
 * @param  {cmLogger} log cm-logger instance
 */
const createThemeFiles = (
  themePath,
  themeName,
  dependenciesToActivate,
  dependenciesToCommentOut,
  themeToDeriveFrom,
  log
) => {
  const files = [
    {
      path: "package.json",
      data: themeData.initPackageJson(
        themeName,
        "src/js/index.js",
        convertDependencyVersionsToCaretVersions(dependenciesToActivate),
        convertDependencyVersionsToCaretVersions(dependenciesToCommentOut)
      ),
    },
    {
      path: "webpack.config.js",
      data: themeData.initWebpackConfigJs(themeToDeriveFrom),
    },
    {
      path: `theme.config.json`,
      data: themeData.initThemeConfigJson(
        themeName,
        Object.keys(dependenciesToActivate).length > 0
      ),
    },
    {
      path: ".prettierignore",
      data: themeData.initThemePrettierignore(),
    },
    {
      path: ".prettierrc",
      data: sharedData.initPrettierrc(),
    },
    {
      path: "src/js/index.js",
      data: themeData.initThemeIndexJs(themeName),
    },
    {
      path: `src/js/${themeName}.js`,
      data: themeData.initThemeJs(themeName),
    },
    {
      path: "src/js/preview.js",
      data: themeData.initPreviewJs(themeName),
    },
    {
      path: `src/sass/_partials.scss`,
      data: themeData.initPartialsSass(),
    },
    {
      path: `src/sass/_variables.scss`,
      data: themeData.initVariablesSass(),
    },
    {
      path: `src/sass/${themeName}.scss`,
      data: themeData.initThemeSass(themeName),
    },
    {
      path: "src/sass/preview.scss",
      data: themeData.initPreviewSass(themeName),
    },
    {
      path: `src/l10n/${sharedData.titleCase(themeName)}_en.properties`,
      data: "",
    },
  ];
  files.forEach(file => {
    log.debug(`Create file "${file.path}"`);
    fs.writeFileSync(path.join(themePath, file.path), file.data);
  });
};

/**
 * creates files of a new brick
 * @param  {string}  brickPath
 * @param  {string}  brickName
 * @param  {cmLogger} log cm-logger instance
 */
const createBrickFiles = (brickPath, brickName, log) => {
  log.debug(`Create files`);
  const files = [
    {
      path: "package.json",
      data: brickData.initPackageJson(brickName),
    },
    {
      path: ".prettierignore",
      data: brickData.initBrickPrettierignore(),
    },
    {
      path: ".prettierrc",
      data: sharedData.initPrettierrc(),
    },
    {
      path: "src/js/index.js",
      data: brickData.initBrickIndexJs(brickName),
    },
    {
      path: "src/js/init.js",
      data: brickData.initBrickInitJs(brickName),
    },
    {
      path: `src/js/${brickName}.js`,
      data: brickData.initBrickJs(),
    },
    {
      path: `src/sass/_partials.scss`,
      data: brickData.initBrickPartialsScss(),
    },
    {
      path: `src/sass/_variables.scss`,
      data: brickData.initBrickVariablesScss(),
    },
    {
      path: `src/sass/partials/_custom-text.scss`,
      data: brickData.initBrickCustomTextPartialsScss(),
    },
    {
      path: `src/sass/variables/_custom-text.scss`,
      data: brickData.initBrickCustomTextVariablesScss(),
    },
    {
      path: `src/l10n/${sharedData.titleCase(brickName)}_en.properties`,
      data: brickData.initBrickEnProperties(),
    },
    {
      path: `src/l10n/${sharedData.titleCase(brickName)}_de.properties`,
      data: brickData.initBrickDeProperties(),
    },
    {
      path: `src/templates/com.coremedia.blueprint.common.contentbeans/Page._body.ftl`,
      data: brickData.initBrickPageBodyFtl(),
    },
  ];
  files.forEach(file => {
    log.debug(`Create file "${file.path}"`);
    fs.writeFileSync(path.join(brickPath, file.path), file.data);
  });
};

/**
 * creates folder structure and files of a new theme
 * @param  {Object}  wsConfig
 * @param  {string}  themePath
 * @param  {string}  themeName
 * @param  {Object} dependenciesToActivate
 * @param  {Object} dependenciesToCommentOut
 * @param  {string} themeToDeriveFrom
 * @param  {cmLogger} log cm-logger instance
 */
const createTheme = (
  wsConfig,
  themePath,
  themeName,
  dependenciesToActivate,
  dependenciesToCommentOut,
  themeToDeriveFrom,
  log
) => {
  log.debug(`Creating theme "${path.basename(themePath)}".`);
  createThemeFolderStructure(wsConfig, themePath, log);
  createThemeFiles(
    themePath,
    themeName,
    dependenciesToActivate,
    dependenciesToCommentOut,
    themeToDeriveFrom,
    log
  );
};

/**
 * creates folder structure and files of a new brick
 * @param  {Object}  wsConfig
 * @param  {string}  brickPath
 * @param  {string}  brickName
 * @param  {cmLogger} log cm-logger instance
 */
const createBrick = (wsConfig, brickPath, brickName, log) => {
  log.debug(`Creating brick "${path.basename(brickPath)}".`);
  createBrickFolderStructure(wsConfig, brickPath, log);
  createBrickFiles(brickPath, brickName, log);
};

/**
 * Ejects a brick by copying it to a new location, renaming the package and adjusting dependencies.
 *
 * @param {string} oldPath the old path of a brick.
 * @param {string} newPath the new path of a brick.
 * @param {Object.<string, string>} newNameByOldName a mapping of package names to be replaced (including the brick name itself)
 * @param {cmLogger} log
 */
const ejectBrick = (oldPath, newPath, newNameByOldName, log) => {
  fs.mkdirSync(newPath, { recursive: true });

  /**
   * @type {Array}
   */
  const hits = glob.sync("**", {
    cwd: oldPath,
    nodir: true,
    dot: true,
    ignore: ["node_modules/**"],
  });

  hits.forEach(hit => {
    const sourceFile = path.join(oldPath, hit);
    const targetFile = path.join(newPath, hit);

    fs.mkdirSync(path.dirname(targetFile), { recursive: true });
    if (hit !== "package.json") {
      log.debug(`Copying ${sourceFile} to ${targetFile}`);
      fs.copyFileSync(sourceFile, targetFile);
    } else {
      log.debug(`Copying & transforming ${sourceFile} to ${targetFile}`);
      const packageJson = JSON.parse(fs.readFileSync(sourceFile, "utf8"));
      packageJson.name = newNameByOldName[packageJson.name];
      packageJson.dependencies = packageJson.dependencies || {};
      packageJson.dependencies = Object.keys(packageJson.dependencies).reduce(
        (dependencies, nextDependency) => ({
          ...dependencies,
          [newNameByOldName[nextDependency] || nextDependency]: packageJson
            .dependencies[nextDependency],
        }),
        {}
      );
      fs.writeFileSync(
        targetFile,
        JSON.stringify(packageJson, null, 2),
        "utf8"
      );
    }
  });
};

module.exports = {
  convertModuleName,
  isModuleNameInUse,
  createThemeFolderStructure,
  createThemeFiles,
  createTheme,
  createBrickFolderStructure,
  createBrickFiles,
  createBrick,
  ejectBrick,
};
