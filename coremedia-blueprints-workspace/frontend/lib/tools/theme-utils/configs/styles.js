const autoprefixer = require("autoprefixer");
const postcssObjectFitImages = require("postcss-object-fit-images");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const fs = require("fs");
const nodeSass = require("node-sass");
const path = require("path");

const {
  workspace: {
    generateCssFileNameFromEntryPointName,
    generateJsFileNameFromEntryPointName,
    getThemeConfig,
  },
} = require("@coremedia/tool-utils");
const deepMerge = require("./utils/deepMerge");
const {
  sassSmartImport,
  sassExcludeImport,
  sassImportOnce,
} = require("../importer/sass");

const themeConfig = getThemeConfig();

const PREVIEW_ENTRY_NAME = "preview";
const CSS_PATH = path.dirname(generateCssFileNameFromEntryPointName("test"));

// the ExtractTextPlugin does not transform the relative urls to files in css when using a different
// output path than the webpack output path, so we need to change the publicPath to be relative from
// the CSS_PATH to the webpack output path.
const EXTRACT_CSS_PUBLIC_PATH =
  CSS_PATH && CSS_PATH !== "." ? path.relative(CSS_PATH, "") + "/" : undefined;

// automatically add preview entry point

const relativePreviewScssPath = "src/sass/preview.scss";
const previewScssPath = path.resolve(themeConfig.path, relativePreviewScssPath);
if (fs.existsSync(previewScssPath)) {
  themeConfig.styles.push({
    type: "webpack",
    src: relativePreviewScssPath,
    entryPointName: PREVIEW_ENTRY_NAME,
    smartImport: "preview",
    include: false,
    ieExpression: null,
  });
}

// Create entry point(s)

const entry = {};

// 1) Theme entry point configuration

themeConfig.styles
  .filter(style => style.type === "webpack")
  .forEach(style => {
    const srcList = style.src instanceof Array ? style.src : [style.src];
    const chunkName = style.entryPointName;
    entry[chunkName] = srcList.map(src => path.resolve(themeConfig.path, src));
  });

// the miniCssExtractPlugin will generate empty JS files which should be removed if the filename is not used
// by a script bundle
const possiblyEmptyJsFiles = Object.keys(entry).map(
  generateJsFileNameFromEntryPointName
);

const neededJsFiles = themeConfig.scripts
  .filter(script => script.type === "webpack")
  .map(script => script.entryPointName)
  .map(generateJsFileNameFromEntryPointName);

const jsToBeRemoved = possiblyEmptyJsFiles.filter(
  possiblyEmptyJsFile => !neededJsFiles.includes(possiblyEmptyJsFile)
);

class MiniCssExtractWithCleanupPlugin extends MiniCssExtractPlugin {
  constructor(config) {
    super(config);
    this._jsToRemove = config.jsToRemove || [];
  }

  apply(compiler) {
    super.apply(compiler);
    compiler.hooks.emit.tapAsync(
      "CoreMedia Mini Css Extract With Cleanup Plugin",
      (compilation, callback) => {
        this._jsToRemove.forEach(jsToRemove => {
          compilation.assets.hasOwnProperty(jsToRemove) &&
            delete compilation.assets[jsToRemove];
        });
        callback();
      }
    );
  }
}

const cssFileName = generateCssFileNameFromEntryPointName("[name]");
const miniCssExtractPlugin = new MiniCssExtractWithCleanupPlugin({
  filename: cssFileName,
  // needs to be set in case chunk splitting is used (otherwise the name would contain an unstable id)
  chunkFilename: cssFileName,
  // remove empty js files
  jsToRemove: jsToBeRemoved,
});

module.exports = ({ dependencyCheckPlugin, mode }) => config =>
  deepMerge(config, {
    entry: entry,
    module: {
      rules: [
        // CSS
        {
          test: /\.scss$/,
          use: [
            {
              loader: MiniCssExtractWithCleanupPlugin.loader,
              options: {
                publicPath: EXTRACT_CSS_PUBLIC_PATH,
              },
            },
            {
              loader: "css-loader",
              options: {
                sourceMap: mode === "development",
              },
            },
            {
              loader: "postcss-loader",
              options: {
                sourceMap: mode === "development",
                plugins: [
                  autoprefixer({
                    // enable css-grid for IE
                    grid: true,
                  }),
                  postcssObjectFitImages,
                ],
              },
            },
            {
              loader: "resolve-url-loader",
              options: {
                sourceMap: mode === "development",
                keepQuery: true,
              },
            },
            {
              loader: "sass-loader",
              options: {
                sourceMap: true, // needed for resolve-url-loader, removed in css-loader
                sassOptions: {
                  outputStyle: "expanded",
                  importer: [
                    sassSmartImport,
                    sassExcludeImport,
                    dependencyCheckPlugin.getNodeSassImporter(),
                    sassImportOnce,
                  ],
                  functions: {
                    "encodeBase64($string)": function($string) {
                      const buffer = new Buffer($string.getValue());
                      return nodeSass.types.String(buffer.toString("base64"));
                    },
                    "encodeURIComponent($string)": function($string) {
                      return nodeSass.types.String(
                        encodeURIComponent($string.getValue())
                      );
                    },
                  },
                  precision: 10,
                },
              },
            },
          ],
        },
      ],
    },
    plugins: [miniCssExtractPlugin],
  });
