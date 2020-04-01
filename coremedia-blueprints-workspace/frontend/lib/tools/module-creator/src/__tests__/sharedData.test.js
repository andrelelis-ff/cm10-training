"use strict";

const { initPrettierrc } = require("../sharedData");

describe("initPrettierrc()", () => {
  it("returns data for .prettierrc", () => {
    expect(initPrettierrc()).toMatchSnapshot();
  });
});
