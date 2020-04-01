"use strict";

const cmLogger = require("../index");

describe("cmLogger", () => {
  beforeEach(() => {
    global.console.log = jest.fn().mockName("console.log");
    global.console.info = jest.fn().mockName("console.info");
    global.console.warn = jest.fn().mockName("console.warn");
    global.console.error = jest.fn().mockName("console.error");
  });
  describe("defaultLogger", () => {
    it(`suppresses a debug message"`, () => {
      const msg = "debug message";
      cmLogger.debug(msg);
      expect(global.console.log).not.toHaveBeenCalled();
    });
    it("logs an info message using console.info", () => {
      const msg = "info message";
      cmLogger.info(msg);
      expect(global.console.info).toHaveBeenCalledWith(msg);
    });
    it("logs a warning message using console.warn", () => {
      const msg = "warning message";
      cmLogger.warn(msg);
      expect(global.console.warn).toHaveBeenCalledWith(msg);
    });
    it("logs an error message using console.error", () => {
      const msg = "error message";
      cmLogger.error(msg);
      expect(global.console.error).toHaveBeenCalledWith(msg);
    });
    it("logs a success message using console.info", () => {
      const msg = "info message";
      cmLogger.info(msg);
      expect(global.console.info).toHaveBeenCalledWith(msg);
    });
  });
  describe("getLogger()", () => {
    it('returns a new logger object with id "newLogger"', () => {
      const log = cmLogger.getLogger("newLogger");
      expect(log).toMatchSnapshot();
    });
    it("returns a new logger object providing an explicit id", () => {
      const log = cmLogger.getLogger({ name: "newLogger", id: "0123456789" });
      expect(log).toMatchSnapshot();
    });
    it('returns the cached logger object with id "cachedLogger"', () => {
      const logA = cmLogger.getLogger("cachedLogger");
      const logB = cmLogger.getLogger("cachedLogger");
      expect(logB).toEqual(logA);
    });
    it("returns a new logger object, if none of the cached loggers has the same id", () => {
      const logA = cmLogger.getLogger("cachedLogger");
      const logB = cmLogger.getLogger("newLogger");
      expect(logB).not.toEqual(logA);
    });
    it("throws a TypeError", () => {
      const error = new TypeError(
        "You must supply a name when creating a logger."
      );
      expect(() => cmLogger.getLogger()).toThrow(error);
    });
  });
  describe("getLevelFromWebpackStats()", () => {
    it("maps Webpack stats values to logging levels", () => {
      const LEVEL = {
        verbose: "debug",
        normal: "info",
        minimal: "warn",
        "errors-only": "error",
        none: "silent",
      };

      Object.keys(LEVEL).forEach(stat => {
        expect(cmLogger.getLevelFromWebpackStats(stat)).toEqual(LEVEL[stat]);
      });
    });
    it("returns undefined, if the stats value is unknown", () => {
      expect(cmLogger.getLevelFromWebpackStats("whatever")).toBe(undefined);
    });
  });
});
