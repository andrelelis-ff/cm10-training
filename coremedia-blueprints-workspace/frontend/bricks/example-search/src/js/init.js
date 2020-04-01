import $ from "jquery";
import { log } from "@coremedia/brick-utils";
import {
  addNodeDecoratorByData,
  addNodeDecoratorBySelector,
} from "@coremedia/brick-node-decoration-service";
import { loadSearchResultPage, loadSearchResults } from "./search";

addNodeDecoratorByData(undefined, "cm-search-results", ($button, url) => {
  log("Initialize loadSearchResultsClickHandler", $button, url);
  $button.on("click touch", () => {
    loadSearchResults(url, $button);
  });
  // enable button as soon as functionality is attached
  $button.removeAttr("disabled");
});

/**
 * Add click handler to all links inside the search page to reload the search via ajax
 */
const searchResultPageContainerId = "cm-search-result-page";
function loadSearchResultPageClickHandler($target) {
  log("Initialize loadSearchResultPageClickHandler", $target);
  /* on links like filters */
  $target.on("click touch", "[data-cm-search-link]", function(event) {
    // avoid 2nd click on label for input field
    event.preventDefault();
    let $this = $(this);
    let link = $this.data("cm-search-link");
    // update search query input field, if suggestion is available
    if ($this.data("cm-search-suggestion")) {
      $("[name=query]").val($this.data("cm-search-suggestion"));
    }
    // browser history is enabled by default
    loadSearchResultPage(
      link,
      searchResultPageContainerId,
      !$("body").data("cm-search-disable-browser-history")
    );
  });
  /* on dropdown */
  $target.on("change", "[data-cm-search-dropdown]", function() {
    let link = $(this)
      .find("option:selected")
      .data("cm-search-sort-link");
    loadSearchResultPage(
      link,
      searchResultPageContainerId,
      !$("body").data("cm-search-disable-browser-history")
    );
  });
  /* on search submit */
  $target.on("submit", "[data-cm-search-form-submit]", function(event) {
    event.preventDefault();
    let query = $("[data-cm-search-form-input]").val();
    // update search query input field, if suggestion is available
    $("[name=query]").val(query);
    // replace query string in search link with new qquery
    let link = $(this).data("cm-search-form-submit");
    let re = new RegExp("([?&])query=.*?(&|$)", "i");
    if (link.match(re)) {
      link = link.replace(re, "$1" + "query=" + query + "$2");
    }
    loadSearchResultPage(
      link,
      searchResultPageContainerId,
      !$("body").data("cm-search-disable-browser-history")
    );
  });
}

function cleanup() {
  $("body").removeClass("cm-body--filter-popup-active");
}

addNodeDecoratorBySelector(
  "#" + searchResultPageContainerId,
  loadSearchResultPageClickHandler,
  cleanup
);

/**
 * Show or hide filter popup on mobile
 */
function toggleFilterMobilePopupClickHandler($target) {
  log("Initialize toggleFilterMobilePopupClickHandler");
  $target.on("click touch", function() {
    $("[data-cm-search-filter-popup]").toggleClass(
      "cm-search__filter-popup--active"
    );
    $("body").toggleClass("cm-body--filter-popup-active");
  });
}
addNodeDecoratorBySelector(
  "[data-cm-search-filter-popup-toggle]",
  toggleFilterMobilePopupClickHandler
);

/**
 * Show or hide filter list items
 */
function toggleSearchFilterClickHandler($target) {
  log("Initialize toggleSearchFilterClickHandler", $target);
  $target.on("click touch", "[data-cm-search-filter-toggle]", function() {
    $(this).toggleClass("cm-search__filter-title--list-collapsed");
    $(this)
      .next("[data-cm-search-filter-links]")
      .toggle();
  });
}
addNodeDecoratorBySelector(
  "[data-cm-search-filter]",
  toggleSearchFilterClickHandler
);

/* --- browser history --- */
if (!$("body").data("cm-search-disable-browser-history")) {
  /**
   * Add event handler for back button to redo the last search from the browser history.
   */
  window.addEventListener(
    "popstate",
    function() {
      if (history.state && history.state.id === "search") {
        loadSearchResultPage(
          history.state.link,
          searchResultPageContainerId,
          false
        );
      }
    },
    false
  );

  /**
   *  Push default history state for browser back button.
   */
  if($('.cm-search--results')[0]) {
    window.history.replaceState(
      { id: "search", link: window.location + "&view=asSearchResultPage" },
      "",
      window.location
    );
  }
}
