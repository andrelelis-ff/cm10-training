"use strict";

const path = require("path");
const fs = require("fs");
const glob = require("glob");
const closestPackage = require("closest-package");
const selfsigned = require("selfsigned");
const { getInstalledPathSync } = require("get-installed-path");
const Ajv = require("ajv");
const JSON5 = require("json5");
const { mergeWith } = require("lodash");

const cmLogger = require("@coremedia/cm-logger");

const PKG_NAME = "@coremedia/tool-utils";
const COREMEDIA_SCOPE = "@coremedia";
const EXAMPLE_SCOPE = "@coremedia-examples";
const DEFAULT_CONFIG_PATH = "config";
const DEFAULT_THEMES_PATH = "themes";
const DEFAULT_TARGET_PATH = "target";
const DEFAULT_BRICKS_PATH = "bricks";

const DEFAULT_VARIANT = "default";

const JSON_SCHEMA_ROOT = path.join(__dirname, "json-schemas");
const JSON_SCHEMA_THEME_CONFIG =
  "http://www.coremedia.com/json-schemas/theme.config.schema.json";
const JSON_SCHEMA_SETTINGS =
  "http://www.coremedia.com/json-schemas/settings.schema.json";

const ajv = new Ajv({
  // fill in default values
  useDefaults: true,
  format: "full",
  schemas: glob
    .sync("**/*.schema.json", {
      cwd: JSON_SCHEMA_ROOT,
      absolute: true,
    })
    .map(schemaFile => readJSONFromFilePath(schemaFile)),
});

/**
 * Extended Error Class for errors handling env.json or apikey.txt files.
 */
class ConfigFileError extends Error {
  constructor(...args) {
    super(...args);
    Error.captureStackTrace(this, ConfigFileError);
    this.code = "ECONFIGFILE";
  }
}

/**
 * Extended Error Class for errors handling apikey.txt file.
 */
class ApiKeyFileError extends Error {
  constructor(...args) {
    super(...args);
    Error.captureStackTrace(this, ApiKeyFileError);
    this.code = "EAPIKEYFILE";
  }
}

// Common cm-logger instance for this module
const log = cmLogger.getLogger({
  name: PKG_NAME,
  level: "info",
});

/**
 * @var {Object} a cached the configuration to avoid running the same code multiple times
 */
let wsConfig;

/**
 * @var {Object} a cached the configuration to avoid running the same code multiple times
 */
let themeConfig;

/**
 * @type {Object} a cached mapping of node modules by name and their {@type CoreMediaEntry}. If a key is not added here,
 *                the module has not been checked yet otherwise the value specifies at least an empty object
 */
const cachedPackageJsonsByName = {};

/**
 * Represents configuration options and their defaults for enviroment specific settings.
 * @class Env
 */
class Env {
  constructor(applyFrom = {}) {
    /**
     * @member {String|undefined} the studio (api) url
     */
    this.studioUrl = applyFrom.studioUrl || undefined;

    /**
     * @member {String|undefined} the proxy url
     */
    this.proxyUrl = applyFrom.proxyUrl || undefined;

    /**
     * @member {String|undefined} the studio client url
     */
    this.studioClientUrl = applyFrom.studioClientUrl || undefined;

    /**
     * @member {boolean} indicates of a browser should be opened when starting the monitor
     */
    this.openBrowser = applyFrom.openBrowser !== false;

    /**
     * @member {String|undefined} the url to use for the preview
     */
    this.previewUrl = applyFrom.previewUrl || undefined;

    /**
     * @member {Object|undefined} custom monitor settings
     */
    this.monitor = applyFrom.monitor || undefined;
  }
}

/**
 * Represents a "coremedia"-Entry in the package.json
 * @class CoreMediaEntry
 */
class CoreMediaEntry {
  constructor(applyFrom) {
    applyFrom = applyFrom || {};
    /**
     * @member {string} indicates the type of the CoreMedia package
     */
    this.type = applyFrom.type || null;
    /**
     * @member {string} indicates the initialization script for the CoreMedia package
     */
    this.init = applyFrom.init || null;
    /**
     * @member {Array} indicates in which variants the smart import mechanism will apply
     */
    this.smartImport = applyFrom.smartImport || [DEFAULT_VARIANT];
    /**
     * @member {Object} indicates a mapping for modules to be shimmed
     */
    this.shim = applyFrom.shim || {};
  }
}

/**
 * Represents a module to shim.
 */
class Shim {
  /**
   * Creates a new Shim
   * @param {String} target module path
   */
  constructor(target) {
    this._target = target;
    this._imports = {};
    this._exports = {};
  }

