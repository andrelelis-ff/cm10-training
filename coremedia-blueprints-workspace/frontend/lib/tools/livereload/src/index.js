"use strict";

const tinylr = require("tiny-lr");
const cmLogger = require("@coremedia/cm-logger");

const {
  workspace: { getMonitorConfig, getCert },
} = require("@coremedia/tool-utils");

const PKG_NAME = "@coremedia/livereload;";

// Holds the servers out of scope in case watch is reloaded
let servers = Object.create(null);
let server;

const init = (logLevel = "warn") => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: logLevel,
  });

  try {
    const { livereload: lrConfig } = getMonitorConfig();
    const cert = getCert();
    const config = Object.assign({}, lrConfig, {
      key: cert,
      cert: cert,
    });

    const HOST = `${config.host}:${config.port}`;

    log.debug(`Initializing LiveReload server on ${HOST}.`);

    if (servers[HOST]) {
      server = servers[HOST];
    } else {
      server = tinylr(config);
      server.server.removeAllListeners("error");
      server.server.on("error", err => {
        if (err.code === "EADDRINUSE") {
          log.error(
            `Port ${config.port} is already in use by another process.`
          );
        } else {
          log.error(err);
        }
      });
      server.listen(config.port, config.host, err => {
        if (err) {
          log.error(err);
          return;
        }
        log.debug(`LiveReload server started on ${HOST}`);
      });
      servers[HOST] = server;
    }
  } catch (error) {
    log.error(error.message);
  }
};

const trigger = (files = [""]) => {
  const log = cmLogger.getLogger({
    name: PKG_NAME,
    level: "warn",
  });

  if (!server) {
    log.error(`LiveReload server has not been started.`);
    return;
  }
  log.debug(`Live reloading ${files.join(", ")}.`);
  server.changed({ body: { files: files } });
};

const getHost = () => Object.keys(servers)[0];

const getServers = () => servers;

const getServer = () => server;

module.exports = {
  PKG_NAME,
  init,
  trigger,
  getHost,
  getServers,
  getServer,
};
