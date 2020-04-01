import $ from "jquery";
import * as logger from "@coremedia/brick-utils";
import {
  decorateNode,
  undecorateNode,
} from "@coremedia/brick-node-decoration-service";

/**
 * Returns all element nodes of the given nodeList as array.
 *
 * @param {NodeList} nodeList
 * @returns {Node[]} returns all element nodes of the given nodeList as array
 */
function getElementNodes(nodeList) {
  return Array.prototype.slice
    .call(nodeList)
    .filter(node => node.nodeType === Node.ELEMENT_NODE);
}

$(function() {
  "use strict";

  if (document.querySelector("[data-cm-developer-mode]")) {
    logger.setLevel(logger.LEVEL.ALL);
  }
  logger.log("Welcome to CoreMedia Salesforce Commerce Cloud Integration");

  // Reinitialize the PDP-Asset-integration whenever the product-image-container is refreshed
  const productImageContainer = $("#pdpMain > .product-image-container")[0];
  if (productImageContainer) {
    const mutationObserver = new MutationObserver(mutations => {
      mutations.forEach(mutation => {
        if (mutation.type === "childList") {
          getElementNodes(mutation.removedNodes).forEach(removedNode => {
            undecorateNode(removedNode);
          });
          getElementNodes(mutation.addedNodes).forEach(addedNode => {
            decorateNode(addedNode);
          });
        }
      });
    });
    mutationObserver.observe(productImageContainer, { childList: true });
  }
});
