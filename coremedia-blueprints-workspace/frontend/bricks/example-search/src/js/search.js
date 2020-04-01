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
export function loadSearchResults(url, button) {
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
      log("Loaded search results successfully.");
      // delete the old spinner (the result has a new one)
      $spinner.remove();
      // replace the button with the result (the result has a new one)
      updateTarget($button, $(nextSearchResults), true);
    })
    .fail(() => {
      error("Could not load more search results.");
      // restore button and spinner again for retry
      $spinner.hide();
      $button.show();
    });
}

/**
 * Loads search results via ajax with the given URL.
 *
 * @param {string} link
 * @param {string} searchResultPageId
 * @param {boolean} enableBrowserHistory default is true
 */
export function loadSearchResultPage(
  link,
  searchResultPageId = "cm-search-results-page",
  enableBrowserHistory = true
) {
  if (link) {
    let $searchResultPageId = $("#" + searchResultPageId);
    log("Load search result page via ajax.", link, $searchResultPageId);
    let $searchResultsContainer = $("#cm-search-results");
    $searchResultsContainer.addClass("cm-search__results--loading");
    // load results page
    $.ajax({
      url: link,
    })
      .done(function(nextSearchResults) {
        log("Loaded search result page successfully.");
        // append the new results to the the search result page
        updateTarget($searchResultPageId, $(nextSearchResults), true);
        //set new page url to browser history
        addToBrowserHistory(link, enableBrowserHistory);
      })
      .fail(function() {
        $searchResultsContainer.removeClass("cm-search__results--loading");
        error("Could not load search result page.");
      });
  }
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
      loadSearchResults(item, searchResultsContainerId);
    });
  } else {
    loadSearchResults(domElementOrJQueryResult, searchResultsContainerId);
  }
}

/**
 * Helper function to remove parameter from URL
 *
 * @param {string} url
 * @param {string} parameter
 * @returns {string} url without parameter
 */
function removeURLParameter(url, parameter) {
  let urlParts = url.split("?");
  if (urlParts.length >= 2) {
    let prefix = encodeURIComponent(parameter) + "=";
    let parameters = urlParts[1].split(/[&]/g);
    for (let i = parameters.length; i-- > 0; ) {
      if (parameters[i].lastIndexOf(prefix, 0) !== -1) {
        parameters.splice(i, 1);
      }
    }
    url = urlParts[0] + "?" + parameters.join("&");
  }
  return url;
}

/**
 * Helper function to add a state to the browser history, if enabled
 *
 * @param {string} link the CAE URL of the search
 * @param {boolean} enableBrowserHistory
 */
function addToBrowserHistory(link, enableBrowserHistory) {
  if (enableBrowserHistory) {
    log("Add search to browser history");
    window.history.pushState(
      { id: "search", link: link },
      "",
      removeURLParameter(link, "view")
    );
  }
}
