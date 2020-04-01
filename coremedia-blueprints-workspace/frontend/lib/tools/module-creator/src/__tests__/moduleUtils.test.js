"use strict";

jest.mock("fs");

const log = {
  debug: jest.fn(),
  info: jest.fn(),
  warn: jest.fn(),
  error: jest.fn(),
  success: jest.fn(),
};

const wsConfig = {
  themesPath: "/path/to/themes/",
  bricksPath: "/path/to/bricks/",
};

describe("convertModuleName()", () => {
  const { convertModuleName } = require("../moduleUtils");
  it("returns converted theme name", () => {
    const themeName =
      ' 1234567890_-Ugly Theme Name! äöüß^°!"§$%&/()=?´`*+#.:,;<>@„¡“¶¢[]|{}”≠¿±‘’æœ•–…∞µ~√ç≈¥≤åº∆π¨Ω†®€∑«»';
    const expected = "1234567890-uglythemename";
    expect(convertModuleName(themeName)).toEqual(expected);
  });
  it("returns empty string", () => {
    const themeName =
      ' äöüß^°!"§$%&/()=?´`*+#.:,;<>@„¡“¶¢[]|{}”≠¿±‘’æœ•–…∞µ~√ç≈¥≤åº∆π¨Ω†®€∑«»';
    expect(convertModuleName(themeName)).toEqual("");
  });
  it("returns empty string", () => {
    const themeName = [1, 2];
    expect(convertModuleName(themeName)).toEqual("");
  });
  it("returns empty string", () => {
    const themeName = {};
    expect(convertModuleName(themeName)).toEqual("");
  });
});

describe("isModuleNameInUse()", () => {
  beforeEach(() => {
    jest.resetModules();
    require("fs").__setMockFiles([
      "/path/to/themes/aurora-theme",
      "/path/to/themes/corporate-theme",
      "/path/to/themes/hybris-theme",
    ]);
  });

  it("returns true", () => {
    const { isModuleNameInUse } = require("../moduleUtils");
    expect(isModuleNameInUse("/path/to/themes/aurora-theme")).toBe(true);
  });
  it("returns false", () => {
    const { isModuleNameInUse } = require("../moduleUtils");
    expect(isModuleNameInUse("/path/to/themes/newest-theme")).toBe(false);
  });
});

describe("createThemeFolderStructure()", () => {
  it("creates folder structure of a new theme", () => {
    const fs = require("fs");
    fs.__resetMockDirectories();

    const { createThemeFolderStructure } = require("../moduleUtils");
    createThemeFolderStructure(wsConfig, "/path/to/themes/newest-theme", log);
    const directories = fs.__getMockDirectories();
    expect(directories).toMatchSnapshot();
  });
});

describe("createBrickFolderStructure()", () => {
  it("creates folder structure of a new brick", () => {
    const fs = require("fs");
    fs.__resetMockDirectories();

    const { createBrickFolderStructure } = require("../moduleUtils");
    createBrickFolderStructure(wsConfig, "/path/to/bricks/newest-brick", log);
    const directories = fs.__getMockDirectories();
    expect(directories).toMatchSnapshot();
  });
});

describe("createThemeFiles()", () => {
  it("creates files of a new theme including bricks", () => {
    const fs = require("fs");
    fs.__resetMockFiles();

    const { createThemeFiles } = require("../moduleUtils");
    createThemeFiles(
      "/path/to/themes/newest-theme",
      "newest",
      { "active-brick": "^1.0.0" },
      { "some-brick": "^1.0.0" },
      "",
      log
    );
    const files = fs.__getMockFiles();
    expect(files).toMatchSnapshot();
  });
});

describe("createBrickFiles()", () => {
  it("creates files of a new brick", () => {
    const fs = require("fs");
    fs.__resetMockFiles();

    const { createBrickFiles } = require("../moduleUtils");
    createBrickFiles("/path/to/brick/newest-brick", "newest", log);
    const files = fs.__getMockFiles();
    expect(files).toMatchSnapshot();
  });
});

describe("createTheme()", () => {
  it("creates folder structure and files of a new empty theme", () => {
    const fs = require("fs");
    fs.__resetMockDirectories();
    fs.__resetMockFiles();

    const { createTheme } = require("../moduleUtils");
    createTheme(
      wsConfig,
      "/path/to/themes/newest-theme",
      "newest",
      {},
      {},
      "",
      log
    );

    const directories = fs.__getMockDirectories();
    const files = fs.__getMockFiles();

    expect(directories).toMatchSnapshot();
    expect(files).toMatchSnapshot();
  });
});

describe("createTheme() with parent theme", () => {
  it("creates folder structure and files of a new theme", () => {
    const fs = require("fs");
    fs.__resetMockDirectories();
    fs.__resetMockFiles();

    const { createTheme } = require("../moduleUtils");
    createTheme(
      wsConfig,
      "/path/to/themes/newest-theme",
      "newest",
      { "parent-theme": "^1.0.0" },
      {},
      "parent-theme",
      log
    );

    const directories = fs.__getMockDirectories();
    const files = fs.__getMockFiles();

    expect(directories).toMatchSnapshot();
    expect(files).toMatchSnapshot();
  });
});

describe("createBrick()", () => {
  it("creates folder structure and files of a new brick", () => {
    const fs = require("fs");
    fs.__resetMockDirectories();
    fs.__resetMockFiles();

    const { createBrick } = require("../moduleUtils");
    createBrick(wsConfig, "/path/to/brick/newest-brick", "newest", log);

    const directories = fs.__getMockDirectories();
    const files = fs.__getMockFiles();

    expect(directories).toMatchSnapshot();
    expect(files).toMatchSnapshot();
  });
});
