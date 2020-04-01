const { webpackConfig } = require("@coremedia/theme-utils");

module.exports = (env, argv) => {
  const config = webpackConfig(env, argv);

  config.externals = config.externals || {};
  config.externals["jquery"] = "jQuery";

  return config;
};
