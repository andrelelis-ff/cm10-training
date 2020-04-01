import $ from "jquery";
import * as utils from "@coremedia/brick-utils";
import * as nodeDecorationService from "@coremedia/brick-node-decoration-service";
import * as imagemap from "./imagemap";

const $document = $(document);

$(function() {
  // initializes imagemaps
  nodeDecorationService.addNodeDecorator(function($target) {
    utils.findAndSelf($target, ".cm-imagemap").each(function() {
      const $imagemap = $(this);
      imagemap.init($imagemap);

      $document.on(utils.EVENT_LAYOUT_CHANGED, function() {
        imagemap.update($imagemap);
      });
    });
  });
});
