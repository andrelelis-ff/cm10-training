import $ from "jquery";
import objectFitImages from "object-fit-images";
import objectFitVideos from "object-fit-videos";
import { debounce } from "./debounce";
import * as Logger from "./logger.js";
import { EVENT_LAYOUT_CHANGED } from "./utils";

/**
 * Add pseudo for selection by data attribute managed by jQuery (not equal to search for [data-...])
 */
$.extend($.expr[":"], {
  data: $.expr.createPseudo
    ? $.expr.createPseudo(function(dataName) {
        return function(elem) {
          return !!$.data(elem, dataName);
        };
      })
    : // support: jQuery <1.8
      function(elem, i, match) {
        return !!$.data(elem, match[3]);
      },
});

/**
 * Coremedia Logger module.
 *
 * The Logger is disabled by default. It will be enabled if developer mode is enabled.
 *
 * usage:
 * 1) enable logging:
 * setLevel(LEVEL.ALL);
 *
 * 2) print to log:
 * log("log this");
 * debug("debug this");
 * info("info this");
 * warn("warn this");
 * error("error this");
 *
 * 3) disable logging:
 * setLevel(LEVEL.OFF);
 *
 * @module logger
 */

// check cookie if developerMode is active (cookie available and not empty)
if (
  document.cookie.indexOf("cmUserVariant=") > 0 &&
  document.cookie.indexOf('cmUserVariant=""') === -1
) {
  Logger.setLevel(Logger.LEVEL.ALL);
}

// --- DOCUMENT READY --------------------------------------------------------------------------------------------------
$(function() {
  const $window = $(window);
  const $document = $(document);

  // enable logger, if developer mode element in DOM exist as fallback
  if (
    document.querySelector("[data-cm-developer-mode]") &&
    Logger.getCurrentLevelName() === Logger.LEVEL.OFF.toString()
  ) {
    Logger.setLevel(Logger.LEVEL.ALL);
  }

  // trigger polyfill for object fit on images and videos (will automatically detect newly attached DOM elements)
  objectFitImages();
  objectFitVideos();

  // trigger layout changed event if the size of the window changes using smartresize plugin
  $window.on(
    "resize",
    {},
    debounce(function() {
      Logger.log("Window resized");
      $document.trigger(EVENT_LAYOUT_CHANGED);
    })
  );
});
