import $ from "jquery";
import * as logger from "@coremedia/brick-utils";
import { addNodeDecoratorByData } from "@coremedia/brick-node-decoration-service";
import { loadPage } from "./pagination";

// Enable pagination

addNodeDecoratorByData(undefined, "cm-pagination-page", ($button, url) => {
  logger.log("Initialize loadPaginationClickHandler", $button, url);
  $button.on("click touch", () => {
    loadPage(url, $button);
  });
  // enable button as soon as functionality is attached
  $button.removeAttr("disabled");
});

// --- DOCUMENT READY --------------------------------------------------------------------------------------------------

$(function() {
  "use strict";

  /* --- Mobile Header Search --- */
  const $search = $("#cmSearchWrapper");
  const $searchInput = $search.find(".cm-search__form-input");
  $(".cm-mobile-search-button, .cm-search__form-close").on("click", function() {
    $search.toggleClass("open");
    if ($search.hasClass("open")) {
      $searchInput.focus();
    }
  });

  // prevent empty search on all search fields
  $(".cm-search__form-button").on("click", function(e) {
    let $input = $(this)
      .parents(".cm-search--form")
      .find(".cm-search__form-fieldset input");
    if ($input.length > 0 && $input.val().length === 0) {
      e.preventDefault();
      $input.focus();
    }
  });
});
