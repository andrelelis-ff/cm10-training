const closestPackage = require("closest-package");
const deepmerge = require("deepmerge");
const nodeSass = require("node-sass");
const path = require("path");
const semver = require("semver");

const { packages } = require("@coremedia/tool-utils");

const NAME = "CoreMedia Dependency Check Plugin";

function getPkgName(file) {
  const pkg = packages.getJsonByFilePath(closestPackage.sync(file));
  return pkg && pkg.name;
}

function getPkgVersion(file) {
  const pkg = packages.getJsonByFilePath(closestPackage.sync(file));
  return pkg && pkg.version;
}

function getDependenciesByName(file) {
  const pkg = packages.getJsonByFilePath(closestPackage.sync(file));

  const pkgDependencies = [];
  if (pkg.dependencies) {
    for (let pkgDependency of Object.keys(pkg.dependencies)) {
      pkgDependencies.push(pkgDependency);
    }
  }
  return pkgDependencies;
}

function hasPkgDependency(sourceFile, requiredFile) {
  const sourcePkgName = getPkgName(sourceFile);
  const requiredPkgName = getPkgName(requiredFile);

  // ignoring babel-runtime here...consider plugging into another phase where the runtime is not yet attached
  if (
    sourcePkgName === requiredPkgName ||
    ["babel-runtime", "@babel/runtime"].includes(requiredPkgName)
  ) {
    return true;
  }

  return getDependenciesByName(sourceFile).indexOf(requiredPkgName) > -1;
}

function toArray(o) {
  if (o instanceof Array) {
    return o;
  }
  if (o) {
    return [o];
  }
  return [];
}

function getMissingDependencyErrorMessage(sourceFile, requiredFile) {
  const sourcePackageName = getPkgName(sourceFile);
  const requiredPackageName = getPkgName(requiredFile);
  let requiredPackageVersion = getPkgVersion(requiredFile);

  if (semver.valid(requiredPackageVersion)) {
    // our new pattern is to only use the major version and let the yarn.lock decide, reflect this in the suggestion
    requiredPackageVersion = `^${semver.major(requiredPackageVersion)}.0.0`;
  }
  const sourcePackageJsonPath = closestPackage.sync(sourceFile);

  return `'${sourceFile}'
By using '${requiredFile}' the package '${sourcePackageName}' has a dependency to '${requiredPackageName}'.

Please add the following dependency to '${sourcePackageJsonPath}':
  ...
  "dependencies": {
    ...
    "${requiredPackageName}": "${requiredPackageVersion}",
    ...
  }
  ...\n`;
}

class MissingDependencyError extends Error {
  constructor(...args) {
    super(...args);
    Error.captureStackTrace(this, MissingDependencyError);
    this.code = "EDEPENDENCYCHECK";
  }
}

class DependencyCheckWebpackPlugin {
  constructor(options) {
    this.options = deepmerge(
      { include: undefined, exclude: undefined },
      options
    );
    this.includes = toArray(this.options.include);
    this.excludes = toArray(this.options.exclude);

    this.dependencies = [];
  }

  apply(compiler) {
    const plugin = this;

    compiler.hooks.done.tap(NAME, function(stats) {
      const sourceModules = stats.compilation.modules;

      for (let sourceModule of sourceModules) {
        const sourceFile = sourceModule && sourceModule.resource;
        const alreadyTrackedResources = [];
        if (sourceFile) {
          for (let dependency of sourceModule.dependencies) {
            const requiredModule = dependency.module;
            const requiredFile = requiredModule && requiredModule.resource;
            if (
              requiredFile &&
              !alreadyTrackedResources.includes(requiredFile)
            ) {
              alreadyTrackedResources.push(requiredFile);
              plugin.dependencies.push({
                sourceFile,
                requiredFile,
              });
            }
          }
        }
      }

      const unmanagedDependencies = plugin.getUnmanagedDependencies();
      for (let unmanagedDependency of unmanagedDependencies) {
        if (plugin.isIncluded(unmanagedDependency.sourceFile)) {
          stats.compilation.errors.push(
            new MissingDependencyError(
              getMissingDependencyErrorMessage(
                unmanagedDependency.sourceFile,
                unmanagedDependency.requiredFile
              )
            )
          );
        }
      }
    });

    compiler.hooks.invalid.tap(NAME, () => {
      plugin.dependencies = [];
    });
  }

  /**
   * Creates node-sass custom importer that checks if an import of a sass file from a module has a dependency of the
   * package.json of the dependending sass file. It will not make any transformation, so after the check it will always be
   * skipped by returning 'NULL' which will skip to the next custom importer or the default node-sass importer.
   */
  getNodeSassImporter() {
    const plugin = this;

    /**
     * @see https://github.com/sass/node-sass#importer--v200---experimental
     * (not so experimental anymore as tools like the webpack sass-loader also won't work without this)
     *
     * @param url the path in import as-is, which LibSass encountered
     * @param prev the previously resolved path
     * @param done a callback function to invoke on async completion
     */
    return function(url, prev, done) {
      if (plugin.isIncluded(prev)) {
        const prefixPattern = /^~/;
        if (prefixPattern.test(url)) {
          url = url.replace(prefixPattern, "");

          const modulePattern = /^((@[^\/]+\/)*[^\/])+/;
          if (modulePattern.test(url)) {
            const moduleName = modulePattern.exec(url)[0];
            const filePathByPackageName = packages.getFilePathByPackageName(
              moduleName,
              path.dirname(prev)
            );
            plugin.dependencies.push({
              sourceFile: prev,
              requiredFile: filePathByPackageName,
            });
            return filePathByPackageName;
          }
        }
      }
      done(nodeSass.types.NULL);
    };
  }

  getUnmanagedDependencies() {
    return this.dependencies
      .filter(({ sourceFile, requiredFile }) => {
        return !hasPkgDependency(sourceFile, requiredFile);
      })
      .map(({ sourceFile, requiredFile }) => {
        return {
          sourceFile: sourceFile,
          requiredFile: requiredFile,
        };
      });
  }

  /**
   * Checks if a path is included, based on a given includes and excludes list.
   * If includes were given the path must match at least one of the includes.
   * If excludes are given the path must not match any of the excludes.
   *
   * @param path the file path to be checked
   * @return {boolean} specifies if the file is to be included
   */
  isIncluded(path) {
    const patternOrStringMatches = patternOrString =>
      (patternOrString instanceof RegExp && patternOrString.test(path)) ||
      (typeof patternOrString === "string" &&
        path.indexOf(patternOrString) > -1);
    return (
      (!this.includes ||
        this.includes.length === 0 ||
        this.includes.some(patternOrStringMatches)) &&
      (!this.excludes || !this.excludes.some(patternOrStringMatches))
    );
  }
}

module.exports = {
  DependencyCheckWebpackPlugin,
};