  /**
   * The target module for the shim.
   * @returns {String} the target module for the shim.
   */
  getTarget() {
    return this._target;
  }

  /** Add a new import to the shim
   * @param variableName variable name to be imported from
   * @param moduleName module name to import from
   */
  addImport(variableName, moduleName) {
    this._imports[variableName] = moduleName;
  }

  /**
   * Mapping of all imported variable names to a module path
   * @returns {Object}
   */
  getImports() {
    // immutable
    return { ...this._imports };
  }

  /**
   * Add a new export to the shim
   * @param variableName variable name to be exported into
   * @param targetVariableName variable name of the target module
   */
  addExport(variableName, targetVariableName) {
    this._exports[variableName] = targetVariableName;
  }

  /**
   * Mapping of all exports variable names to a variable names of the target module
   * @returns {Object}
   */
  getExports() {
    // immutable
    return { ...this._exports };
  }
}

/**
 * Generates a css filename for a given entry point name
 * @param entryPointName the name of the entry point
 * @returns {string}
 */
const generateCssFileNameFromEntryPointName = entryPointName => {
  return path.join("css", `${entryPointName}.css`);
};

/**
 * Generates a js filename for a given entry point name
 * @param entryPointName the name of the entry point
 * @returns {string}
 */

const generateJsFileNameFromEntryPointName = entryPointName => {
  return path.join("js", `${entryPointName}.js`);
};

/**
 * Returns the root of the CoreMedia Frontend Workspace
 * @param {String} from where to start from
 * @return {String|null} the path to the CoreMedia Frontend Workspace or null if not found
 */
const getWSPackageJson = from => {
  let cwd = from;

  const check = () => {
    const packageJsonPath = closestPackage.sync(cwd);

    // closestPackage.sync(cwd) returns null, if no frontend package.json has been found by searching upwards from current directory.
    if (!packageJsonPath) {
      return null;
    }

    return getCoreMediaEntryFromPackageJson(packageJsonPath).type ===
      "workspace"
      ? packageJsonPath
      : next();
  };

  const next = () => {
    cwd = path.join(cwd, "..");
    return check();
  };

  return check();
};

/**
 * Returns the config of the frontend workspace.
 *
 * @return {Object} the workspace configuration
 */
const getWorkspaceConfig = () => {
  if (!wsConfig) {
    // search for closest workspace
    const from = process.cwd();
    let packageJsonPath = getWSPackageJson(from);
    // if no workspace is found, check if build was triggered from a theme and use the theme as workspace instead
    if (!packageJsonPath) {
      const fallbackPackageJsonPath = closestPackage.sync(from);
      const coremediaEntry = getCoreMediaEntryFromPackageJson(
        fallbackPackageJsonPath
      );
      if (coremediaEntry.type === "theme") {
        packageJsonPath = fallbackPackageJsonPath;
      }
    }
    if (!packageJsonPath) {
      throw new Error(`No CoreMedia Frontend Workspace or Theme found!`);
    }
    const wsPath = path.dirname(packageJsonPath);
    const configPath = path.join(wsPath, DEFAULT_CONFIG_PATH);
    const envFile = path.join(configPath, "env.json");
    const certFile = path.join(configPath, "livereload.pem");
    const apiKeyFile = path.join(configPath, "apikey.txt");
    const themesPath = path.join(wsPath, DEFAULT_THEMES_PATH);
    const bricksPath = path.join(wsPath, DEFAULT_BRICKS_PATH);
    const targetPath = path.join(wsPath, DEFAULT_TARGET_PATH);
    const resourcesTargetPath = path.join(targetPath, "resources");
    const descriptorsTargetPath = path.join(resourcesTargetPath, "THEME-METADATA");
    const themesTargetPath = path.join(resourcesTargetPath, "themes");

    wsConfig = {
      path: wsPath,
      pkgPath: packageJsonPath,
      configPath,
      envFile,
      certFile,
      apiKeyFile,
      themesPath,
      bricksPath,
      targetPath,
      resourcesTargetPath,
      descriptorsTargetPath,
      themesTargetPath,
    };
  }
  return wsConfig;
};

/**
 * Read JSON5 from file. Encoding is UTF-8.
 *
 * @param filepath {string} path to the JSON file
 * @returns {any} the JSON parsed from the file
 */
function readJSON5FromFilePath(filepath) {
  return JSON5.parse(fs.readFileSync(filepath, "utf8"));
}

