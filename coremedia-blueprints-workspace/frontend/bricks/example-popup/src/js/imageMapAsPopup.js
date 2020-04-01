import $ from "jquery";
import { api as magnificPopupApi } from "@coremedia/brick-magnific-popup";
import { log } from "@coremedia/brick-utils";

export default function($imageMaps) {
  $imageMaps.each((index, imageMap) => {
    const $popupElement = $(imageMap);

    log("ImageMap with popups found.", $popupElement);

    // items of imagemap (popup targets)
    let items = [];
    $popupElement.find("[data-cm-imagemap-target]").each((index, popupItem) => {
      if (!$(popupItem).hasClass("cm-imagemap__hotzone--disabled")) {
        items.push({
          src: $(popupItem).data("cm-imagemap-target"),
        });
      }
    });

    //clickable buttons (hotzone icons and areas)
    $popupElement
      .find("[data-cm-imagemap-target-id]")
      .on("click", function(event) {
        //prevent the default href link
        event.preventDefault();

        //open the item of the gallery
        let itemId = $(this).data("cm-imagemap-target-id");
        magnificPopupApi.open(
          {
            items: items,
            type: "inline",
            gallery: {
              enabled: true,
            },
            callbacks: {
              change: () => {
                // trigger resize to set the correct image aspect ratio to source
                $popupElement.trigger("resize");
              },
            },
          },
          itemId
        );
      });
    $popupElement
      .find(".cm-button--popup-loading")
      .removeClass("cm-button--popup-loading");
  });
}
