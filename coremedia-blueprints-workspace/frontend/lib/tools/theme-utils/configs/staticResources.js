const CopyWebpackPlugin = require("copy-webpack-plugin");
const closestPackage = require("closest-package");
const fs = require("fs");
const path = require("path");

const {
  dependencies: { getFlattenedDependencies },
  packages,
  workspace: {
    getIsSmartImportModuleFor,
    isThemeModule,
    getThemeConfig,
    parseSettings,
    mergeSettings,
  },
} = require("@coremedia/tool-utils");
const {
  ViewRepositoryMapping,
  ViewRepositoryPlugin,
} = require("../plugins/ViewRepositoryPlugin");
const JoinWebpackPlugin = require("../plugins/JoinWebpackPlugin");
const { ThemeDescriptorPlugin } = require("../plugins/ThemeDescriptorPlugin");
const { ZipperWebpackPlugin } = require("../plugins/ZipperWebpackPlugin");
const deepMerge = require("./utils/deepMerge");

const PROPERTIES_REG_EXP = /.+_(.+)\.properties$/;
const PROPERTIES_GLOB = "*_*.properties";
const SETTINGS_REG_EXP = /([^/\\]+)\.settings\.json$/;
const SETTINGS_GLOB = "*.settings.json";

const themeConfig = getThemeConfig();

const buildConfig = themeConfig["buildConfig"];

// inline all images that are smaller than 10000 bytes if not specified differently
const imageEmbedThreshold = buildConfig["imageEmbedThreshold"];

/**
 * Creates a single JoinWebpackPlugin instance for the given node modules to join the resource bundles into a
 * single resource bundle (one property file for every language) which is stored in the target folder.
 *
 * @param prefix the prefix to use
 * @param relativeResourceBundleSrc the relative source folder of the resource bundles (must be the same in all node modules)
 * @param relativeResourceBundleTarget the relative target folder for the single bundles
 * @param nodeModules the node modules
 * @returns {JoinWebpackPlugin} the instance of the plugin
 */
function configureJoinWebpackPluginForResourceBundles(
  prefix,
  relativeResourceBundleSrc,
  relativeResourceBundleTarget,
  nodeModules
) {
  // add search patterns for resource bundles, already filtered by actual dependencies to reduce overhead
  const searchPatterns = nodeModules
    .map(nodeModule => path.dirname(nodeModule.getPkgPath()))
    .map(nodeModulePkgPath =>
      path.resolve(
        nodeModulePkgPath,
        relativeResourceBundleSrc,
        PROPERTIES_GLOB
      )
    );

  return new JoinWebpackPlugin({
    name: path.join(relativeResourceBundleTarget, `${prefix}_[1].properties`),
    search: searchPatterns,
    join: function(common, addition, filename) {
      // gather resource bundles using a map (with package name as key), so they can be ordered before being stored
      common = common || {};
      const pkgPath = closestPackage.sync(filename);
      const pkg = packages.getJsonByFilePath(pkgPath);

      // add source of the keys as comment
      const relativeFilePath = path.relative(themeConfig.path, filename);
      addition =
        `# Resource Bundle from: ${pkg.name}\n# File: ${relativeFilePath}\n\n` +
        addition;

      common[pkg.name] = addition;
      return common;
    },
    save: function(common) {
      // join the map by iterating of the flattened dependency order
      const orderedContent = nodeModules.map(
        nodeModule => common[nodeModule.getName()] || ""
      );
      // join resource bundles, omit empty / non-existing entries
      return orderedContent.filter(content => !!content).join("\n");
    },
    group: "[1]",
    regExp: PROPERTIES_REG_EXP,
  });
}

/**
 * Create patterns for the given paths relative to the given baseFolder to be used in the CopyWebpackPlugin. If a path
 * does not exists, no pattern will be generated for the path.
 *
 * @param baseFolder A basefolder to resolve the paths against
 * @param paths {Array} The paths to configure
 */
function createPatternsCopyOverPaths(baseFolder, paths) {
  return paths
    .map(relativeThemePath => path.join(baseFolder, relativeThemePath))
    .filter(fs.existsSync)
    .map(themePath => ({
      from: themePath,
      to: path.relative(baseFolder, themePath),
      force: true,
      cache: true,
    }));
}

