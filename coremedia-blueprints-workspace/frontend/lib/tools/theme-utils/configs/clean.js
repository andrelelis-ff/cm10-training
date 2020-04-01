const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const deepMerge = require("./utils/deepMerge");

/**
 * @module contains the webpack configuration for cleaning the target directory
 */
module.exports = () => config =>
  deepMerge(config, {
    plugins: [
      new CleanWebpackPlugin({
        cleanStaleWebpackAssets: false,
      }),
    ],
  });
