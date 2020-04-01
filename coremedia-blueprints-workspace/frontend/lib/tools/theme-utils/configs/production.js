const deepMerge = require("./utils/deepMerge");
const OptimizeCssAssetsPlugin = require("optimize-css-assets-webpack-plugin");

const optimizeCssAssetsPlugin = new OptimizeCssAssetsPlugin();

/**
 * @module contains the webpack configuration for the production environment
 */
module.exports = () => config =>
  deepMerge(config, {
    mode: "production",
    // see https://webpack.js.org/configuration/stats/
    stats: "minimal",
    performance: {
      // disable newly introduced performance warnings about chunk size as we will not
      // make heavy use of chunk splitting at the moment (which could improve performance)
      hints: false,
    },
    plugins: [optimizeCssAssetsPlugin],
  });
