import $ from "jquery";
import { EVENT_LAYOUT_CHANGED } from "@coremedia/brick-utils";

$(function() {
  // update tabs in wcs (e.g. pdp)
  $(".tab_container").on("click", function() {
    $(document).trigger(EVENT_LAYOUT_CHANGED);
  });
});
