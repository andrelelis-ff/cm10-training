"use strict";

const cmLogger = jest.genMockFromModule("@coremedia/cm-logger");

cmLogger.getLogger = jest.fn().mockReturnValue({
  debug: jest.fn().mockName("debug"),
  info: jest.fn().mockName("info"),
  warn: jest.fn().mockName("warn"),
  error: jest.fn().mockName("error"),
  success: jest.fn().mockName("success"),
});

module.exports = cmLogger;
