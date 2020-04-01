"use strict";

/**
 * Validate URL.
 * @param {string} value
 * @return {boolean}
 */
const isValidURL = value => {
  if (
    !(
      typeof value === "string" &&
      value.length > 0 &&
      /^https?:\/\/(.*)$/i.test(value)
    )
  ) {
    return "Please enter a valid URL.";
  }
  return true;
};

/**
 * Validate string value.
 * @param {string} value
 * @return {boolean}
 */
const isValidStringValue = value => {
  if (typeof value !== "string" || value.length === 0) {
    return "Please enter a string value.";
  }
  return true;
};

module.exports = {
  isValidURL,
  isValidStringValue,
};
