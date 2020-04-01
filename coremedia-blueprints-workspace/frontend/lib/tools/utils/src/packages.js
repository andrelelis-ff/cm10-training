const closestPackage = require("closest-package");
const fs = require("fs");
const { getInstalledPathSync } = require("get-installed-path");
const path = require("path");

function normalizeFilePath(filePath) {
  const resolvedFilepath = path.resolve(filePath);
  if (!fs.existsSync(resolvedFilepath)) {
    throw new Error(`File not found: '${filePath}'`);
  }
  return fs.realpathSync(resolvedFilepath);
}

/**
 * Finds the installation path of the given module name. Optionally a file path can be provided to indicate where to
 * start look from.
 *
 * @param moduleName
 * @param resolveFromFolder
 * @throws Error in case the installation path could not be found
 */
function resolveFilePathFromPackageName(moduleName, resolveFromFolder) {
  let nodeModulePaths = process.mainModule.paths;
  if (resolveFromFolder) {
    nodeModulePaths = [
      path.join(
        path.dirname(closestPackage.sync(resolveFromFolder)),
        "node_modules"
      ),
    ].concat(nodeModulePaths);
  }
  try {
    return path.join(
      getInstalledPathSync(moduleName, { paths: nodeModulePaths }),
      "package.json"
    );
  } catch (e) {
    // could not find module
    const error = new Error(
      `Could not find installation folder for module '${moduleName}', searched in ${nodeModulePaths}`
    );
    error.stack += "\nCaused by: " + e.stack;
    throw error;
  }
}

class Packages {
  constructor() {
    this._jsonByFilePath = new Map();
    this._filePathByResolveFromByPackageName = new Map();
  }

  getFilePathByPackageName(packageName, resolveFromFolder) {
    resolveFromFolder = normalizeFilePath(resolveFromFolder || process.cwd());
    if (!this._filePathByResolveFromByPackageName.has(resolveFromFolder)) {
      this._filePathByResolveFromByPackageName.set(
        resolveFromFolder,
        new Map()
      );
    }
    const filePathByPackageName = this._filePathByResolveFromByPackageName.get(
      resolveFromFolder
    );

    if (!filePathByPackageName.has(packageName)) {
      const filePath = normalizeFilePath(
        resolveFilePathFromPackageName(packageName, resolveFromFolder)
      );
      filePathByPackageName.set(packageName, filePath);
    }
    return filePathByPackageName.get(packageName);
  }

  getJsonByFilePath(filePath) {
    filePath = normalizeFilePath(filePath);
    if (!this._jsonByFilePath.has(filePath)) {
      const json = JSON.parse(fs.readFileSync(filePath, "utf8"));
      this._jsonByFilePath.set(filePath, json);
      if (json && json.name) {
        const packageName = json.name;
        // check consistency
        if (this._filePathByResolveFromByPackageName.has(packageName)) {
          let cachedFilePath = this._filePathByResolveFromByPackageName.get(
            packageName
          );
          if (cachedFilePath !== filePath) {
            console.warn(
              `Expected package: '${packageName}' to be found in '${cachedFilePath}' but there was another occurence in '${filePath}'`
            );
          }
        } else {
          this._filePathByResolveFromByPackageName.set(packageName, filePath);
        }
      }
    }
    return this._jsonByFilePath.get(filePath);
  }

  getJsonByPackageName(packageName, resolveFromFolder = undefined) {
    const filePath = this.getFilePathByPackageName(
      packageName,
      resolveFromFolder
    );
    if (!filePath) {
      return null;
    }
    return this.getJsonByFilePath(filePath);
  }

  clearCache() {
    this._jsonByFilePath.clear();
    this._filePathByResolveFromByPackageName.clear();
  }
}

module.exports = new Packages();