/**
 * Read JSON from file. Encoding is UTF-8.
 *
 * @param filepath {string} path to the JSON file
 * @returns {any} the JSON parsed from the file
 * @throws Error if the JSON file could not be read
 */
function readJSONFromFilePath(filepath) {
  return JSON.parse(fs.readFileSync(filepath, "utf8"));
}

/**
 * Searches for $Link keys in a mutable json object and transforms the link via a transformer function.
 * The links will be changed directly in the json object. There is no return value.
 *
 * @param jsonObj {json} the json object
 * @param transformer {function} a transformer function that returns the new link value. Expects first parameter to be the property value
 * @param transformerParams {any} rest parameter, used to pass values into the transformer function
 *
 */
const transformLinksInJson = (jsonObj, transformer, ...transformerParams) => {
  Object.keys(jsonObj).forEach(function(key) {
    let jsonProperty = jsonObj[key];
    if (key === "$Link") {
      // this is a file link in the json, we need to
      jsonObj[key] = transformer(jsonProperty, ...transformerParams);
    } else if (Array.isArray(jsonProperty)) {
      jsonProperty.forEach(element => {
        transformLinksInJson(element, transformer, ...transformerParams);
      });
    } else if (typeof jsonProperty === "object") {
      transformLinksInJson(jsonProperty, transformer, ...transformerParams);
    }
  });
};

/**
 * Validates a given settings json object.
 *
 * @param settingsJson the settings json object
 * @throws Error if the validation was not successful
 */
function validateSettings(settingsJson) {
  const settingsSchema = ajv.getSchema(JSON_SCHEMA_SETTINGS);
  if (!settingsSchema(settingsJson)) {
    throw new Error(
      "Settings are not valid. If you have defined an array make sure that the array is not empty and the type of all items is the same."
    );
  }
}

/**
 * Checks a settings json object for $Link keys, resolves the paths of each key and throws an error if paths are invalid.
 *
 * @param jsonObj {Object} A basefolder to resolve the paths against
 * @param settingsDirPath {string} the path of the settings folder, that contains the relevant settings file
 * @throws {Error} if a path does not exist
 */
function checkPathsInSettingsJson(jsonObj, settingsDirPath) {
  Object.keys(jsonObj).forEach(function(key) {
    let jsonProperty = jsonObj[key];
    if (key === "$Link") {
      // this is a file link in the json, we need to check if the file exists
      let pathToFile = path.join(settingsDirPath, jsonProperty);
      if (!fs.existsSync(pathToFile)) {
        throw new Error(
          `A link in a settings json file is not valid. The following file does not exist: ${pathToFile}`
        );
      }
    } else if (Array.isArray(jsonProperty)) {
      jsonProperty.forEach(element => {
        checkPathsInSettingsJson(element, settingsDirPath);
      });
    } else if (typeof jsonProperty === "object") {
      checkPathsInSettingsJson(jsonProperty, settingsDirPath);
    }
  });
}

/**
 * Parses JSON settings of a given path. If the settings have been already read they can be passed as a second
 * parameter.
 *
 * @param filename {string} the absolute filename of the settings file to be parsed
 * @param settings {string} skip reading the file by passing its contents (optional)
 * @returns {Object} The parsed settings object
 */
function parseSettings(filename, settings = undefined) {
  // create a json object
  let settingsJson;
  try {
    if (settings === undefined) {
      settings = fs.readFileSync(filepath, "utf8");
    }
    settingsJson = JSON.parse(settings);
  } catch (err) {
    log.warn(`The JSON file in ${filename} is invalid:\n${err.message}`);
    settingsJson = {};
  }

  const typeOfSettingsJson = typeof settingsJson;
  if (typeOfSettingsJson !== "object") {
    log.warn(
      `The JSON must be of type "object" but found "${typeOfSettingsJson}"`
    );
    settingsJson = {};
  }

  try {
    validateSettings(settingsJson);
  } catch (e) {
    log.warn(`The settings stored in '${filename}' are invalid:\n${e.message}`);
    settingsJson = {};
  }

  const settingsDirPath = path.dirname(filename);
  // check json for invalid/non-existing filenames
  checkPathsInSettingsJson(settingsJson, settingsDirPath);

  const transformLinksRelativeToSrcPath = relativeFilePath =>
    path.join(settingsDirPath, relativeFilePath);
  transformLinksInJson(settingsJson, transformLinksRelativeToSrcPath);

  return settingsJson;
}

/**
 * Merges an array of settings objects into a single Object. The higher the array index the higher the precedence.
 *
 * @param settings {Array<Object>} the settings objects to be merged
 * @returns {Object} The merge result
 */
