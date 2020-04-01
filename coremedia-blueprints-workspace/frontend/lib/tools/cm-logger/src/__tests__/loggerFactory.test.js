"use strict";

const loggerFactory = require("../loggerFactory");

describe("loggerFactory()", () => {
  beforeEach(() => {
    global.console.log = jest.fn().mockName("console.log");
    global.console.info = jest.fn().mockName("console.info");
    global.console.warn = jest.fn().mockName("console.warn");
    global.console.error = jest.fn().mockName("console.error");
  });
  it("returns a logger object", () => {
    const log = loggerFactory({ id: "id1234567890" });
    expect(log).toMatchSnapshot();
  });
  describe("debug()", () => {
    it("logs a debug message using console.log", () => {
      const log = loggerFactory({ level: "debug" });
      log.debug("debug message");
      expect(global.console.log).toHaveBeenCalled();
    });
    ["INFO", "WARN", "ERROR", "SILENT"].forEach(level => {
      it(`suppresses a debug message when using level "${level.toLowerCase()}"`, () => {
        const log = loggerFactory({ level });
        log.debug("debug message");
        expect(global.console.log).not.toHaveBeenCalled();
      });
    });
  });
  describe("info()", () => {
    it("logs an info message using console.info", () => {
      const log = loggerFactory({ level: "info" });
      log.info("info message");
      expect(global.console.info).toHaveBeenCalled();
    });
    ["WARN", "ERROR", "SILENT"].forEach(level => {
      it(`suppresses an info message when using level "${level.toLowerCase()}"`, () => {
        const log = loggerFactory({ level });
        log.info("info message");
        expect(global.console.info).not.toHaveBeenCalled();
      });
    });
  });
  describe("warn()", () => {
    it("logs a warning message using console.warn", () => {
      const log = loggerFactory({ level: "warn" });
      log.warn("warn message");
      expect(global.console.warn).toHaveBeenCalled();
    });
    ["ERROR", "SILENT"].forEach(level => {
      it(`suppresses a warning message when using level "${level.toLowerCase()}"`, () => {
        const log = loggerFactory({ level });
        log.warn("warning message");
        expect(global.console.warn).not.toHaveBeenCalled();
      });
    });
  });
  describe("error()", () => {
    it("logs an error message using console.error", () => {
      const log = loggerFactory({ level: "error" });
      log.error("error message");
      expect(global.console.error).toHaveBeenCalled();
    });
    it("suppresses an error message", () => {
      const log = loggerFactory({ level: "silent" });
      log.error("error message");
      expect(global.console.error).not.toHaveBeenCalled();
    });
  });
  describe("success()", () => {
    it("logs a success message using console.info", () => {
      const log = loggerFactory({ level: "info" });
      log.success("success message");
      expect(global.console.info).toHaveBeenCalled();
    });
    it("suppresses a success message", () => {
      const log = loggerFactory({ level: "silent" });
      log.success("success message");
      expect(global.console.info).not.toHaveBeenCalled();
    });
  });
  describe("buildPrefix()", () => {
    it("returns the prefix string corresponding to the given configuration", () => {
      const { buildPrefix } = loggerFactory;

      const value = buildPrefix("info", {
        level: "info",
        name: "LoggerName",
        prefix: {
          level: opts => `[${opts.level}]`,
          name: opts => opts.name,
          template: `[{{time}}] {{level}} `,
          time: () => {
            const date = new Date("2018-01-17T18:00:00");
            return `${date.getHours()}:${date.getMinutes()}:${date.getSeconds()}`;
          },
        },
      });
      expect(value).toMatchSnapshot();
    });
    it("returns the prefix string corresponding to the given configuration with unresolved placeholders", () => {
      const { buildPrefix } = loggerFactory;

      const value = buildPrefix("warn", {
        level: "warn",
        name: "LoggerName",
        prefix: {
          level: opts => `[${opts.level}]`,
          name: opts => opts.name,
          template: `[{{time}}] {{level}} ｢{{name}}｣ {{unresolved}} `,
          time: () => {
            const date = new Date("2018-01-17T18:00:00");
            return `${date.getHours()}:${date.getMinutes()}:${date.getSeconds()}`;
          },
        },
      });
      expect(value).toMatchSnapshot();
    });
  });
});
