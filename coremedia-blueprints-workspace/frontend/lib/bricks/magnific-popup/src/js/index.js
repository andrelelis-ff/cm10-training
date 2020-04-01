import $ from "jquery";

// Load magnific-popup using shims
//
// Requirements:
// - window must be available and pointing to the actual window instance of the browser
// - window.jQuery must provide module "jquery"
//
// Problems:
// Assigning window.jQuery with the imports-loader will initialize the window with an empty object and window=>window
// does not work as it will assign window with an empty object.
//
// Solution:
// We will manipulate the window object and revert the changes afterwards.
if (window) {
  const hadjQuery = window.hasOwnProperty("jQuery");
  const oldjQuery = window.jQuery;
  window.jQuery = $;
  require("magnific-popup");
  window.jQuery = oldjQuery;
  if (!hadjQuery) {
    delete window.jQuery;
  }
} else {
  throw new Error(
    "Magnific Popup was not loaded because it relies on a 'window' object that does not exist."
  );
}

/**
 * Initialized a new magnific-popup on the given $self.
 *
 * @param $self {jQuery} the target of the popup
 * @param options {object|string} the options to provide
 * @returns {jQuery}
 *
 * @see http://dimsemenov.com/plugins/magnific-popup/documentation.html#initializing-popup
 */
export default function($self, options) {
  if ($self) {
    return $self.magnificPopup(options);
  }
  return $self;
}

/**
 * Provides the magnific-popup API that is attached to jQuery ($.magnificPopup)
 *
 * @see http://dimsemenov.com/plugins/magnific-popup/documentation.html#api
 */
export const api = $.magnificPopup;
