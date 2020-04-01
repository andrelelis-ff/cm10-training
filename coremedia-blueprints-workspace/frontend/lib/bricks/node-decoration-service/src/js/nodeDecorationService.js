import $ from "jquery";
import { findAndSelf } from "@coremedia/brick-utils";

/**
 * specifies functionalities to be applied if decorateNode is called
 * @type {Array}
 */
const nodeDecorators = [];

/**
 * specifies functionalities to be removed if undecorateNode is called
 * @type {Array}
 */
const nodeUndecorators = [];

const getSelectorFunction = function(selector, handler) {
  return function($target) {
    findAndSelf($target, selector).each(function() {
      handler($(this));
    });
  };
};

function getDataFunction(baseConfig, identifier, handler) {
  return function($target) {
    const selector = "[data-" + identifier + "]";
    findAndSelf($target, selector).each(function() {
      const $this = $(this);
      const data = $this.data(identifier);
      const config = baseConfig ? $.extend({}, baseConfig, data) : data;
      const state = $.extend({}, $this.data(identifier + "-state"));
      handler($this, config, state);
      if (!$.isEmptyObject(state)) {
        $this.data(identifier + "-state", state);
      }
    });
  };
}

/**
 * Adds a node decorator to list of node decorators
 *
 * @param {nodeDecoratorCallback=} nodeDecorator
 * @param {nodeDecoratorCallback=} nodeUndecorator
 */
export function addNodeDecorator(nodeDecorator, nodeUndecorator) {
  nodeDecorator && nodeDecorators.push(nodeDecorator);
  nodeUndecorator && nodeUndecorators.push(nodeUndecorator);
}
/**
 * @callback nodeDecoratorCallback
 * @param {jQuery} $target
 */

/**
 * Adds a node decorator and already performs selection tasks
 *
 * @param {String} selector
 * @param {nodeDecoratorBySelectorCallback=} decorationHandler
 * @param {nodeDecoratorBySelectorCallback=} undecorationHandler
 */
export function addNodeDecoratorBySelector(
  selector,
  decorationHandler,
  undecorationHandler
) {
  decorationHandler &&
    nodeDecorators.push(getSelectorFunction(selector, decorationHandler));
  undecorationHandler &&
    nodeUndecorators.push(getSelectorFunction(selector, undecorationHandler));
}

/**
 * @callback nodeDecoratorBySelectorCallback
 * @param {jQuery} $target
 */

/**
 * Adds a node decorator and already performs selection, configuration and state tasks.
 *
 * @param {Object} baseConfig
 * @param {String} identifier
 * @param {nodeDecoratorByDataCallback=} decorationHandler
 * @param {nodeDecoratorByDataCallback=} undecorationHandler
 */
export function addNodeDecoratorByData(
  baseConfig,
  identifier,
  decorationHandler,
  undecorationHandler
) {
  decorationHandler &&
    nodeDecorators.push(
      getDataFunction(baseConfig, identifier, decorationHandler)
    );
  undecorationHandler &&
    nodeUndecorators.push(
      getDataFunction(baseConfig, identifier, undecorationHandler)
    );
}

/**
 * @callback nodeDecoratorByDataCallback
 * @param {jQuery} $target
 * @param {Object} config
 * @param {Object} state
 */

/**
 * Applies node decorators to target node
 *
 * @param {object|jQuery} node can be plain DOM-Node or JQuery Wrapped DOM-Node
 */
export function decorateNode(node) {
  let $target;
  if (node instanceof $) {
    $target = node;
  } else {
    $target = $(node);
  }
  nodeDecorators.forEach(function(functionality) {
    functionality($target);
  });
}

/**
 * Applies node undecorators to target node
 *
 * @param {object|jQuery} node can be plain DOM-Node or JQuery Wrapped DOM-Node
 */
export function undecorateNode(node) {
  let $target;
  if (node instanceof $) {
    $target = node;
  } else {
    $target = $(node);
  }
  nodeUndecorators.forEach(function(functionality) {
    functionality($target);
  });
}
