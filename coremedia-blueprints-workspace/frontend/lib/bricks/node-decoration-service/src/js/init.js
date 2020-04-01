import $ from "jquery";
import { decorateNode } from "./nodeDecorationService";

// --- DOCUMENT READY --------------------------------------------------------------------------------------------------
$(function() {
  // append to dom ready (will be executed after all dom ready functions have finished)
  $(function() {
    decorateNode(document);
  });
});
