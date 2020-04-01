"use strict";

const path = require("path");
const { IS_WINDOWS } = require("../lib/constants");

function getPackageManagerExecutable() {
  const npmExecPath = process.env.npm_execpath;
  if (path.basename(npmExecPath).toLowerCase() === "yarn.js") {
    return IS_WINDOWS ? "yarn.cmd" : "yarn";
  } else {
    return IS_WINDOWS ? "npm.cmd" : "npm";
  }
}

module.exports = {
  PACKAGE_MANAGER_EXECUTABLE: getPackageManagerExecutable(),
};