function getPatternsForCopyResources() {
  const mappingFromTo = themeConfig.scripts
    .concat(themeConfig.styles)
    .filter(resource => resource.type === "copy")
    .reduce(
      (aggregator, resource) => ({
        ...aggregator,
        [resource.src]: resource.target,
      }),
      {}
    );
  return Object.keys(mappingFromTo).map(from => ({
    from: path.join(themeConfig.path, from),
    to: mappingFromTo[from],
    force: true,
    cache: true,
  }));
}

function getPatternForThemeDescriptor() {
  // theme descriptor is optional since 1901
  const descriptorFileName = path.basename(themeConfig.descriptorTargetPath);
  const descriptorSourcePath = path.join(themeConfig.path, descriptorFileName);
  return fs.existsSync(descriptorSourcePath)
    ? {
        context: themeConfig.path,
        from: descriptorFileName,
        to: path.relative(
          themeConfig.themeTargetPath,
          path.dirname(themeConfig.descriptorTargetPath)
        ),
        force: true,
        cache: true,
      }
    : null;
}

/**
 * @module contains the webpack configuration for static resources like templates and resource bundles
 */
module.exports = () => config => {
  const nonThemeDependencies = getFlattenedDependencies(
    themeConfig.pkgPath,
    getIsSmartImportModuleFor(null)
  ).filter(nodeModule => !isThemeModule(nodeModule));

  const themeDependencies = getFlattenedDependencies(
    themeConfig.pkgPath,
    isThemeModule
  );

  const themePaths = themeDependencies.map(themeDependency =>
    path.dirname(themeDependency.getPkgPath())
  );

  themePaths.push(themeConfig.path);

  const joinWebpackPlugin = configureJoinWebpackPluginForResourceBundles(
    "Bricks",
    "src/l10n",
    path.relative(
      themeConfig.themeTargetPath,
      themeConfig.resourceBundleTargetPath
    ),
    nonThemeDependencies
  );

  const joinSettingsWebpackPlugin = (() => {
    const relativeSettingsSrc = "src/settings";
    const relativeSettingsTarget = path.relative(
      themeConfig.themeTargetPath,
      themeConfig.settingsTargetPath
    );
    const nodeModules = nonThemeDependencies.concat(themeDependencies);
    const searchPatterns = nodeModules
      .map(nodeModule => path.dirname(nodeModule.getPkgPath()))
      // add the theme that is being build
      .concat([themeConfig.path])
      .map(nodeModulePkgPath =>
        path.resolve(nodeModulePkgPath, relativeSettingsSrc, SETTINGS_GLOB)
      );

    return new JoinWebpackPlugin({
      name: path.join(relativeSettingsTarget, `[1].settings.json`),
      search: searchPatterns,
      join: (settingsByPackageName, newSettings, filename) => {
        // gather resource bundles using a map (with package name as key), so they can be ordered before being stored
        settingsByPackageName = settingsByPackageName || {};
        const packageDirPath = closestPackage.sync(filename);
        const packageName = packages.getJsonByFilePath(packageDirPath).name;

        settingsByPackageName[packageName] = parseSettings(
          filename,
          newSettings
        );
        return settingsByPackageName;
      },
      save: settingsByPackageName => {
        // join the map by iterating of the flattened dependency order
        const orderedSettings = nodeModules.map(
          nodeModule => settingsByPackageName[nodeModule.getName()] || ""
        );
        orderedSettings.push(
          settingsByPackageName[themeConfig.packageName] || ""
        );

        const mergedSettings = mergeSettings(orderedSettings);
        return JSON.stringify(mergedSettings, null, 2);
      },
      group: "[1]",
      regExp: SETTINGS_REG_EXP,
    });
  })();

  const viewRepositoryPlugin = new ViewRepositoryPlugin({
    templateGlobPattern: "**/*.+(ftl|fm|ftlh|ftlx)",
    targetPath: themeConfig.templatesTargetPath,
    mappings: [new ViewRepositoryMapping(themeConfig.name)],
  });

  return deepMerge(config, {
    entry: {
      ...viewRepositoryPlugin.getEntry(),
    },
    module: {
      rules: [
        // let svgParamLoader process the svg files to inject parameters if needed
        {
          test: /\.param\.svg$/,
          loader: require.resolve("../loaders/SvgParamLoader"),
          options: {
            limit: true, // always inline svg with parameters
          },
        },
        {
          test: /\.(svg|jpg|jpeg|png|gif)$/,
          loader: "url-loader",
          exclude: /\.param\.svg$/, // do not double load svg files with injected parameters
          options: {
            name: "[name].[ext]",
            limit: imageEmbedThreshold,
            outputPath: "img/",
          },
        },
        {
          test: /\.(woff|woff2|ttf|eot)$/,
          loader: "file-loader",
          options: {
            name: "[name].[ext]",
            outputPath: "fonts/",
          },
        },
        // templates
        {
          test: /.(ftl|fm|ftlh|ftlx)$/,
          use: [
            viewRepositoryPlugin.getLoaderConfig(),
            {
              loader: require.resolve("../loaders/TransformFreemarkerLoader/"),
              options: {
                viewRepositoryName: themeConfig.name,
              },
            },
          ],
        },
        {
          test: /\.swf$/,
          loader: "file-loader",
          options: {
            name: "[name].[ext]",
            outputPath: "swf/",
          },
        },
        {
          test: PROPERTIES_REG_EXP,
          use: [joinWebpackPlugin.loader()],
        },
        {
          type: "javascript/auto",
          test: SETTINGS_REG_EXP,
          use: [joinSettingsWebpackPlugin.loader()],
        },
      ],
    },
    plugins: [
      viewRepositoryPlugin,
      joinWebpackPlugin,
      // configure for themes
      ...themePaths.map(
        themePath =>
          new CopyWebpackPlugin([
            ...createPatternsCopyOverPaths(path.join(themePath, "src"), [
              "css",
              "fonts",
              "img",
              "images",
              "vendor",
              path.relative(
                themeConfig.themeTargetPath,
                themeConfig.resourceBundleTargetPath
              ),
            ]),
          ])
      ),
      joinSettingsWebpackPlugin,
      // theme descriptor
      new CopyWebpackPlugin(
        [
          getPatternForThemeDescriptor(),
          ...getPatternsForCopyResources(),
          // remove null patterns
        ].filter(descriptor => !!descriptor)
      ),
      new ThemeDescriptorPlugin(themeConfig),
      new ZipperWebpackPlugin(
        [
          {
            source: path.relative(
              themeConfig.resourcesTargetPath,
              themeConfig.themeTemplatesTargetPath
            ),
            prefix: path.normalize("META-INF/resources/"),
            context: path.relative(
              themeConfig.themeTargetPath,
              themeConfig.resourcesTargetPath
            ),
          },
          // also zip the freemarkerLibs into the theme again otherwise the Frontend Developer Workflow will not be able
          // to properly handle importing from a theme template to a brick freemarker lib
          {
            source: "bricks/freemarkerLibs",
            prefix: path.join(
              "META-INF/resources/WEB-INF/templates",
              themeConfig.name
            ),
            context: path.relative(
              themeConfig.themeTargetPath,
              themeConfig.templatesTargetPath
            ),
          },
        ],
        {
          filepath: path.relative(
            themeConfig.themeTargetPath,
            themeConfig.themeTemplatesJarTargetPath
          ),
          compilerEvent: "after-emit",
        }
      ),
      new ZipperWebpackPlugin(
        [
          {
            source: path.relative(
              themeConfig.resourcesTargetPath,
              themeConfig.brickTemplatesTargetPath
            ),
            prefix: path.normalize("META-INF/resources/"),
            context: path.relative(
              themeConfig.themeTargetPath,
              themeConfig.resourcesTargetPath
            ),
          },
        ],
        {
          filepath: path.relative(
            themeConfig.themeTargetPath,
            themeConfig.brickTemplatesJarTargetPath
          ),
          compilerEvent: "after-emit",
        }
      ),
    ],
  });
};
