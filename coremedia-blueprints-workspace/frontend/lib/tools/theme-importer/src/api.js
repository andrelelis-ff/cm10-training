"use strict";

const fs = require("fs");

class HttpError extends Error {
  constructor(code, message) {
    super(message);
    this.code = code;
    Error.captureStackTrace(this, HttpError);
  }
}

/**
 * Returns a Promise for executing a request.
 * @param {Object} options
 * @returns {Promise}
 * @private
 */
const request = options => {
  const request = require("request");
  return new Promise((resolve, reject) => {
    try {
      request(options, (error, response, body) => {
        const result = { error, response, body };

        if (error) {
          reject(error);
        } else if (response.statusCode >= 200 && response.statusCode < 400) {
          resolve(result);
        } else {
          let httpError;

          if (response.statusCode === 401) {
            httpError = new HttpError(
              "EUNAUTHORIZED",
              `${response.statusCode} ${response.statusMessage}: ${
                /login$/.test(options.url)
                  ? "Invalid username or password."
                  : "Your API key is invalid and has been removed. Please login again."
              }`
            );
          } else if (response.statusCode === 404) {
            httpError = new HttpError(
              "ENOTFOUND",
              `${response.statusCode} ${response.statusMessage}: The server has not found anything matching the Request-URI. Please check the specified Studio URL.`
            );
          } else if (response.statusCode === 409) {
            let errors = JSON.parse(body);
            httpError = new HttpError(
              "ECONFLICT",
              `${response.statusCode}: Could not upload theme because of problems with following files: 
                  ${errors.failedPaths}
                  Please check the status of the files in Studio.`
            );
          } else if (response.statusCode < 200 || response.statusCode > 399) {
            let cause;
            try {
              if (body) {
                cause = JSON.parse(body).cause;
              }
            } catch (error) {
              // no cause available
            }
            httpError = new HttpError(
              "EMISC",
              `${response.statusCode} ${
                response.statusMessage
              }: Please contact your system administrator. ${cause &&
                `, cause: ${cause}`}`
            );
          }
          reject(httpError);
        }
      });
    } catch (e) {
      reject(e);
    }
  });
};

/**
 * Returns an options object to be passed to request function.
 * @param {string} url
 * @param {Object} opts
 * @returns {Object}
 * @private
 */
const getOptions = (url, opts) => {
  const options = {
    url,
    strictSSL: false, // disable strictSSL for own certificates
    followAllRedirects: true,
    followOriginalHttpMethod: true,
    method: "POST",
  };
  if (opts && typeof opts === "object") {
    if (opts.proxy && typeof opts.proxy === "string") {
      Object.assign(options, {
        proxy: opts.proxy,
      });
    }
    if (opts.auth && typeof opts.auth === "object") {
      Object.assign(options, {
        auth: opts.auth,
      });
    }
    if (opts.apiKey && typeof opts.apiKey === "string") {
      Object.assign(options, {
        headers: {
          Authorization: `CMAPIKey ${opts.apiKey}`,
        },
      });
    }
    if (opts.formData && typeof opts.formData === "object") {
      Object.assign(options, {
        formData: opts.formData,
      });
    }
  }
  return options;
};

/**
 * Returns a Promise for requesting an API key.
 * @param {string} url - the base URL
 * @param {string} proxy - the proxy URL
 * @param {string} username - the user name
 * @param {string} password - the password or token
 * @returns {Promise} - promise resolving to object with url and apiKey attributes
 */
const login = (url, proxy, username, password) =>
  getBackendData(url, proxy).then(backendData => {
    if (backendData && backendData.cognitoPoolData) {
      return authenticateCognito(
        backendData.cognitoPoolData,
        username,
        password
      ).then(value => loginCMS(backendData.url, proxy, username, value));
    } else {
      return loginCMS(backendData.url, proxy, username, password);
    }
  });

/**
 * Returns a Promise for requesting an API key from the CMS
 * using the given user name and password.
 * If a Cognito login is used, the password must be a Cognito
 * token.
 *
 * @param {string} url - the base URL
 * @param {string} proxy - the proxy URL
 * @param {string} username - the user name
 * @param {string} password - the password or token
 * @returns {Promise} - promise resolving to object with url and apiKey attributes
 */
