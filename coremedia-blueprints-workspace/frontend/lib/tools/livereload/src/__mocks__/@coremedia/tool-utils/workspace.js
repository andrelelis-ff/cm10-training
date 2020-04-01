"use strict";

const { workspace } = jest.genMockFromModule("@coremedia/tool-utils");

workspace.getMonitorConfig = jest.fn().mockReturnValue({
  target: "remote",
  livereload: {
    host: "localhost",
    port: 35729,
  },
});

workspace.getCert = jest.fn().mockReturnValue("0123456789");

module.exports = workspace;
