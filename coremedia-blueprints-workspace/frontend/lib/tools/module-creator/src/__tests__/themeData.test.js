"use strict";

const {
  initPackageJson,
  initWebpackConfigJs,
  initThemeConfigJson,
  initThemePrettierignore,
  initThemeSass,
  initPreviewSass,
  initThemeIndexJs,
  initThemeJs,
  initPreviewJs,
} = require("../themeData");

describe("initPackageJson()", () => {
  it("returns data for package.json", () => {
    expect(
      initPackageJson(
        "test",
        "src/js/index.js",
        {},
        {
          "some-brick": "^1.0.0",
          "some-other-brick": "^1.0.0",
        }
      )
    ).toMatchSnapshot();
  });
});

describe("initThemePrettierignore()", () => {
  it("returns data for .prettierignore", () => {
    expect(initThemePrettierignore()).toMatchSnapshot();
  });
});

describe("initWebpackConfigJs()", () => {
  it("returns data for webpack.config.js", () => {
    expect(initWebpackConfigJs()).toMatchSnapshot();
  });
});

describe("initThemeConfigJson()", () => {
  it("returns data for theme descriptor", () => {
    expect(initThemeConfigJson("test", false)).toMatchSnapshot();
  });
});

describe("initThemeSass()", () => {
  it("returns data for <themeName>.sass", () => {
    expect(initThemeSass("test")).toMatchSnapshot();
  });
});

describe("initPreviewSass()", () => {
  it("returns data for preview.sass", () => {
    expect(initPreviewSass("test")).toMatchSnapshot();
  });
});

describe("initThemeIndexJs()", () => {
  it("returns data for index.js", () => {
    expect(initThemeIndexJs("test")).toMatchSnapshot();
  });
});

describe("initThemeJs()", () => {
  it("returns data for <themeName>.js", () => {
    expect(initThemeJs("test")).toMatchSnapshot();
  });
});

describe("initPreviewJs()", () => {
  it("returns data for preview.js", () => {
    expect(initPreviewJs("test")).toMatchSnapshot();
  });
});
