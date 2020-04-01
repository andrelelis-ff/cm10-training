const fs = require("fs");
const resolveFrom = require("resolve-from");
const path = require("path");
const {
  workspace: {
    EXAMPLE_SCOPE,
    generateJsFileNameFromEntryPointName,
    getInitJs,
    getShims,
    getThemeConfig,
    getIsSmartImportModuleFor,
    isExampleModuleName,
    isExampleBrickModule,
    isExampleThemeModule,
  },
  dependencies: { getFlattenedDependencies },
} = require("@coremedia/tool-utils");
const deepMerge = require("./utils/deepMerge");

const themeConfig = getThemeConfig();

const PREVIEW_ENTRY_NAME = "preview";
const COMMONS_CHUNK_NAME = "commons";

function getMainJs(packageName) {
  let mainJs = null;
  try {
    mainJs = resolveFrom(themeConfig.path, packageName);
  } catch (e) {
    // ignore, just a check
  }
  return mainJs;
}

const flattenedThemeDependencies = getFlattenedDependencies(
  themeConfig.pkgPath,
  getIsSmartImportModuleFor(null)
);

/**
 * Builds an entry point with the given name and collects smart import dependencies using the given variant.
 *
 * @param variant the variant to use
 * @param paths the path to the file entry
 * @returns {object} the entry point
 */
function buildEntryPoint(variant, paths) {
  paths = paths.filter(path => fs.existsSync(path));
  // if the entry point provides no entry point for JavaScript, use an empty index.js (otherwise webpack will not run)
  if (paths.length === 0) {
    paths.push(require.resolve("./emptyIndex"));
  }
  const autoActivateDependencies = flattenedThemeDependencies.filter(
    getIsSmartImportModuleFor(variant)
  );
  const autoActivateEntryPoints = autoActivateDependencies
    .map(getInitJs)
    .filter(dependency => !!dependency);
  return [...autoActivateEntryPoints, ...paths];
}

function getLoaderParams(obj) {
  return Object.keys(obj)
    .map(key => {
      const value = obj[key];
      if (key) {
        return `${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
      }
      return encodeURIComponent(value);
    })
    .join("&");
}

function getShimLoaderConfig() {
  return flattenedThemeDependencies
    .map(getShims)
    .reduce((aggregator, next) => aggregator.concat(next), [])
    .map(shim => {
      const loaders = [];
      const imports = shim.getImports();
      const exports = shim.getExports();
      if (Object.keys(imports).length) {
        loaders.push("imports-loader?" + getLoaderParams(imports));
      }
      if (Object.keys(exports).length) {
        loaders.push("exports-loader?" + getLoaderParams(exports));
      }
      return {
        test: require.resolve(shim.getTarget()),
        use: loaders,
      };
    })
    .reduce((aggregator, next) => aggregator.concat(next), []);
}

// Check if example theme is used or non-example theme has dependencies to example packages.
if (isExampleModuleName(themeConfig.packageName)) {
  console.log(
    `${themeConfig.packageName} is an example theme.
If you want to reuse this theme in your project, please create a copy of the theme folder and change the package name in the "package.json" so it does no longer start with "${EXAMPLE_SCOPE}/". It is also adviced to change the theme name.`
  );
} else {
  const dependenciesToExampleThemes = flattenedThemeDependencies.filter(
    isExampleThemeModule
  );
  if (dependenciesToExampleThemes.length > 0) {
    console.warn(
      `You have dependencies to the following examples themes:
${dependenciesToExampleThemes
  .map(nodeModule => `- ${nodeModule.getName()}`)
  .join("\n")}
If you want to use these themes in your project, please create a copy and change their package name so it does no longer start with "${EXAMPLE_SCOPE}/". It is also adviced to change the name of the theme in the theme configuration.`
    );
  } else {
    const dependenciesToExampleBricks = flattenedThemeDependencies.filter(
      isExampleBrickModule
    );
    if (dependenciesToExampleBricks.length > 0) {
      console.warn(
        `You have dependencies to the following examples bricks:
${dependenciesToExampleBricks
  .map(nodeModule => `- ${nodeModule.getName()}`)
  .join("\n")}
If you want to use these bricks in your project, you need to eject them (e.g. by using "yarn run eject" from the workspace root). For more information, check the CoreMedia Frontend Developer Guide.`
      );
    }
  }
}

// check if theme has own babel configuration file, fallback to theme-utils if not
const babelConfigFile = !fs.existsSync(
  path.resolve(themeConfig.path, "babel.config.js")
)
  ? path.resolve(__dirname, "babel.config.js")
  : undefined;

// automatically add preview entry point
// TODO: right now the "main" entry in the theme's package.json is only taken into account for the location of the preview.js
const mainJsPath = getMainJs(".");

const previewJsPath = mainJsPath
  ? path.resolve(path.dirname(mainJsPath), "preview.js")
  : path.resolve(themeConfig.path, "src/js/preview.js");

themeConfig.scripts.push({
  type: "webpack",
  src: path.relative(themeConfig.path, previewJsPath),
  entryPointName: PREVIEW_ENTRY_NAME,
  smartImport: "preview",
  include: false,
  ieExpression: null,
  defer: true,
});

const linkedChunks = [];
const entry = themeConfig.scripts
  .filter(script => script.type === "webpack")
  .reduce((entries, script) => {
    const chunkName = script.entryPointName;
    const srcList = script.src instanceof Array ? script.src : [script.src];
    entries[chunkName] = buildEntryPoint(
      script.smartImport,
      srcList.map(src => path.join(themeConfig.path, src))
    );
    if (script.include) {
      linkedChunks.push(chunkName);
    }
    return entries;
  }, {});

const additionalConfig = {};
if (linkedChunks.length > 0) {
  additionalConfig.optimization = {
    runtimeChunk: {
      // share the same runtime between chunks (otherwise theme and preview have different module instances)
      name: COMMONS_CHUNK_NAME,
    },
    splitChunks: {
      cacheGroups: {
        commons: {
          // share common code between first linked chunk and the preview
          name: COMMONS_CHUNK_NAME,
          chunks: chunk => {
            return [linkedChunks[0], PREVIEW_ENTRY_NAME].includes(chunk.name);
          },
          minChunks: 2,
          minSize: 0,
        },
      },
    },
  };
}

module.exports = ({
  include,
  exclude,
  dependencyCheckPlugin,
  mode,
}) => config =>
  deepMerge(config, {
    entry: entry,
    output: {
      filename: generateJsFileNameFromEntryPointName("[name]"),
    },
    module: {
      rules: [
        {
          test: /\.js$/,
          loader: "eslint-loader",
          options: {
            cache: true,
          },
          enforce: "pre",
          include: include,
          exclude: exclude,
        },
        {
          test: /\.js$/,
          use: [
            {
              loader: "babel-loader",
              options: {
                configFile: babelConfigFile,
                // babel-loader specific options
                cacheDirectory: true,
              },
            },
          ],
          exclude: exclude,
        },
        ...getShimLoaderConfig(),
      ],
    },
    plugins: [dependencyCheckPlugin],
    ...additionalConfig,
  });