function mergeSettings(settings) {
  return settings
    .filter(content => !!content)
    .reduce((mergedSettings, additionalSettings) => {
      const newMergedSettings = mergeWith(
        mergeWith({}, mergedSettings),
        additionalSettings
      );
      try {
        validateSettings(newMergedSettings);
        return newMergedSettings;
      } catch (e) {
        const beforeMerge = JSON.stringify(mergedSettings, null, 2);
        const settingsToMerge = JSON.stringify(additionalSettings, null, 2);
        const afterMerge = JSON.stringify(newMergedSettings, null, 2);
        log.warn(`Merging of settings failed as the result becomes invalid:
Settings (before merge):
${beforeMerge}

Additional settings (to be merged):
${settingsToMerge}

Settings (after merge):
${afterMerge}

Ignoring additional settings. 

${e.message}`);
      }
      return mergedSettings;
    });
}

/**
 * Write JSON to file. Encoding is UTF-8.
 *
 * @param filepath {string} path to the JSON file
 * @param content {any} the JSON to write to the file
 * @param additionalWriteOptions additional options to be passed to fs.writeFileSync
 */
function writeJSONToFilePath(filepath, content, additionalWriteOptions = {}) {
  const data = JSON.stringify(content, null, 2);
  fs.writeFileSync(wsConfig.envFile, data, {
    encoding: "utf8",
    ...additionalWriteOptions,
  });
}

function isExternalLink(ref) {
  return /^(http:|https:)?\/\//.test(ref);
}

function assertConfiguredPathExists(path) {
  if (!fs.existsSync(path)) {
    log.error(
      `Could not find file: '${path}'. Please check your theme configuration.`
    );
    process.exit(-1);
  }
}

/**
 * Complements what JSON schema cannot do.
 *
 * @param themeConfig
 * @param defaultRootPath
 */
function postProcessThemeConfig(themeConfig, defaultRootPath) {
  themeConfig.path = defaultRootPath;
  assertConfiguredPathExists(defaultRootPath);

  if (themeConfig.thumbnail !== null) {
    assertConfiguredPathExists(
      path.join(themeConfig.path, themeConfig.thumbnail)
    );
  }

  // check relative file paths
  themeConfig.styles
    .concat(themeConfig.scripts)
    .filter(item => ["webpack", "copy"].includes(item.type))
    .map(item => item.src)
    .map(src => (src instanceof Array ? src : [src]))
    .reduce((aggregator, srcList) => aggregator.concat(srcList), [])
    .forEach(src =>
      assertConfiguredPathExists(path.join(themeConfig.path, src))
    );

  // fill in missing entry point names
  themeConfig.styles
    .concat(themeConfig.scripts)
    .filter(item => item.type === "webpack" && !item.entryPointName)
    .forEach(webpack => {
      const firstSrc =
        webpack.src instanceof Array ? webpack.src[0] : webpack.src;
      webpack.entryPointName = firstSrc
        ? path.basename(firstSrc).replace(/\.(scss|css|js)$/, "")
        : themeConfig.name;
    });
}

/**
 * Gives the config of the theme the process has been started from.
 *
 * @return {Object} the theme configuration
 */