const loginCMS = (url, proxy, username, password) => {
  return new Promise((resolve, reject) => {
    try {
      const options = getOptions(`${url}/api/themeImporter/login`, {
        proxy,
        auth: {
          user: username,
          pass: password,
        },
      });

      request(options)
        .then(value => {
          resolve({
            url,
            apiKey: value.body,
          });
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
 * Returns a Promise for determining the Cognito user pool configuration.
 *
 * @param {string} url - the base URL
 * @param {string} proxy - the proxy URL
 * @returns {Promise} - promise resolving to object with url and cognitoPoolData attributes
 */
const getBackendData = (url, proxy) => {
  return new Promise((resolve, reject) => {
    try {
      const options = getOptions(`${url}/api/themeImporter/config`, {
        proxy,
      });

      request(options)
        .then(value => {
          const { cognitoPoolData } = JSON.parse(value.body);
          resolve({
            url,
            cognitoPoolData,
          });
        })
        .catch(() => {
          const modernUrl = `${url}/rest`;
          const options = getOptions(`${modernUrl}/api/themeImporter/config`, {
            proxy,
          });

          request(options)
            .then(value => {
              const { cognitoPoolData } = JSON.parse(value.body);
              resolve({
                url: modernUrl,
                cognitoPoolData,
              });
            })
            .catch(reject);
        });
    } catch (e) {
      reject(e);
    }
  });
};

/**
 * Returns a Promise for retrieving an authentication token from Cognito.
 *
 * @param {ICognitoUserPoolData} poolData - the configuration of the Cognito user pool
 * @param {string} username - the user name
 * @param {string} password - the password
 * @returns {Promise}
 */
const authenticateCognito = (poolData, username, password) => {
  const AmazonCognitoIdentity = require("amazon-cognito-identity-js");

  return new Promise((resolve, reject) => {
    const authenticationData = {
      Username: username,
      Password: password,
    };
    const authenticationDetails = new AmazonCognitoIdentity.AuthenticationDetails(
      authenticationData
    );
    const userPool = new AmazonCognitoIdentity.CognitoUserPool(poolData);
    const userData = {
      Username: username,
      Pool: userPool,
    };
    const cognitoUser = new AmazonCognitoIdentity.CognitoUser(userData);

    const callback = {
      onSuccess: result => {
        const accessToken = result.getAccessToken().getJwtToken();
        resolve(accessToken);
      },
      onFailure: error => {
        reject(error);
      },
      newPasswordRequired: (/*userAttributes, requiredAttributes*/) => {
        reject("Please log into the Cloud Manager once.");
      },
    };

    cognitoUser.authenticateUser(authenticationDetails, callback);
  });
};

/**
 * Returns a Promise for requesting a logout.
 * @param {string} url
 * @param {string} proxy
 * @param {string} apiKey
 * @returns {Promise}
 */
const logout = (url, proxy, apiKey) => {
  return new Promise((resolve, reject) => {
    const options = getOptions(`${url}/api/themeImporter/logout`, {
      proxy,
      apiKey,
    });

    request(options)
      .then(() => {
        try {
          resolve();
        } catch (e) {
          reject(
            new Error(
              "API key couldn´t be deleted on local disk, but has been invalidated on server side."
            )
          );
        }
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * Returns a Promise for requesting a user verification.
 * @param {string} url
 * @param {string} proxy
 * @param {string} apiKey
 * @returns {Promise}
 */
const whoami = (url, proxy, apiKey) => {
  return new Promise((resolve, reject) => {
    const options = getOptions(`${url}/api/themeImporter/whoami`, {
      proxy,
      apiKey,
    });
    request(options)
      .then(value => {
        resolve(JSON.parse(value.body));
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * Returns a Promise for requesting a theme upload.
 * @param {string} url
 * @param {string} proxy
 * @param {string} apiKey
 * @param {string} file
 * @param {string} [clean=false]
 * @returns {Promise}
 */
const upload = (url, proxy, apiKey, file, clean = "false") => {
  return new Promise((resolve, reject) => {
    const options = getOptions(`${url}/api/themeImporter/upload`, {
      proxy,
      apiKey,
      formData: {
        path: "/Themes",
        clean,
        file: fs.createReadStream(file),
      },
    });

    request(options)
      .then(() => {
        resolve();
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * Returns a Promise for requesting a theme upload for deployment.
 * @param {string} url
 * @param {string} proxy
 * @param {string} apiKey
 * @param {string} file
 * @returns {Promise}
 */
const deploy = (url, proxy, apiKey, file) => {
  return new Promise((resolve, reject) => {
    const options = getOptions(`${url}/api/themeImporter/deploy`, {
      proxy,
      apiKey,
      formData: {
        path: "/Themes",
        file: fs.createReadStream(file),
      },
    });

    request(options)
      .then(() => {
        resolve();
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * Returns a Promise for requesting a file delete.
 * @param {string} url
 * @param {string} proxy
 * @param {string} apiKey
 * @param {string} file
 * @returns {Promise}
 */
const deleteFile = (url, proxy, apiKey, file) => {
  return new Promise((resolve, reject) => {
    const options = getOptions(`${url}/api/themeImporter/delete`, {
      proxy,
      apiKey,
      formData: {
        path: "/Themes",
        file,
      },
    });

    request(options)
      .then(() => {
        resolve();
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * api module
 * @module
 */
module.exports = {
  login,
  logout,
  whoami,
  upload,
  deploy,
  deleteFile,
};
