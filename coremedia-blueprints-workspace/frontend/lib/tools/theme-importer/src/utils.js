"use strict";

/**
 * Validate parameter theme.
 * @param {string} themeName
 * @return {boolean}
 */
const validateThemeName = themeName =>
  typeof themeName === "string" && themeName.length > 0;

/**
 * Validate parameter fileList.
 * @param {string[]} fileList
 * @return {boolean}
 */
const validateFileList = fileList =>
  fileList && Array.isArray(fileList) && fileList.length > 0;

/**
 * Validate parameter file.
 * @param {string} file
 * @return {boolean}
 */
const validateFile = file => typeof file === "string" && file.length > 0;

/**
 * Return Preview URL including userVariant Parameter for current user.
 * @param {string} previewUrl
 * @param {number} userId
 * @return {string}
 */
const getPreviewUrlDevMode = (previewUrl, userId) => {
  let previewUrlDevMode;
  if (previewUrl) {
    previewUrlDevMode = previewUrl.replace(/\/$/, "");
    const userVariantParam = `userVariant=${userId}`;

    if (/userVariant=[0-9]*/.test(previewUrlDevMode)) {
      // replace existing userVariant parameter to ensure that correct user id is provided
      previewUrlDevMode = previewUrlDevMode.replace(
        /userVariant=[0-9]*/,
        userVariantParam
      );
    } else {
      // append userVariant parameter
      const delimiter = /\?/.test(previewUrlDevMode) ? "&" : "?";
      previewUrlDevMode += `${delimiter}${userVariantParam}`;
    }
  }
  return previewUrlDevMode;
};

/**
 * utils module
 * @module
 */
module.exports = {
  validateThemeName,
  validateFileList,
  validateFile,
  getPreviewUrlDevMode,
};
