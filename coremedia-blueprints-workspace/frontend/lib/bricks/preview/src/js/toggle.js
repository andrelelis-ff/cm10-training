import $ from "jquery";
import { EVENT_LAYOUT_CHANGED } from "@coremedia/brick-utils";

/**
 * Toggle (open/close) an element with class "toggle-container" by clicking on an element
 * with class "toggle-button" inside a container. The state will be stored in sessionStorage,
 * if available in browser and unique data-id is set.
 *
 * Example:
 *
 * <div id="example" data-id="example">
 *   <a href="#" class="toggle-button">Headline</a>
 *   <div class="toggle-container">Content</div>
 * </div>
 */

const $document = $(document);

/* Defines that the toggle is on */
export const STATE_ON = "on";
/* Defines that the toggle is off */
export const STATE_OFF = "off";

/**
 * Returns the state of a toggleItem base on the visibility of the
 * toggleContainer element.
 *
 * @param {object} toggleItem
 * @returns {string} "on" or "off"
 */
export function getState(toggleItem) {
  // if toggle-container is visible state is on otherwise off
  return $(toggleItem)
    .find(".toggle-container:first")
    .hasClass("toggle-container-off")
    ? STATE_OFF
    : STATE_ON;
}

/**
 *  Sets the toggle on
 *
 * @param {object} toggleItem
 */
export function on(toggleItem) {
  const $toggleItem = $(toggleItem);
  $toggleItem.find(".toggle-button:first").removeClass("toggle-off");
  $toggleItem
    .find(".toggle-container:first")
    .removeClass("toggle-container-off");
  $toggleItem.trigger("toggleStateChanged", [STATE_ON]);
}

/**
 * Sets the toggle off
 *
 * @param {object} toggleItem
 */
export function off(toggleItem) {
  const $toggleItem = $(toggleItem);
  $toggleItem.find(".toggle-button:first").addClass("toggle-off");
  $toggleItem.find(".toggle-container:first").addClass("toggle-container-off");
  $toggleItem.trigger("toggleStateChanged", [STATE_OFF]);
}

/**
 * If the toggle is on set the toggle off otherwise on.
 *
 * @param {object} toggleItem
 */
export function toggle(toggleItem) {
  if (getState(toggleItem) === STATE_ON) {
    off(toggleItem);
  } else {
    on(toggleItem);
  }
}

/**
 * Initializes the toggleItem, binds handlers and sets its state base on the session.
 *
 * @param {object} toggleItem
 */
export function init(toggleItem) {
  // check if browser supported sessionStorage
  const storageEnabled = typeof Storage !== "undefined";
  const $toggleItem = $(toggleItem);
  // only safe state if toggleItem has an id and storage is supported
  const useStorage = storageEnabled && $toggleItem.data("id") !== undefined;

  if (useStorage) {
    const state = sessionStorage.getItem($toggleItem.data("id"));
    if (state === STATE_ON) {
      on(toggleItem);
    }
    if (state === STATE_OFF) {
      off(toggleItem);
    }
  }

  // bind click-listener
  $toggleItem
    .find(".toggle-button")
    .bind("click", function() {
      toggle(toggleItem);
      return false;
    })
    .removeClass("toggle-button--disabled");
  // bind toggleState-listener
  $toggleItem.bind("toggleStateChanged", function(event, newState) {
    if (useStorage) {
      sessionStorage.setItem($toggleItem.data("id"), newState);
    }
    $document.trigger(EVENT_LAYOUT_CHANGED);
  });
}