function getThemeConfig() {
  if (!themeConfig) {
    const wsConfig = getWorkspaceConfig();

    const packageJsonPath = closestPackage.sync(process.cwd());
    const packageRootFolder = path.dirname(packageJsonPath);
    const themeJsonPath = path.join(packageRootFolder, "theme.config.json");
    const themeJson5Path = path.join(packageRootFolder, "theme.config.json5");
    const packageJson = readJSONFromFilePath(packageJsonPath);

    let plainThemeConfig;
    if (fs.existsSync(themeJson5Path)) {
      plainThemeConfig = readJSON5FromFilePath(themeJson5Path);
    } else if (fs.existsSync(themeJsonPath)) {
      plainThemeConfig = readJSONFromFilePath(themeJsonPath);
    } else {
      // packageJson.theme as fallback for backward compatibility
      plainThemeConfig = packageJson.coremedia || packageJson.theme;
    }

    if (!plainThemeConfig) {
      throw new Error(
        `No theme config found in '${themeJson5Path}, '${themeJsonPath}' or '${packageJsonPath}'`
      );
    }

    const themeConfigValidator = ajv.getSchema(JSON_SCHEMA_THEME_CONFIG);
    if (!themeConfigValidator(plainThemeConfig)) {
      const concatinatedErrors = themeConfigValidator.errors
        .map(error => {
          // print out additional property name
          if (error.keyword === "additionalProperties") {
            error.message = `should not have property: ${
              error.params.additionalProperty
            }`;
          }
          return `- ${error.dataPath || "root"}: ${error.message}`;
        })
        .join("\n");
      log.error(
        `The theme configuration is not valid:\n${JSON.stringify(
          plainThemeConfig,
          null,
          2
        )}\n\nThe following errors occured:\n${concatinatedErrors}`
      );
      process.exit(-1);
    }

    postProcessThemeConfig(plainThemeConfig, packageRootFolder);

    const targetPath = plainThemeConfig.targetPath
      ? path.resolve(packageRootFolder, plainThemeConfig.targetPath)
      : wsConfig.targetPath;

    const resourcesTargetPath = wsConfig.resourcesTargetPath;

    const descriptorTargetPath = path.join(
      wsConfig.descriptorsTargetPath,
      `${plainThemeConfig.name}-theme.xml`
    );

    const themeTargetPath = path.join(
      wsConfig.themesTargetPath,
      plainThemeConfig.name
    );

    const templatesTargetPath = path.join(
      resourcesTargetPath,
      "WEB-INF/templates"
    );

    const brickTemplatesTargetPath = path.join(templatesTargetPath, "bricks");

    const themeTemplatesTargetPath = path.join(
      templatesTargetPath,
      plainThemeConfig.name
    );

    const themeTemplatesJarTargetPath = path.join(
      themeTargetPath,
      `templates/${plainThemeConfig.name}-templates.jar`
    );

    const brickTemplatesJarTargetPath = path.join(
      themeTargetPath,
      "templates/bricks-templates.jar"
    );

    const resourceBundleTargetPath = path.join(themeTargetPath, "l10n");
    const settingsTargetPath = path.join(themeTargetPath, "settings");

    const themeContentTargetPath = path.resolve(
      targetPath,
      "content/Themes",
      plainThemeConfig.name
    );

    const themeArchiveTargetPath = path.resolve(
      targetPath,
      `themes/${plainThemeConfig.name}-theme.zip`
    );

    const themeUpdateArchiveTargetPath = path.resolve(
      targetPath,
      `themes/${plainThemeConfig.name}-update.zip`
    );

    themeConfig = {
      packageName: packageJson.name,
      pkgPath: packageJsonPath,
      targetPath,
      resourcesTargetPath,
      descriptorTargetPath,
      themeTargetPath,
      templatesTargetPath,
      brickTemplatesTargetPath,
      themeTemplatesTargetPath,
      themeTemplatesJarTargetPath,
      brickTemplatesJarTargetPath,
      resourceBundleTargetPath,
      settingsTargetPath,
      themeContentTargetPath,
      themeArchiveTargetPath,
      themeUpdateArchiveTargetPath,
      ...plainThemeConfig,
    };
  }
  return themeConfig;
}

/**
 * @param {String} packageJsonPath
 * @returns {CoreMediaEntry}
 */
function getCoreMediaEntryFromPackageJson(packageJsonPath) {
  const packageJson = readJSONFromFilePath(packageJsonPath);
  return new CoreMediaEntry(packageJson.coremedia);
}

/**
 * @param nodeModule {NodeModule}
 * @returns {CoreMediaEntry}
 */
function getCoreMediaEntry(nodeModule) {
  const moduleName = nodeModule.getName();
  if (!(moduleName in cachedPackageJsonsByName)) {
    cachedPackageJsonsByName[moduleName] = getCoreMediaEntryFromPackageJson(
      nodeModule.getPkgPath()
    );
  }
  return cachedPackageJsonsByName[moduleName];
}

function getPackageType(nodeModule) {
  return getCoreMediaEntry(nodeModule).type;
}

function getSmartImportType(nodeModule) {
  return getCoreMediaEntry(nodeModule).smartImport;
}

/**
 * Returns an absolute path to the initialization script of the package if defined.
 *
 * @param nodeModule The module
 * @returns {String} if an JavaScript for initialization is given, return absolute path, otherwise NULL
 */
function getInitJs(nodeModule) {
  const init = getCoreMediaEntry(nodeModule).init;
  if (!init) {
    return null;
  }
  const pkgPath = nodeModule.getPkgPath();
  const pkgDir = path.dirname(pkgPath);
  return path.resolve(pkgDir, init);
}

function absoluteRequire(require, context) {
  // check if relative path is provided
  if (require.length > 0 && require[0] === ".") {
    return path.join(context, require);
  }
  // otherwise just return the require statement as module requires are already absolute
  return require;
}

