import * as hashBasedFragment from "./hashBasedFragment";
import $ from "jquery";

export const BASE_CONFIG = {};

export default class {
  constructor($form) {
    $form.on("submit", function(e) {
      e.preventDefault();
      const requestParams = {};
      const fields = $form.serializeArray();
      $.each(fields, function(i, field) {
        requestParams[field.name] = field.value;
      });
      window.location.hash =
        "#" + hashBasedFragment.requestParamsToString(requestParams);
    });
  }
}
