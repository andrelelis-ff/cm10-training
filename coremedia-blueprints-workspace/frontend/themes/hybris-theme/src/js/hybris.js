import $ from "jquery";
import * as utils from "@coremedia/brick-utils";
import {
  EVENT_DEVICE_CHANGED,
  detectDeviceType,
} from "@coremedia/brick-device-detector";

// --- DOCUMENT READY --------------------------------------------------------------------------------------------------
$(function() {
  const $document = $(document);

  utils.log("Welcome to CoreMedia Hybris Integration");

  // to load initially hidden images in tabs
  $(".tabs-list a").on("click", function() {
    $document.trigger(utils.EVENT_LAYOUT_CHANGED);
  });

  $document.on(EVENT_DEVICE_CHANGED, function() {
    if (detectDeviceType() === "desktop") {
      $(".cm-header__navigation")
        .removeClass("collapse")
        .removeAttr("style");
    }
  });
});
