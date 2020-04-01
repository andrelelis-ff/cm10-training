import $ from "jquery";
import * as nodeDecorationService from "@coremedia/brick-node-decoration-service";
import responsiveImages from "./responsiveImages";
import { EVENT_LAYOUT_CHANGED } from "@coremedia/brick-utils";

const RESPONSIVE_IMAGES_SELECTOR = "[data-cm-responsive-media]";

$(function() {
  // initializes responsive images
  nodeDecorationService.addNodeDecoratorBySelector(
    RESPONSIVE_IMAGES_SELECTOR,
    function($target) {
      responsiveImages($target);
    }
  );

  // adds removes spinner if an image has finished loading
  nodeDecorationService.addNodeDecoratorBySelector(
    ".cm-media--loading",
    function($target) {
      const callback = function() {
        $target.removeClass("cm-media--loading");
      };
      if (typeof $.fn.imagesLoaded === typeof callback) {
        $target.imagesLoaded(callback);
      } else {
        $target.on("load", callback);
      }
    }
  );

  $(document).on(EVENT_LAYOUT_CHANGED, function() {
    responsiveImages($(RESPONSIVE_IMAGES_SELECTOR));
  });
});
