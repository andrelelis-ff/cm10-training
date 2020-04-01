const nodeSass = require("node-sass");

const { resolveScss } = require("./utils");

module.exports = function(url, prev, done) {
  // Create an import cache if it doesn't exist
  if (!this._importOnceCache) {
    this._importOnceCache = {};
  }

  const resolvedAbsolutePath = resolveScss(url, prev);
  if (resolvedAbsolutePath && resolvedAbsolutePath in this._importOnceCache) {
    // already imported, return empty string as content
    done({
      contents: "",
    });
  } else {
    // add new file to cache
    this._importOnceCache[resolvedAbsolutePath] = true;
    // leave handling to other importers
    done(nodeSass.types.NULL);
  }
};
