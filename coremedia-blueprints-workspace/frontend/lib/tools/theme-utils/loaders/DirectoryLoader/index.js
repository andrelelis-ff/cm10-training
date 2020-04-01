const fs = require("fs");
const loaderUtils = require("loader-utils");
const path = require("path");
const glob = require("glob");
const closestPackage = require("closest-package");
const {
  dependencies: { getDependencies, getDependentsFirstLoadOrder },
  packages,
  workspace: { getIsSmartImportModuleFor },
} = require("@coremedia/tool-utils");
const { loadModules } = require("../utils");

const importedViews = [];
let dependenciesToLoadByPackageName = {};

module.exports = function() {
  const callback = this.async();
  // cannot be cached because of 2 reasons:
  // 1) loader is not stateless due to needing to track the "importedViews"
  // 2) a plugin configuration in clean.js clears the templates that have not been provided by the last build
  this.cacheable(false);

  // parse query params
  const queryParams = loaderUtils.getOptions(this) || {};
  const directory = queryParams.directory;
  const pattern = queryParams.pattern || "**/*.*";
  const subtask = !!queryParams.pkgPath;
  const pkgPath = queryParams.pkgPath || closestPackage.sync(process.cwd());

  if (directory === null || directory === undefined) {
    throw new Error("Directory Loader was used without providing a directory");
  }

  const packageName = packages.getJsonByFilePath(pkgPath).name;
  const modulesToLoad = [];

  if (!subtask) {
    // clear array of imported views as new run was started
    importedViews.length = 0;

    const dependencies = getDependencies(
      pkgPath,
      getIsSmartImportModuleFor(null)
    );
    dependenciesToLoadByPackageName = getDependentsFirstLoadOrder(
      dependencies,
      packageName
    );
  }

  const resolvedDirectory = path.resolve(path.dirname(pkgPath), directory);
  if (resolvedDirectory && fs.existsSync(resolvedDirectory)) {
    // track context dependency of directly registered template folders
    this.addContextDependency(resolvedDirectory);
    const foundSubdirectories = glob.sync("**/*/", { cwd: resolvedDirectory });
    // track all sub folders
    foundSubdirectories.forEach(foundSubdirectory =>
      this.addContextDependency(
        path.join(resolvedDirectory, path.basename(foundSubdirectory))
      )
    );

    // find all views
    const globResult = glob
      .sync(pattern, {
        cwd: resolvedDirectory,
        nodir: true,
      })
      .filter(file => !importedViews.includes(file));

    importedViews.push(...globResult);

    const foundModules = globResult
      .map(file => fs.realpathSync(path.join(resolvedDirectory, file)))
      .map(file =>
        loaderUtils.urlToRequest(
          path.relative(process.cwd(), file),
          this.context
        )
      );
    modulesToLoad.push(...foundModules);
  }

  // modules need to be loaded synchronously, so wait until a module is fully loaded
  modulesToLoad.push(
    ...(dependenciesToLoadByPackageName[packageName] || []).map(
      dependency =>
        `${__filename}?directory=${encodeURIComponent(
          directory
        )}&pattern=${encodeURIComponent(pattern)}&pkgPath=${encodeURIComponent(
          dependency.getPkgPath()
        )}!`
    )
  );
  loadModules(this, modulesToLoad, () => {
    callback(null, "");
  });
};
