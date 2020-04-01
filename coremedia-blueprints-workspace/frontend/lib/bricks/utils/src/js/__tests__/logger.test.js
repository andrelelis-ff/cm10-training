import * as Logger from "../logger";

describe("Logger", () => {
  beforeEach(() => {
    global.console.log = jest.fn();
  });

  describe("setLevel()", () => {
    it("should set logging level", () => {
      const level = Logger.LEVEL.WARN;
      expect(Logger.setLevel(level)).toEqual(level);
    });
    it("should throw TypeError, if argument is not numeric", () => {
      //noinspection JSCheckFunctionSignatures
      expect(() => Logger.setLevel("XXX")).toThrow(TypeError);
    });
  });
  describe("setPrefix()", () => {
    it("should set prefix", () => {
      const prefix = "[LoggerTest]";
      expect(Logger.setPrefix(prefix)).toEqual(prefix);
    });
    it("should throw TypeError, if argument is not a string", () => {
      //noinspection JSCheckFunctionSignatures
      expect(() => Logger.setPrefix(123)).toThrow(TypeError);
    });
  });
  describe("getPrefix()", () => {
    it("should return prefix", () => {
      const prefix = "[LoggerTest]";
      Logger.setPrefix(prefix);
      expect(Logger.getPrefix()).toEqual(prefix);
    });
  });
  describe("log()", () => {
    it("should print a log message to the console", () => {
      const msg = "This is a log message.";
      Logger.setLevel(Logger.LEVEL.LOG);
      Logger.log(msg);
      // Logger.setLevel() executes a console.log!
      expect(global.console.log).toHaveBeenCalledTimes(2);
      expect(console.log).toHaveBeenCalledWith(Logger.getPrefix(), msg);
    });
    it("should not print a log message to the console, if logging level doesn´t match", () => {
      const msg = "This is a log message.";
      Logger.setLevel(Logger.LEVEL.INFO);
      Logger.log(msg);
      // Logger.setLevel() executes a console.log!
      expect(console.log).toHaveBeenCalledTimes(1);
      expect(console.log).not.toHaveBeenCalledWith(Logger.getPrefix(), msg);
    });
  });
  describe("info()", () => {
    beforeEach(() => {
      global.console.info = jest.fn();
    });

    it("should print an info message to the console", () => {
      const msg = "This is an info message.";
      Logger.setLevel(Logger.LEVEL.INFO);
      Logger.info(msg);
      expect(console.info).toHaveBeenCalledTimes(1);
      expect(console.info).toHaveBeenCalledWith(Logger.getPrefix(), msg);
    });
    it("should not print an info message to the console, if logging level doesn´t match", () => {
      const msg = "This is an info message.";
      Logger.setLevel(Logger.LEVEL.WARN);
      Logger.info(msg);
      expect(console.info).not.toHaveBeenCalled();
    });
  });
  describe("warn()", () => {
    beforeEach(() => {
      global.console.warn = jest.fn();
    });

    it("should print an warning message to the console", () => {
      const msg = "This is a warning message.";
      Logger.setLevel(Logger.LEVEL.WARN);
      Logger.warn(msg);
      expect(console.warn).toHaveBeenCalledTimes(1);
      expect(console.warn).toHaveBeenCalledWith(Logger.getPrefix(), msg);
    });
    it("should not print a warning message to the console, if logging level doesn´t match", () => {
      const msg = "This is a warning message.";
      Logger.setLevel(Logger.LEVEL.ERROR);
      Logger.warn(msg);
      expect(console.warn).not.toHaveBeenCalled();
    });
  });
  describe("error()", () => {
    beforeEach(() => {
      global.console.error = jest.fn();
    });

    it("should print an error message to the console", () => {
      const msg = "This is an error message.";
      Logger.setLevel(Logger.LEVEL.ERROR);
      Logger.error(msg);
      expect(console.error).toHaveBeenCalledTimes(1);
      expect(console.error).toHaveBeenCalledWith(Logger.getPrefix(), msg);
    });
    it("should not print an error message to the console, if logging level doesn´t match", () => {
      const msg = "This is an error message.";
      Logger.setLevel(Logger.LEVEL.OFF);
      Logger.error(msg);
      expect(console.error).not.toHaveBeenCalled();
    });
  });
});
