"use strict";

const path = require("path");
const fs = require("fs");
const cmLogger = require("@coremedia/cm-logger");
const {
  workspace: { getEnv, createApiKeyFile, removeApiKeyFile, getApiKey },
} = require("@coremedia/tool-utils");
const zipper = require("@coremedia/zipper");

const api = require("./api");
const utils = require("./utils");

const PKG_NAME = "@coremedia/theme-importer";

/**
 * Validate parameter theme.
 * @param {string} themeName
 */
const validateThemeName = utils.validateThemeName;

/**
 * Validate parameter fileList.
 * @param {string[]} fileList
 */
const validateFileList = utils.validateFileList;

/**
 * Validate parameter file.
 * @param {string} file
 */
const validateFile = utils.validateFile;

const log = cmLogger.getLogger({
  name: PKG_NAME,
  level: "info",
});

/**
 * Returns a Promise for requesting an API key.
 * @param {string} studioUrl
 * @param {string} previewUrl
 * @param {string} proxyUrl
 * @param {string} username
 * @param {string} password
 * @returns {Promise}
 */
const login = (studioUrl, previewUrl, proxyUrl, username, password) => {
  return new Promise((resolve, reject) => {
    const trimmedStudioUrl = studioUrl.replace(/\/$/, "");
    let finalStudioUrl;

    log.info(`Using ${trimmedStudioUrl}`);

    api
      .login(trimmedStudioUrl, proxyUrl, username, password)
      .then(result => {
        finalStudioUrl = result.url;
        if (trimmedStudioUrl !== finalStudioUrl) {
          log.info(`Studio running at ${finalStudioUrl}`);
        }
        createApiKeyFile(result.apiKey);
        return api.whoami(finalStudioUrl, proxyUrl, result.apiKey);
      })
      .then(user => {
        const previewUrlDevMode = utils.getPreviewUrlDevMode(
          previewUrl,
          user.id
        );
        resolve({
          studioApiUrl: finalStudioUrl,
          previewUrl: previewUrlDevMode,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * Returns a Promise for requesting a logout.
 * @returns {Promise}
 */
const logout = () => {
  return new Promise((resolve, reject) => {
    try {
      const { studioUrl, proxyUrl } = getEnv();
      const apiKey = getApiKey();

      log.debug(`Using ${studioUrl}`);

      api
        .logout(studioUrl, proxyUrl, apiKey)
        .then(() => {
          removeApiKeyFile();
          resolve("You have successfully been logged out.");
        })
        .catch(e => {
          reject(e);
        });
    } catch (e) {
      reject(e);
    }
  });
};

/**
 * Returns a Promise for requesting a user verification.
 * @returns {Promise}
 */
const whoami = () => {
  return new Promise((resolve, reject) => {
    try {
      const { studioUrl, proxyUrl } = getEnv();
      const apiKey = getApiKey();

      log.debug(`Using ${studioUrl}`);

      api
        .whoami(studioUrl, proxyUrl, apiKey)
        .then(user => {
          resolve(user);
        })
        .catch(e => {
          if (e.code === "EUNAUTHORIZED") {
            removeApiKeyFile();
          }
          reject(e);
        });
    } catch (e) {
      reject(e);
    }
  });
};

/**
 * Returns a Promise for requesting a theme upload.
 * @param {Object} themeConfig
 * @returns {Promise}
 */
const uploadTheme = themeConfig => {
  return new Promise((resolve, reject) => {
    try {
      const { studioUrl, proxyUrl } = getEnv();
      const apiKey = getApiKey();

      if (!fs.existsSync(themeConfig.themeArchiveTargetPath)) {
        reject(
          new Error(
            `[${PKG_NAME}] ${themeConfig.themeArchiveTargetPath} doesn´t exist.`
          )
        );
      } else {
        log.debug(`Using ${studioUrl}`);
        log.info("Uploading theme to remote server.");

        api
          .upload(
            studioUrl,
            proxyUrl,
            apiKey,
            themeConfig.themeArchiveTargetPath,
            "true"
          )
          .then(() => {
            resolve(themeConfig.themeArchiveTargetPath);
          })
          .catch(e => {
            if (e.code === "EUNAUTHORIZED") {
              removeApiKeyFile();
            }
            reject(e);
          });
      }
    } catch (e) {
      reject(e);
    }
  });
};

/**
 * Returns a Promise for requesting a theme upload.
 * @param {Object} themeConfig
 * @returns {Promise}
 */
const deployTheme = themeConfig => {
  return new Promise((resolve, reject) => {
    try {
      const { studioUrl, proxyUrl } = getEnv();
      const apiKey = getApiKey();

      if (!fs.existsSync(themeConfig.themeArchiveTargetPath)) {
        reject(
          new Error(
            `[${PKG_NAME}] ${themeConfig.themeArchiveTargetPath} doesn´t exist.`
          )
        );
      } else {
        log.debug(`Using ${studioUrl}`);
        log.info("Uploading and deploying theme to remote server.");

        api
          .deploy(
            studioUrl,
            proxyUrl,
            apiKey,
            themeConfig.themeArchiveTargetPath
          )
          .then(() => {
            resolve(themeConfig.themeArchiveTargetPath);
          })
          .catch(e => {
            if (e.code === "EUNAUTHORIZED") {
              removeApiKeyFile();
            }
            reject(e);
          });
      }
    } catch (e) {
      reject(e);
    }
  });
};

/**
 * Returns a Promise for requesting an upload of changed files.
 * @param {Object} themeConfig
 * @param {string[]} fileList
 * @param {string} [logLevel="info"]
 * @returns {Promise}
 */
const uploadFiles = (themeConfig, fileList, logLevel = "info") => {
  return new Promise((resolve, reject) => {
    try {
      const _log = cmLogger.getLogger({
        id: +new Date(),
        name: PKG_NAME,
        level: logLevel,
      });
      _log.info("Uploading files to server.");

      if (!utils.validateFileList(fileList)) {
        throw new Error(`[${PKG_NAME}] No files were provided.`);
      }

      const { studioUrl, proxyUrl } = getEnv();
      const apiKey = getApiKey();
      const patterns = fileList.map(file => {
        const context = file.includes(themeConfig.themeTargetPath)
          ? path.join(themeConfig.themeTargetPath, "..")
          : themeConfig.resourcesTargetPath;
        return {
          context,
          source: file.replace(context + path.sep, ""),
        };
      });

      zipper(patterns, {
        filepath: themeConfig.themeUpdateArchiveTargetPath,
        logLevel,
      })
        .then(count => {
          //_log.debug(`Using ${studioUrl}`);
          _log.debug(
            `Uploading file ${path.basename(
              themeConfig.themeUpdateArchiveTargetPath
            )}`
          );

          api
            .upload(
              studioUrl,
              proxyUrl,
              apiKey,
              themeConfig.themeUpdateArchiveTargetPath
            )
            .then(() => {
              resolve(count);
            })
            .catch(e => {
              reject(e);
            });
        })
        .catch(e => {
          reject(
            new Error(
              `[${PKG_NAME}] An error occured while preparing for upload: ${e.message}`
            )
          );
        });
    } catch (e) {
      reject(e);
    }
  });
};

/**
 * Returns a Promise for requesting a file delete.
 * @param {Object} themeConfig
 * @param {string} file
 * @returns {Promise}
 */
const deleteFile = (themeConfig, file) => {
  return new Promise(resolve => {
    try {
      log.info(`Deleting file ${file} on server.`);

      if (!utils.validateFile(file)) {
        throw new Error(`[${PKG_NAME}] No file was provided.`);
      }

      const { studioUrl, proxyUrl } = getEnv();
      const apiKey = getApiKey();
      const FILE_PATH = file.replace(
        themeConfig.themeTargetPath + path.sep,
        ""
      );

      log.debug(`Using ${studioUrl}`);

      api
        .deleteFile(studioUrl, proxyUrl, apiKey, FILE_PATH)
        .then(() => {
          resolve({
            type: "SUCCESS",
            file: path.basename(file),
          });
        })
        .catch(e => {
          resolve({
            type: "ERROR",
            file: path.basename(file),
            error: e.message,
          });
        });
    } catch (e) {
      resolve({
        type: "ERROR",
        file: path.basename(file),
        error: e.message,
      });
    }
  });
};

/**
 * remoteThemeImporter module
 * @module
 */
module.exports = {
  validateThemeName,
  validateFileList,
  validateFile,
  login,
  logout,
  whoami,
  uploadTheme,
  deployTheme,
  uploadFiles,
  deleteFile,
};
