"use strict";

const {
  initPackageJson,
  initBrickIndexJs,
  initBrickInitJs,
  initBrickJs,
  initBrickPartialsScss,
  initBrickVariablesScss,
  initBrickCustomPartialsScss,
  initBrickCustomVariablesScss,
  initBrickPrettierignore,
  initBrickPageBodyFtl,
  initBrickDeProperties,
  initBrickEnProperties,
} = require("../brickData");

describe("initPackageJson()", () => {
  it("returns data for package.json", () => {
    expect(initPackageJson("test")).toMatchSnapshot();
  });
});

describe("initBrickIndexJs()", () => {
  it("returns data for index.js", () => {
    expect(initBrickIndexJs("test")).toMatchSnapshot();
  });
});

describe("initBrickInitJs()", () => {
  it("returns data for init.js", () => {
    expect(initBrickInitJs("test")).toMatchSnapshot();
  });
});

describe("initBrickJs()", () => {
  it("returns data for <brickName>.js", () => {
    expect(initBrickJs()).toMatchSnapshot();
  });
});

describe("initBrickPartialsScss()", () => {
  it("returns data for _partials.scss", () => {
    expect(initBrickPartialsScss("test")).toMatchSnapshot();
  });
});

describe("initBrickVariablesScss()", () => {
  it("returns data for _variables.scss", () => {
    expect(initBrickVariablesScss("test")).toMatchSnapshot();
  });
});

describe("initBrickCustomPartialsScss()", () => {
  it("returns data for partials/<brickName>.scss", () => {
    expect(initBrickCustomPartialsScss()).toMatchSnapshot();
  });
});

describe("initBrickCustomVariablesScss()", () => {
  it("returns data for variables/<brickName>.scss", () => {
    expect(initBrickCustomVariablesScss()).toMatchSnapshot();
  });
});

describe("initBrickPageBodyFtl()", () => {
  it("returns data for Page._body.ftl", () => {
    expect(initBrickPageBodyFtl()).toMatchSnapshot();
  });
});

describe("initBrickPrettierignore()", () => {
  it("returns data for .prettierignore", () => {
    expect(initBrickPrettierignore()).toMatchSnapshot();
  });
});

describe("initBrickDeProperties()", () => {
  it("returns data for <brickName>_de.properties", () => {
    expect(initBrickDeProperties()).toMatchSnapshot();
  });
});

describe("initBrickEnProperties()", () => {
  it("returns data for <brickName>_en.properties", () => {
    expect(initBrickEnProperties()).toMatchSnapshot();
  });
});
