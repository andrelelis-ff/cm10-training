"use strict";

module.exports = jest.fn().mockImplementation(() => ({
  server: {
    removeAllListeners: jest.fn(),
    on: jest.fn(),
  },
  listen: jest.fn(),
  changed: jest.fn(),
}));
