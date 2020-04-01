const closestPackage = require("closest-package");
const fs = require("fs");
const glob = require("glob");
const path = require("path");
const { packages } = require("@coremedia/tool-utils");

const NAME = "CoreMedia View Repository Plugin";
const RELATIVE_TEMPLATES_PATH = path.join("src", "templates");

class ViewRepositoryMapping {
  constructor(name, acceptResource) {
    this.name = name;
    this.acceptResource = acceptResource || (() => true);
  }
}

let instance_number = 0;

class ViewRepositoryPlugin {
  constructor(config) {
    this._templateGlobPattern = config.templateGlobPattern || "**/*.*";
    this._targetPath = config.targetPath;
    this._mappings = config.mappings || [];
    if (
      !this._mappings.every(mapping => mapping instanceof ViewRepositoryMapping)
    ) {
      throw new Error(
        "All mappings have to be an instance of ViewRepositoryMapping!"
      );
    }
    this._directoryLoaderEntryName = `DirectoryLoader${++instance_number}.tmp`;
  }

  getEntry() {
    return {
      [this._directoryLoaderEntryName]: [
        require.resolve("../../loaders/DirectoryLoader") +
          `?directory=${encodeURIComponent(
            RELATIVE_TEMPLATES_PATH
          )}&pattern=${encodeURIComponent(this._templateGlobPattern)}!`,
      ],
    };
  }

  // cleanup templates (webpack does not support deletion in watch mode)
  apply(compiler) {
    this._outputPath = compiler.options.output.path;
    compiler.hooks.emit.tapAsync(NAME, (compilation, callback) => {
      const assets = compilation.assets;
      const newlyCreatedAssets = Object.keys(assets).map(
        key => assets[key].existsAt
      );
      const viewRepositoryTargetPaths = this._mappings.map(mapping =>
        path.join(this._targetPath, mapping.name)
      );

      glob
        .sync(this._templateGlobPattern, {
          cwd: this._targetPath,
          nodir: true,
        })
        // make absolute path
        .map(relativeFilePath => path.join(this._targetPath, relativeFilePath)) // filter out newly created assets
        .filter(
          absoluteFilePath => !newlyCreatedAssets.includes(absoluteFilePath)
        )
        // only consider deleting files that are part of a registered view repository (other theme builds might
        // share the targetPath)
        .filter(absoluteFilePath =>
          viewRepositoryTargetPaths.some(
            viewRepositoryTargetPath =>
              !path
                .relative(viewRepositoryTargetPath, absoluteFilePath)
                .startsWith(".")
          )
        )
        .forEach(fs.unlinkSync);

      // remove temporary JavaScript file
      const assetsToRemove = compilation.chunks
        .filter(chunk => chunk.name === this._directoryLoaderEntryName)
        .map(chunk => chunk.files)
        .reduce((allFiles, files) => allFiles.concat(files), []);

      assetsToRemove.forEach(file => {
        file in assets && delete assets[file];
      });

      callback();
    });
  }

  getLoaderConfig() {
    return {
      loader: "file-loader",
      options: {
        name: resourcePath => {
          const packageJsonPath = closestPackage.sync(resourcePath);
          const packageJson = packages.getJsonByFilePath(packageJsonPath);

          /**
           * @type {ViewRepositoryMapping}
           */
          const mapping = this._mappings.find(mapping =>
            mapping.acceptResource(resourcePath, packageJsonPath)
          );
          if (!mapping) {
            throw new Error(
              `No view repository found for template "${resourcePath}.`
            );
          }
          const relativePathFromPackage = path.relative(
            path.dirname(packageJsonPath),
            resourcePath
          );
          let middleSegment = path.relative(
            RELATIVE_TEMPLATES_PATH,
            relativePathFromPackage
          );
          // if the middle segment loads to a path outside the templates path, put it into freemarkerLibs
          if (middleSegment.startsWith("..")) {
            middleSegment = path.join(
              "freemarkerLibs",
              packageJson.name,
              path.basename(relativePathFromPackage)
            );
          }
          return path.join(mapping.name, middleSegment);
        },
        outputPath: url =>
          path.join(path.relative(this._outputPath, this._targetPath), url),
      },
    };
  }
}

module.exports = { ViewRepositoryPlugin, ViewRepositoryMapping };
