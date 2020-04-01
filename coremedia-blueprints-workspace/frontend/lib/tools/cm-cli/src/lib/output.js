const {
  workspace: { COREMEDIA_SCOPE, EXAMPLE_SCOPE },
} = require("@coremedia/tool-utils");

/**
 * @typedef Choice
 * @property {String} name
 * @property {String} short
 * @property {*} value
 */

/**
 * Creates a choice from a brick package name for inquirer.
 *
 * @param {String} packageName the package name of the brick
 * @param {*} value (optional) a value if it differs from the name
 * @returns {Choice}
 */
function getBrickChoice(packageName, value = packageName) {
  const displayName = packageName
    .replace(`${COREMEDIA_SCOPE}/brick-`, "")
    .replace(`${EXAMPLE_SCOPE}/brick-`, "(Example) ");
  return {
    name: displayName,
    short: displayName,
    value: value,
  };
}

/**
 * Creates a choice from a theme package name for inquirer.
 *
 * @param packageName the package name of the theme
 * @param {*} value (optional) a value if it differs from the name
 * @returns {Choice}
 */
function getThemeChoice(packageName, value = packageName) {
  const displayName = packageName
    .replace(`${COREMEDIA_SCOPE}/`, "")
    .replace(`${EXAMPLE_SCOPE}/`, "(Example) ");
  return {
    name: displayName,
    short: displayName,
    value: value,
  };
}

/**
 * Sorts choices provided to inquirer.
 *
 * @param {Array<Choice>} choices
 * @returns {Array<Choice>}
 */
function sortChoices(choices) {
  return choices.sort(({ name = "" }, { name: otherName = "" }) =>
    name.localeCompare(otherName)
  );
}

module.exports = {
  getBrickChoice,
  getThemeChoice,
  sortChoices,
};
