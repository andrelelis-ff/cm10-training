const deepMerge = require("./utils/deepMerge");
const path = require("path");
const {
  workspace: { getThemeConfig },
} = require("@coremedia/tool-utils");
const { ZipperWebpackPlugin } = require("../plugins/ZipperWebpackPlugin");

const themeConfig = getThemeConfig();

/**
 * @module contains the webpack configuration for creating the theme zip file
 */
module.exports = () => config =>
  deepMerge(config, {
    plugins: [
      new ZipperWebpackPlugin(
        [
          {
            source: themeConfig.name,
            context: "..",
          },
          {
            source: path.relative(
              themeConfig.resourcesTargetPath,
              themeConfig.descriptorTargetPath
            ), // include the THEME-METADATA directory in archive
            context: path.relative(
              themeConfig.themeTargetPath,
              themeConfig.resourcesTargetPath
            ),
          },
        ],
        {
          filepath: path.relative(
            themeConfig.themeTargetPath,
            themeConfig.themeArchiveTargetPath
          ),
          compilerEvent: "after-emit",
        }
      ),
    ],
  });
