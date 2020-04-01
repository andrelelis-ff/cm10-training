/**
 * Helper for Webpack loaders needing to load multiple modules.
 *
 * @param loaderContext The context of the loader
 * @param {Array<String>} modules an array of modules to load
 * @param callback will be called as soon as all modules are loaded
 */
function loadModules(loaderContext, modules, callback) {
  if (modules.length === 0) {
    callback();
  } else {
    // trigger next module:
    const nextModule = modules.shift();

    loaderContext.loadModule(nextModule, err => {
      if (err) {
        throw new Error(
          `Could not import module: "${nextModule}" in source file "${loaderContext.resourcePath}".\n\n${err}`
        );
      }
      loadModules(loaderContext, modules, callback);
    });
  }
}

module.exports = {
  loadModules,
};
