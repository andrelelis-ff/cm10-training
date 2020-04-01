const closestPackage = require("closest-package");
const fs = require("fs");
const path = require("path");
const nodeSass = require("node-sass");

const {
  workspace: { getThemeConfig, getIsSmartImportModuleFor },
  packages,
  dependencies: {
    flattenDependencies,
    getDependencies,
    getDependentsFirstLoadOrder,
  },
} = require("@coremedia/tool-utils");

const { resolveScss } = require("./utils");

const REGEX = /(~(.+))?\?smart-import-(variables|partials)/;

const SASS_PATHS = ["src/sass", "src"];

const themeConfig = getThemeConfig();

function getSmartImportPath(dependency, varsOrPartials, prev) {
  const candidates = SASS_PATHS.map(sassPath =>
    path.join(dependency.getName(), sassPath, varsOrPartials)
  )
    .map(url => `~${url.replace(/\\/g, "/")}`)
    .filter(url => fs.existsSync(resolveScss(url, prev)));
  return candidates.length > 0 ? candidates[0] : null;
}

function getVariant(prev) {
  const styleEntriesWithSmartImport = themeConfig.styles.filter(
    style => style.type === "webpack" && style.smartImport
  );

  const resolvedPrev = path.resolve(prev);
  const styleEntryPoint = styleEntriesWithSmartImport.find(style => {
    const srcList = style.src instanceof Array ? style.src : [style.src];
    return srcList.map(src => path.resolve(src)).includes(resolvedPrev);
  });
  return styleEntryPoint ? styleEntryPoint.smartImport : null;
}

function calculateSmartImport(prev) {
  const variant = getVariant(prev);
  const themeDependencies = getDependencies(
    themeConfig.pkgPath,
    getIsSmartImportModuleFor(null)
  );
  return {
    root: prev,
    variant: variant,
    dependentsFirstLoadOrder: getDependentsFirstLoadOrder(
      themeDependencies,
      themeConfig.packageName
    ),
    // filter partials (=> css generating code) that would be included twice.
    // we always assume that the variant is loaded in addition to the default.
    whiteList: flattenDependencies(themeDependencies)
      .filter(getIsSmartImportModuleFor(variant))
      .map(dependency => dependency.getName()),
  };
}

module.exports = function(url, prev, done) {
  // take the prev of the first import to get variant information (prev is the entry point)
  this._sassSmartImport = this._sassSmartImport || calculateSmartImport(prev);

  const match = REGEX.exec(url);
  if (match && match.length > 0) {
    const { whiteList } = this._sassSmartImport;

    const [, , providedPackageName, varsOrPartials] = match;

    // either package name is provided explicitly or we need to evaluate it from prev
    let packageName;
    let pkgJson;
    if (providedPackageName) {
      packageName = providedPackageName;
      pkgJson = packages.getFilePathByPackageName(providedPackageName);
    } else {
      pkgJson = closestPackage.sync(prev);
      packageName = packages.getJsonByFilePath(pkgJson).name;
    }

    let dependenciesToImport;
    if (varsOrPartials === "variables") {
      // variables need dependents first load order
      dependenciesToImport =
        this._sassSmartImport.dependentsFirstLoadOrder[packageName] || [];
    } else {
      // for partials the direct load order specified through the dependency hierarchy is sufficient (which are our dependencies, we just don't flatten them)
      dependenciesToImport = getDependencies(
        pkgJson,
        getIsSmartImportModuleFor(null)
      );
    }

    // Get scss paths of the dependent bricks
    const sassPaths = dependenciesToImport.map(dependency => {
      const includeParts =
        whiteList.includes(dependency.getName()) &&
        getSmartImportPath(dependency, varsOrPartials, pkgJson);
      return includeParts
        ? includeParts
        : `~${dependency.getName()}?smart-import-${varsOrPartials}`;
    });

    const scssContent = sassPaths
      .map(sassPath => `@import "${sassPath}";`)
      .join("\n");

    done({
      contents: scssContent,
      // also rewrite filename otherwise the loop protection might abort the scss compilation
      file: path.resolve(
        path.dirname(pkgJson),
        "smart-import-" + varsOrPartials + ".scss"
      ),
    });
  } else {
    done(nodeSass.types.NULL);
  }
};
