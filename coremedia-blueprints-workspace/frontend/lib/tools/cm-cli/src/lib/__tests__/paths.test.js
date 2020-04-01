"use strict";

jest.mock("../constants");

describe("PACKAGE_MANAGER_EXECUTABLE()", () => {
  beforeEach(() => {
    jest.resetModules();
  });
  it("returns npm", () => {
    process.env.npm_execpath = "/path/to/npm-cli.js";
    const constants = require("../constants");
    constants.IS_WINDOWS = false;
    const { PACKAGE_MANAGER_EXECUTABLE } = require("../paths");
    expect(PACKAGE_MANAGER_EXECUTABLE).toEqual("npm");
  });
  it("returns yarn", () => {
    process.env.npm_execpath = "/path/to/yarn.js";
    const constants = require("../constants");
    constants.IS_WINDOWS = false;
    const { PACKAGE_MANAGER_EXECUTABLE } = require("../paths");
    expect(PACKAGE_MANAGER_EXECUTABLE).toEqual("yarn");
  });
  it("returns npm.cmd", () => {
    process.env.npm_execpath = "/path/to/npm-cli.js";
    const constants = require("../constants");
    constants.IS_WINDOWS = true;
    const { PACKAGE_MANAGER_EXECUTABLE } = require("../paths");
    expect(PACKAGE_MANAGER_EXECUTABLE).toEqual("npm.cmd");
  });
  it("returns yarn", () => {
    process.env.npm_execpath = "/path/to/yarn.js";
    const constants = require("../constants");
    constants.IS_WINDOWS = true;
    const { PACKAGE_MANAGER_EXECUTABLE } = require("../paths");
    expect(PACKAGE_MANAGER_EXECUTABLE).toEqual("yarn.cmd");
  });
});
