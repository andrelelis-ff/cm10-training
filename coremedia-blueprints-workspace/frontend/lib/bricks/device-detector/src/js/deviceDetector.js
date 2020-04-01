import $ from "jquery";
import { debounce } from "@coremedia/brick-utils";

const $window = $(window);
const $document = $(document);

// EVENTS
const EVENT_PREFIX = "coremedia.blueprint.deviceDetector.";
export const EVENT_DEVICE_CHANGED = EVENT_PREFIX + "deviceChanged";

/**
 * store the last device whose specific settings were applied
 * @type {{type: String|undefined, orientation: String|undefined, isTouch: boolean|undefined}}
 */
const lastDevice = {
  type: undefined,
  orientation: undefined,
  isTouch: undefined,
};

/**
 * reads the current device type from body:after content defined by css media queries
 * @returns {string} "mobile"|"tablet"|"desktop"
 */
export function detectDeviceType() {
  return window
    .getComputedStyle(document.body, ":after")
    .getPropertyValue("content")
    .replace(/['"]/g, "");
}

/**
 * reads the current device orientation from body:before content defined by css media queries
 * @returns {string} "portrait"|"landscape"
 */
export function detectDeviceOrientation() {
  return window
    .getComputedStyle(document.body, ":before")
    .getPropertyValue("content")
    .replace(/['"]/g, "");
}

/**
 * checks if the current device is a touch device which means that swiping is possible but hovering is not.
 * @returns {boolean} true if touch device otherwise false
 */
export function isTouchDevice() {
  return "ontouchstart" in window || navigator.msMaxTouchPoints > 0;
}

/**
 * returns the current device
 * @returns {{type: String|undefined, orientation: String|undefined, isTouch: boolean|undefined}}
 */
export function getLastDevice() {
  return {
    type: lastDevice.type,
    orientation: lastDevice.orientation,
    isTouch: lastDevice.isTouch,
  };
}

/**
 * Updates the device detection. If device has changed device specific settings are applied.
 */
function update() {
  const newDevice = {
    type: detectDeviceType(),
    orientation: detectDeviceOrientation(),
    isTouch: isTouchDevice(),
  };
  if (
    lastDevice.type === undefined ||
    lastDevice.orientation === undefined ||
    lastDevice.isTouch === undefined ||
    lastDevice.type !== newDevice.type ||
    lastDevice.orientation !== newDevice.orientation ||
    lastDevice.isTouch !== newDevice.isTouch
  ) {
    $document.trigger(EVENT_DEVICE_CHANGED, [newDevice, lastDevice]);

    lastDevice.type = newDevice.type;
    lastDevice.orientation = newDevice.orientation;
    lastDevice.isTouch = newDevice.isTouch;
  }
}

/**
 * inits the device detector
 */
export function init() {
  $window.on(
    "resize",
    {},
    debounce(function() {
      update();
    })
  );
  // delay initial update after all other document ready functions have been called
  setTimeout(function() {
    update();
  }, 1);
}
