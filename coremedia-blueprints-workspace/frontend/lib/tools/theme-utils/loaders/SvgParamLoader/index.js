const escapeStringRegexp = require("escape-string-regexp");
const loaderUtils = require("loader-utils");
const urlLoader = require("url-loader");

// based on https://www.w3.org/TR/SVGParamPrimer/

module.exports = function(src) {
  let transformedSrc = src;
  if (this.resourceQuery) {
    const queryParams = loaderUtils.parseQuery(this.resourceQuery);
    for (const key of Object.keys(queryParams)) {
      const value = queryParams[key];
      if (!value || typeof value !== "string") {
        continue;
      }
      const regExp = new RegExp(escapeStringRegexp(`param(${key})`), "g");
      transformedSrc = transformedSrc.replace(
        regExp,
        decodeURIComponent(value)
      );
    }
  }
  return urlLoader.call(this, transformedSrc);
};