/**
 * Parses a {@link Shim} from a given config {@link Object}.
 * @param {String} target the target
 * @param {Object} config the config to parse from
 * @param context the context to resolved from
 */
function parseShim(target, config, context) {
  const shim = new Shim(absoluteRequire(target, context));

  Object.keys(config.imports || {}).forEach(name => {
    const module = config.imports[name];
    shim.addImport(name, absoluteRequire(module, context));
  });

  Object.keys(config.exports || {}).forEach(name => {
    shim.addExport(name, config.exports[name]);
  });

  return shim;
}

/**
 * Returns the defined shims of a given module.
 *
 * @param {NodeModule} nodeModule The module
 * @returns {Array<Shim>} the defined shims
 */
function getShims(nodeModule) {
  const shimConfigByTarget = getCoreMediaEntry(nodeModule).shim;
  return Object.keys(shimConfigByTarget).map(target =>
    parseShim(
      target,
      shimConfigByTarget[target],
      path.dirname(nodeModule.getPkgPath())
    )
  );
}

/**
 * Checks if the given node module is a brick module
 * @param nodeModule {NodeModule} the module
 * @return {boolean}
 */
function isBrickModule(nodeModule) {
  return getPackageType(nodeModule) === "brick";
}

/**
 * Checks if the given node module is an example brick module
 * @param nodeModule {NodeModule} the module
 * @return {boolean}
 */
function isExampleBrickModule(nodeModule) {
  return isBrickModule(nodeModule) && isExampleModuleName(nodeModule.getName());
}

/**
 * Checks if the given module name is an example module name.
 * @param name
 * @returns {boolean}
 */
function isExampleModuleName(name) {
  return name && name.startsWith(EXAMPLE_SCOPE);
}

/**
 * Checks if the given node module is a theme module
 * @param nodeModule {NodeModule} the module
 * @return {boolean}
 */
function isThemeModule(nodeModule) {
  return getPackageType(nodeModule) === "theme";
}

/**
 * Checks if the given node module is an example brick module
 * @param nodeModule {NodeModule} the module
 * @return {boolean}
 */
function isExampleThemeModule(nodeModule) {
  return isThemeModule(nodeModule) && isExampleModuleName(nodeModule.getName());
}

/**
 * Checks if the given node module is a lib module
 * @param nodeModule {NodeModule} the module
 * @return {boolean}
 */
function isLibraryModule(nodeModule) {
  return getPackageType(nodeModule) === "lib";
}

/**
 * Checks if the given node module is meant to be smart imported from this variant. If variant is set to null it will
 * ignore the variant check.
 *
 * @param {String} variant the variant to check (if null variant check will be ignored, defaults to {@link DEFAULT_VARIANT}).
 * @returns {function(NodeModule=): boolean}
 */
function getIsSmartImportModuleFor(variant = DEFAULT_VARIANT) {
  return nodeModule =>
    (isBrickModule(nodeModule) ||
      isLibraryModule(nodeModule) ||
      isThemeModule(nodeModule)) &&
    (!variant || getSmartImportType(nodeModule).includes(variant));
}

/**
 * Finds the installation path of the given module name. Optionally a file path can be provided to indicate where to
 * start look from.
 *
 * @param moduleName
 * @param relativeFrom
 * @throws Error in case the installation path could not be found
 */
function getInstallationPath(moduleName, relativeFrom) {
  let nodeModulePaths = process.mainModule.paths;
  if (relativeFrom) {
    nodeModulePaths = [
      path.join(
        path.dirname(closestPackage.sync(relativeFrom)),
        "node_modules"
      ),
    ].concat(nodeModulePaths);
  }
  try {
    return getInstalledPathSync(moduleName, { paths: nodeModulePaths });
  } catch (e) {
    // could not find module
    throw new Error(
      `Could not find installation folder for module '${moduleName}', searched in ${nodeModulePaths}`
    );
  }
}

/**
 * Collects all available modules with in the frontend workspace. Expects the module type (theme, brick,...) as parameter.
 *
 * @param {String} moduleType of the module
 * @returns {Object} an object containing the name of the package as key and the version as value
 */
