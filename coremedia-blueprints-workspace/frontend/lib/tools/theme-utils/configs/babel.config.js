const browserslist = require("browserslist");

// pickEnv is adopted from
// https://github.com/ai/browserslist/blob/bc78b5d635344796c77a7e23b66f311ad0d9ab5e/index.js#L99-L114
function pickEnv(config) {
  if (typeof config !== "object") return config;

  let env;
  if (typeof process.env.BROWSERSLIST_ENV === "string") {
    env = process.env.BROWSERSLIST_ENV;
  } else if (typeof process.env.NODE_ENV === "string") {
    env = process.env.NODE_ENV;
  } else {
    env = "development";
  }

  return config[env] || config.defaults;
}

module.exports = api => {
  api.cache(true);
  const browserslistConfig = pickEnv(browserslist.findConfig("."));

  let targetBrowsers = undefined;
  if (browserslistConfig) {
    targetBrowsers = Array.isArray(browserslistConfig)
      ? browserslistConfig
      : [browserslistConfig];
  }

  return {
    // Do not use .babelrc file
    babelrc: false,
    comments: false,
    plugins: ["@babel/transform-runtime"],
    presets: [
      [
        "@babel/preset-env",
        {
          targets: {
            browsers: targetBrowsers,
          },
        },
      ],
      "@babel/react",
    ],
  };
};
