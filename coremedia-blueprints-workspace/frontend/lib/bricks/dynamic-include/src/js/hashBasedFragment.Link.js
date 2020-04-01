import * as hashBasedFragment from "./hashBasedFragment";

export const BASE_CONFIG = {
  requestParams: [],
};

export default class {
  constructor($link, linkConfig) {
    $link.attr(
      "href",
      "#" + hashBasedFragment.requestParamsToString(linkConfig.requestParams)
    );
  }
}