function getAvailableModules(moduleType) {
  if (typeof moduleType !== "string") {
    return [];
  }
  const wsConfig = getWorkspaceConfig();
  const wsPatterns = readJSONFromFilePath(wsConfig.pkgPath).workspaces || [];
  const wsDirectories = wsPatterns
    .map(wsPattern =>
      glob.sync(wsPattern, {
        cwd: wsConfig.path,
      })
    )
    .reduce((all, newValue) => all.concat(newValue), []);

  const packageJsonPaths = wsDirectories
    .map(directory => path.join(wsConfig.path, directory, "package.json"))
    .filter(fs.existsSync)
    .filter(packageJsonPath => {
      const packageJson = readJSONFromFilePath(packageJsonPath);
      return packageJson.coremedia && packageJson.coremedia.type === moduleType;
    });

  return packageJsonPaths
    .map(packageJsonPath => {
      const packageJson = readJSONFromFilePath(packageJsonPath);
      return {
        [packageJson.name]: `^${packageJson.version}`,
      };
    })
    .reduce(
      (aggregator, newValue) => ({
        ...aggregator,
        ...newValue,
      }),
      {}
    );
}

/**
 * Collects all available bricks in the frontend workspace.
 *
 * @returns {Object<String, String>} an object containing the name of the package as key and the version as value
 */
function getAvailableBricks() {
  return getAvailableModules("brick");
}

/**
 * Collects all available themes in the frontend workspace.
 *
 * @returns {Object} an object containing the name of the package as key and the version as value
 */
function getAvailableThemes() {
  return getAvailableModules("theme");
}

/**
 * Return config for monitor script.
 * @returns {Object}
 * @private
 */
const getMonitorConfig = () => {
  const monitorConfig = {
    livereload: {},
  };
  const defaultMonitorConfig = {
    target: "remote",
    livereload: {
      host: "localhost",
      port: 35729,
    },
  };

  let customMonitorConfig;
  try {
    customMonitorConfig = getEnv().monitor;
  } catch (e) {
    // no custom monitor config, use default config
  }

  if (typeof customMonitorConfig !== "object") {
    Object.assign(monitorConfig, defaultMonitorConfig);
  } else {
    // Check property target
    if (typeof customMonitorConfig.target !== "string") {
      Object.assign(monitorConfig, {
        target: defaultMonitorConfig.target,
      });
    } else if (
      customMonitorConfig.target !== "local" &&
      customMonitorConfig.target !== "remote"
    ) {
      throw new Error(
        'Property monitor.target must be either "remote" or "local". Please check env.json in config directory in root of frontend workspace.'
      );
    } else {
      Object.assign(monitorConfig, {
        target: customMonitorConfig.target,
      });
    }

    // Check property livereload
    if (typeof customMonitorConfig.livereload !== "object") {
      Object.assign(monitorConfig, {
        livereload: defaultMonitorConfig.livereload,
      });
    } else {
      // Check property livereload.host
      if (typeof customMonitorConfig.livereload.host !== "string") {
        Object.assign(monitorConfig.livereload, {
          host: defaultMonitorConfig.livereload.host,
        });
      } else {
        Object.assign(monitorConfig.livereload, {
          host: customMonitorConfig.livereload.host,
        });
      }

      // Check property livereload.port
      if (typeof customMonitorConfig.livereload.port !== "number") {
        Object.assign(monitorConfig.livereload, {
          port: defaultMonitorConfig.livereload.port,
        });
      } else if (
        customMonitorConfig.livereload.port < 1024 ||
        customMonitorConfig.livereload.port > 49151
      ) {
        throw new Error(
          "Property monitor.livereload.port must be a number between 1024 and 49151. Please check env.json in config directory in root of frontend workspace."
        );
      } else {
        Object.assign(monitorConfig.livereload, {
          port: customMonitorConfig.livereload.port,
        });
      }
    }

    // Add certificate to property livereload
    Object.assign(monitorConfig.livereload, {
      key: defaultMonitorConfig.livereload.key,
      cert: defaultMonitorConfig.livereload.cert,
    });
  }
  return monitorConfig;
};

/**
 * Write variables into env.json
 * @param {Object} vars
 */
const createEnvFile = vars => {
  try {
    const wsConfig = getWorkspaceConfig();
    let env = {};
    if (!fs.existsSync(wsConfig.configPath)) {
      fs.mkdirSync(wsConfig.configPath);
    } else if (fs.existsSync(wsConfig.envFile)) {
      env = readJSONFromFilePath(wsConfig.envFile, "utf8");
    }
    env = Object.assign(env, vars);
    writeJSONToFilePath(wsConfig.envFile, env, {
      mode: 0o600,
    });
  } catch (e) {
    throw new ConfigFileError(
      `An error occured while trying to store the environment variables: ${
        e.message
      }`
    );
  }
};

/**
 * Returns the current enviroment configuration
 * @return {Env} the current enviroment configuration
 */
