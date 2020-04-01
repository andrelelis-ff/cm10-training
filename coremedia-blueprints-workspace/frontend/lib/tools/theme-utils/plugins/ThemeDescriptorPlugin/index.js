const fs = require("fs");
const mkdirp = require("mkdirp");
const path = require("path");
const xmlBuilder = require("xmlbuilder");
const deepMerge = require("../../configs/utils/deepMerge");
const { RawSource } = require("webpack-sources");
const {
  workspace: {
    transformLinksInJson,
    generateJsFileNameFromEntryPointName,
    generateCssFileNameFromEntryPointName,
  },
} = require("@coremedia/tool-utils");

function getXmlPath(path) {
  return path.replace(/\\/g, "/");
}

function getTargets(code, filesToLoadByEntryPoint, entryPointFilesFilter) {
  const targets = [];
  switch (code.type) {
    case "webpack":
      const filesToLoad = filesToLoadByEntryPoint[code.entryPointName] || [];
      const jsFilesToLoad = filesToLoad.filter(
        filename =>
          !entryPointFilesFilter || entryPointFilesFilter.test(filename)
      );
      targets.push(...jsFilesToLoad);
      break;
    case "copy":
      targets.push(code.target);
      break;
    case "externalLink":
    default:
      targets.push(code.src);
  }
  return targets;
}

class ThemeDescriptorPlugin {
  constructor(themeConfig) {
    this.themeConfig = themeConfig;
  }

