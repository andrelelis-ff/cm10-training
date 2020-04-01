import $ from "jquery";
import { error, log } from "@coremedia/brick-utils";
import { updateTarget } from "@coremedia/brick-dynamic-include";

/**
 * Loads more search results (next page) via ajax below the search results.
 * The given button will be replaced by the search results.
 *
 * @param {string} url
 * @param {*} button
 */
export function loadPage(url, button) {
  const $button = $(button);
  const $spinner = $button.next();

  log("Load more search results via ajax.", $button, url);
  $button.hide();
  $spinner.show();

  // load more results
  log("ajaxUrl", url);
  $.ajax({
    url: url,
  })
    .done(nextSearchResults => {
      log("Loaded next page successfully.");
      // delete the old spinner (the result has a new one)
      $spinner.remove();
      // replace the button with the result (the result has a new one)
      updateTarget($button, $(nextSearchResults), true);
    })
    .fail(() => {
      error("Could not load next page.");
      // restore button and spinner again for retry
      $spinner.hide();
      $button.show();
    });
}

/**
 * Default wrapper function to handle dom elements or jQuery selectors
 *
 * @param domElementOrJQueryResult
 * @param searchResultsContainerId
 */
export default function(domElementOrJQueryResult, searchResultsContainerId) {
  if (domElementOrJQueryResult instanceof $) {
    $.each(domElementOrJQueryResult, function(index, item) {
      loadPage(item, searchResultsContainerId);
    });
  } else {
    loadPage(domElementOrJQueryResult, searchResultsContainerId);
  }
}