const getEnv = () => {
  const wsConfig = getWorkspaceConfig();
  if (!fs.existsSync(wsConfig.envFile)) {
    throw new ConfigFileError("No environment file found. Please login.");
  }
  try {
    return new Env(readJSONFromFilePath(wsConfig.envFile, "utf8"));
  } catch (e) {
    throw new ConfigFileError("The environment file couldn´t be read.");
  }
};

/**
 * Sets the enviroment configuration
 * @param {Env} env the enviroment configuration
 */
const setEnv = env => {
  createEnvFile(env);
};

/**
 * Checks, if certificate for the LiveReload server is expired
 * @returns {Boolean}
 * @private
 */
const isCertExpired = certFile => {
  const certStat = new Date(fs.statSync(certFile).mtime);
  const certExpiration = 1000 * 60 * 60 * 24 * 30;
  const now = new Date();

  // if certificate is older than 30 days, delete it to create a new one.
  if (certStat < now - certExpiration) {
    fs.unlinkSync(certFile);
    return true;
  }
  return false;
};

/**
 * Creates certificate for the LiveReload server
 * @private
 */
const createCertFile = (configPath, certFile) => {
  try {
    const attributes = [{ name: "commonName", value: "localhost" }];
    const pems = selfsigned.generate(attributes, {
      algorithm: "sha256",
      days: 30,
      keySize: 2048,
      extensions: [
        { name: "subjectAltName", altNames: [{ type: 6, value: "localhost" }] },
      ],
    });

    if (!fs.existsSync(configPath)) {
      fs.mkdirSync(configPath);
    }
    fs.writeFileSync(certFile, pems.private + pems.cert, {
      encoding: "utf-8",
    });
  } catch (e) {
    log.error(
      `An error occured while trying to store the certificate for the LiveReload server: ${
        e.message
      }`
    );
  }
};

/**
 * Return certificate for LiveReload server
 * @returns {string}
 * @private
 */
const getCert = () => {
  const wsConfig = getWorkspaceConfig();

  if (!fs.existsSync(wsConfig.certFile) || isCertExpired(wsConfig.certFile)) {
    createCertFile(wsConfig.configPath, wsConfig.certFile);
  }
  let cert;
  try {
    cert = fs.readFileSync(wsConfig.certFile);
  } catch (error) {
    // cert file does not exist
  }
  return cert;
};

/**
 * Write file apikey.txt
 * @param {string} apiKey
 */
const createApiKeyFile = apiKey => {
  try {
    const wsConfig = getWorkspaceConfig();

    if (!fs.existsSync(wsConfig.configPath)) {
      fs.mkdirSync(wsConfig.configPath);
    }
    fs.writeFileSync(wsConfig.apiKeyFile, apiKey, {
      encoding: "utf8",
      mode: 0o600,
    });
  } catch (e) {
    throw new ApiKeyFileError(
      `An error occured while trying to store the API key: ${e.message}`
    );
  }
};

/**
 * remove file apikey.txt
 */
const removeApiKeyFile = () => {
  try {
    const wsConfig = getWorkspaceConfig();

    fs.unlinkSync(wsConfig.apiKeyFile);
  } catch (e) {
    // apikey.txt couldn´t be deleted
  }
};

/**
 * Returns content of apikey.txt
 * @return {string} apiKey
 */
const getApiKey = () => {
  const wsConfig = getWorkspaceConfig();

  if (!fs.existsSync(wsConfig.apiKeyFile)) {
    throw new ApiKeyFileError("No API key found. Please login.");
  }
  const apiKey = fs.readFileSync(wsConfig.apiKeyFile, "utf8");
  if (typeof apiKey !== "string" || apiKey.length === 0) {
    throw new ApiKeyFileError("No API key found. Please login.");
  }
  return apiKey;
};

module.exports = {
  PKG_NAME,
  COREMEDIA_SCOPE,
  EXAMPLE_SCOPE,
  DEFAULT_VARIANT,
  generateCssFileNameFromEntryPointName,
  generateJsFileNameFromEntryPointName,
  getWorkspaceConfig,
  transformLinksInJson,
  parseSettings,
  mergeSettings,
  getThemeConfig,
  isExampleModuleName,
  isBrickModule,
  isExampleBrickModule,
  isThemeModule,
  isExampleThemeModule,
  getInitJs,
  getShims,
  getIsSmartImportModuleFor,
  getInstallationPath,
  getAvailableBricks,
  getAvailableThemes,
  getMonitorConfig,
  Env,
  getEnv,
  setEnv,
  getCert,
  createApiKeyFile,
  removeApiKeyFile,
  getApiKey,
};