  // cleanup templates (webpack does not support deletion in watch mode)
  apply(compiler) {
    compiler.hooks.emit.tapAsync(
      "CoreMedia Theme Descriptor Plugin",
      (compilation, callback) => {
        // generate files to load per entry point
        const filesToLoadByEntryPoint = {};
        compilation.entrypoints.forEach((entryPoint, entryPointName) => {
          filesToLoadByEntryPoint[entryPointName] = [];
          entryPoint.chunks.forEach(chunk => {
            chunk.files.forEach(file => {
              filesToLoadByEntryPoint[entryPointName].push(file);
            });
          });
        });

        const root = xmlBuilder
          .create("themeDefinition", { encoding: "UTF-8" }, undefined)
          .att("modelVersion", "1");

        const themeConfig = this.themeConfig;

        // general information
        root.ele("name", themeConfig.name);
        root.ele("description", themeConfig.description);

        // thumbnail is not an asset, so copy it manually
        if (themeConfig.thumbnail) {
          const thumbnailSrcPath = path.join(
            themeConfig.path,
            themeConfig.thumbnail
          );
          const thumbnailTargetPath = path.join(
            themeConfig.themeTargetPath,
            path.basename(thumbnailSrcPath)
          );
          mkdirp.sync(path.dirname(thumbnailTargetPath));
          fs.copyFileSync(thumbnailSrcPath, thumbnailTargetPath);
          root.ele(
            "thumbnail",
            getXmlPath(
              path.relative(themeConfig.themeTargetPath, thumbnailTargetPath)
            )
          );
        }

        // styleSheets
        if (themeConfig.styles.length > 0) {
          const styleSheets = root.ele("styleSheets");
          const alreadyAddedStyles = new Set();
          themeConfig.styles.forEach(style => {
            getTargets(style, filesToLoadByEntryPoint, /\.css$/)
              .filter(target => !alreadyAddedStyles.has(target))
              .forEach(target => {
                // avoid windows path separators!
                styleSheets.ele("css", getXmlPath(target), {
                  ...(style.ieExpression !== null
                    ? { ieExpression: style.ieExpression }
                    : {}),
                  disableCompress: style.disableServerSideCompression !== false,
                  notLinked: style.include !== true,
                });
              });
          });
        }

        // javaScripts
        if (themeConfig.scripts.length > 0) {
          const javaScripts = root.ele("javaScripts");
          const alreadyAddedScripts = new Set();
          themeConfig.scripts.forEach(scripts => {
            // avoid windows path separators!
            getTargets(scripts, filesToLoadByEntryPoint, /\.js$/)
              .filter(target => !alreadyAddedScripts.has(target))
              .forEach(target => {
                alreadyAddedScripts.add(target);
                javaScripts.ele("javaScript", getXmlPath(target), {
                  ...(scripts.ieExpression !== null
                    ? { ieExpression: scripts.ieExpression }
                    : {}),
                  ...(scripts.inHead !== undefined
                    ? { inHead: scripts.inHead }
                    : {}),
                  disableCompress:
                    scripts.disableServerSideCompression !== false,
                  ...(scripts.defer === true ? { defer: true } : {}),
                  notLinked: scripts.include !== true,
                });
              });
          });
        }

        // resourceBundles
        if (themeConfig.l10n.bundleNames.length > 0) {
          const resourceBundles = root.ele("resourceBundles");
          themeConfig.l10n.bundleNames.forEach(bundleName => {
            resourceBundles.ele(
              "resourceBundle",
              `l10n/${bundleName}_${themeConfig.l10n.masterLanguage}.properties`
            );
          });
        }

        // templateSets
        const templateSets = root.ele("templateSets");
        templateSets.ele(
          "templateSet",
          getXmlPath(
            path.relative(
              themeConfig.themeTargetPath,
              themeConfig.themeTemplatesJarTargetPath
            )
          )
        );

        const srcTargetMappings = {};
        themeConfig.scripts
          .concat(themeConfig.styles)
          .filter(code => code.type === "copy")
          .forEach(copyCode => {
            // copyCode always has "src" and "target"
            srcTargetMappings[copyCode.src] = copyCode.target;
          });
        themeConfig.scripts
          .filter(script => script.type === "webpack")
          .forEach(webpackScript => {
            // webpackScript always has "src" and "entryPoint"
            srcTargetMappings[
              webpackScript.src
            ] = generateJsFileNameFromEntryPointName(
              webpackScript.entryPointName
            );
          });
        themeConfig.styles
          .filter(style => style.type === "webpack")
          .forEach(webpackStyle => {
            srcTargetMappings[
              webpackStyle.src
            ] = generateCssFileNameFromEntryPointName(
              webpackStyle.entryPointName
            );
          });

        const absoluteSrcTargetMappings = Object.keys(srcTargetMappings).reduce(
          (aggregator, srcPath) => {
            const targetPath = srcTargetMappings[srcPath];
            aggregator[path.resolve(themeConfig.path, srcPath)] = path.resolve(
              themeConfig.themeTargetPath,
              targetPath
            );
            return aggregator;
          },
          {}
        );

        const settingsLinkTransformer = (filePath, ...params) => {
          const assetPath = params[0];
          const absoluteFilePath = path.resolve(themeConfig.path, filePath);
          const newAbsoluteTargetPath =
            absoluteSrcTargetMappings[absoluteFilePath];

          // return a path relative to the assetPath
          if (newAbsoluteTargetPath) {
            const absoluteSettingsDirPath = path.dirname(
              path.resolve(themeConfig.themeTargetPath, assetPath)
            );
            return path.relative(
              absoluteSettingsDirPath,
              newAbsoluteTargetPath
            );
          }
          // no matching paths in theme config
          throw new Error(
            `Could not find "${filePath}" in theme build. Please make sure that you have configured the file in the theme configuration.`
          );
        };

        Object.keys(compilation.assets)
          .filter(assetKey => /(.+)\.settings\.json$/.test(assetKey))
          .forEach(key => {
            const settingsJson = JSON.parse(compilation.assets[key]._value);
            transformLinksInJson(settingsJson, settingsLinkTransformer, key);
            compilation.assets[key] = new RawSource(
              JSON.stringify(settingsJson, null, 2)
            );
          });

        // hard coded configuration for preview settings
        // this is sufficient as long as we do not have a generic settings mechanism in the workspace
        const relativePreviewSettingsFolder = "settings";
        const relativePreviewSettingsPath = path.join(
          relativePreviewSettingsFolder,
          "Preview.settings.json"
        );

        const previewAsset = compilation.assets[relativePreviewSettingsPath];
        const previewAssetJson = previewAsset
          ? JSON.parse(previewAsset._value)
          : {};

        const previewSettings = JSON.stringify(
          deepMerge(previewAssetJson, {
            previewJs: [
              {
                $Link: getXmlPath(
                  path.relative(
                    relativePreviewSettingsFolder,
                    filesToLoadByEntryPoint["preview"].find(filename =>
                      /preview.js$/.test(filename)
                    )
                  )
                ),
              },
            ],
            previewCss: [
              {
                $Link: getXmlPath(
                  path.relative(
                    relativePreviewSettingsFolder,
                    filesToLoadByEntryPoint["preview"].find(filename =>
                      /preview.css$/.test(filename)
                    )
                  )
                ),
              },
            ],
          }),
          null,
          2
        );
        compilation.assets[relativePreviewSettingsPath] = new RawSource(
          previewSettings
        );

        const settings = root.ele("settings");
        Object.keys(compilation.assets)
          .filter(assetKey => /(.+)\.settings\.json$/.test(assetKey))
          .forEach(assetKey => {
            settings.ele("setting", getXmlPath(assetKey));
          });

        const xml = root.end({
          pretty: true,
          indent: "  ",
          newline: "\n",
        });

        mkdirp.sync(path.dirname(themeConfig.descriptorTargetPath));
        fs.writeFileSync(themeConfig.descriptorTargetPath, xml, {
          encoding: "UTF-8",
        });
        callback();
      }
    );
  }
}

module.exports = {
  ThemeDescriptorPlugin,
};
