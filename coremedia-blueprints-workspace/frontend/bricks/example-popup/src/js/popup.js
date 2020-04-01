import $ from "jquery";
import { log } from "@coremedia/brick-utils";
import magnificPopup from "@coremedia/brick-magnific-popup";

export default function($popupElements) {
  $popupElements.each((index, popupElement) => {
    const $popupElement = $(popupElement);

    log("Popup found.", $popupElement);
    magnificPopup($popupElement, {
      type: "inline",
      callbacks: {
        open: function() {
          // trigger resize to set the correct image aspect ratio to source
          $popupElement.trigger("resize");
        },
      },
    });
    $popupElement.removeClass("cm-button--popup-loading");
  });
}
