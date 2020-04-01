/*** BEM helpers */

function getBEMElementClassName(bemBlock, bemElement) {
  return bemBlock + "__" + bemElement;
}

function getBEMModifierClassName(bemBlock, bemModifier) {
  return bemBlock + "--" + bemModifier;
}

/**
 * Adds a B.E.M. conform element class to the dom-elements.
 *
 * @param $jQueryResult {jQuery} the jQuery result to operate on
 * @param bemBlock {string} the block name
 * @param bemElement {string} the element name
 * @return {jQuery} the found elements as jQuery result
 */
export function findBEMElement($jQueryResult, bemBlock, bemElement) {
  return $jQueryResult.find("." + getBEMElementClassName(bemBlock, bemElement));
}

/**
 * Adds a B.E.M. conform modifier class to the dom-elements.
 *
 * @param $jQueryResult {jQuery} the jQuery result to operate on
 * @param bemBlock {string} the block name
 * @param bemModifier {string} the modifier name
 * @return {jQuery} the given $jQueryResult for chaining
 */
export function addBEMModifier($jQueryResult, bemBlock, bemModifier) {
  return $jQueryResult.addClass(getBEMModifierClassName(bemBlock, bemModifier));
}

/**
 * Removes a B.E.M. modifier class from the dom-elements (if existing).
 *
 * @param $jQueryResult {jQuery} the jQuery result to operate on
 * @param bemBlock {string} the block name
 * @param bemModifier {string} the modifier name
 * @return {jQuery} the given $jQueryResult for chaining
 */
export function removeBEMModifier($jQueryResult, bemBlock, bemModifier) {
  return $jQueryResult.removeClass(
    getBEMModifierClassName(bemBlock, bemModifier)
  );
}
