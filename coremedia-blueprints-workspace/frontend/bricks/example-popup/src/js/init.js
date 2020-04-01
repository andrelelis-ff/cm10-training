import $ from "jquery";
import { addNodeDecoratorByData } from "@coremedia/brick-node-decoration-service";
import popup from "./popup";
import videoAsPopup from "./videoAsPopup";
import imageMapAsPopup from "./imageMapAsPopup";

// --- JQUERY DOCUMENT READY -------------------------------------------------------------------------------------------
$(function() {
  // disable any click events for the popup buttons since it should only trigger the popup to open
  $("body").on("click", ".cm-button--popup-loading", function(e) {
    return false;
  });

  // add node decorator by magnific-popup data attribute for default popups
  // see https://dimsemenov.com/plugins/magnific-popup/documentation.html#inline-type 3)
  addNodeDecoratorByData({}, "mfp-src", popup);

  // add node decorator for imagemaps (as gallery)
  addNodeDecoratorByData({}, "cm-imagemap-popup", imageMapAsPopup);

  // add node decorator for videos
  addNodeDecoratorByData({ url: undefined }, "cm-video-popup", videoAsPopup);
});
