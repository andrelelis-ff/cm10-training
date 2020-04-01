"use strict";

const sharedData = require("./sharedData");

/**
 * returns content for package.json
 * @param  {string} themeName the name of the theme
 * @param  {string} mainJSFile relative path to the main JavaScript file of the theme
 * @param  {Object} dependenciesToActivate
 * @param  {Object} dependenciesToCommentOut
 * @return {string}
 */
const initPackageJson = (
  themeName,
  mainJSFile,
  dependenciesToActivate,
  dependenciesToCommentOut
) => {
  let commentDependencies = {};
  if (Object.keys(dependenciesToCommentOut).length > 0) {
    commentDependencies = {
      __comment__dependencies__: {
        __comment__:
          "List of all brick dependencies. In order to add a dependency, move the entry to 'dependencies'",
        ...dependenciesToCommentOut,
        "@coremedia/brick-utils": "^1.0.0",
        jquery: "^3.0.0",
      },
    };
  }

  const content = {
    name: `@coremedia/${themeName}-theme`,
    version: "1.0.0",
    private: true,
    scripts: {
      build: "webpack",
      deploy: "cm theme-importer deploy-theme",
      start: "cm monitor",
      prettier: 'prettier "**/*" --write',
      "theme-importer": "cm theme-importer",
    },
    ...commentDependencies,
    dependencies: {
      ...dependenciesToActivate,
      "@coremedia/cm-cli": "^2.0.0",
      "@coremedia/theme-utils": "^3.0.0",
      webpack: "^4.0.0",
    },
    devDependencies: {
      prettier: "^1.0.0",
    },
    main: mainJSFile,
    coremedia: {
      type: "theme",
      init: `src/js/${themeName}.js`,
    },
    browserslist: [
      "last 1 Chrome version",
      "last 1 Firefox version",
      "last 1 Edge version",
      "Explorer >= 11",
    ],
  };
  return JSON.stringify(content, null, 2);
};

/**
 * returns content for webpack.config.js
 * @param  {string} themeToDeriveFrom
 * @return {string}
 */
const initWebpackConfigJs = (themeToDeriveFrom = "") => {
  let importWebpackConfig = `const { webpackConfig } = require("@coremedia/theme-utils");`;
  if (typeof themeToDeriveFrom === "string" && themeToDeriveFrom.length !== 0) {
    importWebpackConfig = `const webpackConfig = require("${themeToDeriveFrom}/webpack.config.js");`;
  }
  return `${importWebpackConfig}

module.exports = webpackConfig;`;
};

/**
 * returns content for theme descriptor
 * @param  {string} themeName
 * @param  {boolean} usingBricks
 * @return {string}
 */
const initThemeConfigJson = (themeName, usingBricks) => {
  const content = {
    name: `${themeName}`,

    styles: [
      {
        type: "webpack",
        src: `src/sass/${themeName}.scss`,
      },
    ],
    scripts: [
      {
        type: "webpack",
        src: `src/js/${themeName}.js`,
      },
    ],
    l10n: {
      bundleNames: [
        sharedData.titleCase(themeName),
        ...(usingBricks ? ["Bricks"] : []),
      ],
    },
  };
  return JSON.stringify(content, null, 2);
};

/**
 * returns content for <themeName>.sass
 * @param  {string} themeName
 * @return {string}
 */
const initThemeSass = themeName => `/*! Theme ${themeName} */
// ### VARIABLES ###

// Own variables (need to be loaded first, so default values can be overridden)
// @see http://sass-lang.com/documentation/file.SASS_REFERENCE.html#Variable_Defaults___default

@import "variables";

// ### PARTIALS ###

@import "partials";
`;

/**
 * returns content for _partials.sass
 * @return {string}
 */
const initPartialsSass = () => `// Dependency styles
@import "?smart-import-partials";

// Own partials
`;

/**
 * returns content for _variables.sass
 * @return {string}
 */
const initVariablesSass = () => `// Own variable imports first

// Dependency variables
@import "?smart-import-variables";
`;

/**
 * returns content for preview.sass
 * @param  {string} themeName
 * @return {string}
 */
const initPreviewSass = themeName => `/*! Theme ${themeName}: Preview Styles */
// ### VARIABLES ###

// Own variables (need to be loaded first, so default values can be overridden)
// @see http://sass-lang.com/documentation/file.SASS_REFERENCE.html#Variable_Defaults___default

//@import "variables/...";

// Dependency variables

@import "?smart-import-variables";

// ... add third-party dependencies here (after smart-import-variables)

// ### PARTIALS ###

// Dependency partials

// ... add third-party dependencies here (before smart-import-partials)

@import "?smart-import-partials";

// Own partials

//@import "partials/...";
`;

/**
 * returns content for index.js
 * @param  {string} themeName
 * @return {string}
 */
const initThemeIndexJs = themeName => `/*! Theme ${themeName} */
import "./${themeName}.js";
`;

/**
 * returns content for .prettierignore
 * @return {string}
 */
const initThemePrettierignore = () => `/*
/*
!/src
/src/*
!/src/js
`;

/**
 * returns content for <themeName>.js
 * @param  {string} themeName
 * @return {string}
 */
const initThemeJs = themeName => `//import $ from "jquery";
//import * as utils from "@coremedia/brick-utils";

// --- JQUERY DOCUMENT READY -------------------------------------------------------------------------------------------

//$(function () {
//  utils.log("Theme ${themeName} is used.");
//});
`;

/**
 * returns content for preview.js
 * @param  {string} themeName
 * @return {string}
 */
const initPreviewJs = themeName => `/*! Theme ${themeName}: Preview JS */
// add preview specific code here...
`;

module.exports = {
  initPackageJson,
  initThemePrettierignore,
  initWebpackConfigJs,
  initThemeConfigJson,
  initPartialsSass,
  initVariablesSass,
  initThemeSass,
  initPreviewSass,
  initThemeIndexJs,
  initThemeJs,
  initPreviewJs,
};
