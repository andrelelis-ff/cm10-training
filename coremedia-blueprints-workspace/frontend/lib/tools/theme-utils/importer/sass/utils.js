const fs = require("fs");
const path = require("path");

const {
  workspace: { getInstallationPath },
} = require("@coremedia/tool-utils");

const SassNameExpander = require("./SassNameExpander");

const prefixPattern = /^~/;

function resolveScssModuleImport(url, prev) {
  const originalUrl = url;
  if (prefixPattern.test(url)) {
    url = url.replace(prefixPattern, "");

    const modulePattern = /^((@[^\/]+\/)*[^\/])+/;
    if (modulePattern.test(url)) {
      url = url.replace(modulePattern, moduleName =>
        getInstallationPath(moduleName, prev)
      );
    } else {
      throw new Error(
        `Could not resolve url: "${originalUrl}" which was marked for exclude.`
      );
    }
  }
  return url;
}

function resolveScssByNameExpander(url) {
  const nameExpander = new SassNameExpander(path.basename(url));
  nameExpander.addLocation(path.dirname(url));
  const possibleFiles = nameExpander.files.values();
  let result;

  while ((result = possibleFiles.next().value)) {
    if (fs.existsSync(result)) {
      result = fs.realpathSync(result);
      break;
    }
  }

  return result;
}

function resolveScss(url, prev) {
  const importedFromPath = path.dirname(prev);
  url = resolveScssModuleImport(url, prev);

  const absolutePath = path.resolve(importedFromPath, url);
  return resolveScssByNameExpander(absolutePath);
}

module.exports = {
  resolveScss,
};
