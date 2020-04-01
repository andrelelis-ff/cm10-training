import $ from "jquery";
import * as logger from "@coremedia/brick-utils";

import * as hashBasedFragment from "./hashBasedFragment";
import { updateTargetWithAjaxResponse } from "./fragment";

const $window = $(window);

const STATE_DATA_ATTRIBUTE_NAME = "hash-based-fragment-handler-state";

/**
 * baseUrl must always be given
 * if global is set to true a selector must be specified
 * @param handlerConfig
 * @returns {boolean}
 */
function validateHandlerConfig(handlerConfig) {
  return (
    handlerConfig.baseUrl &&
    (!handlerConfig.fragmentContainer ||
      handlerConfig.fragmentContainer.global === false ||
      (handlerConfig.fragmentContainer.global === true &&
        handlerConfig.fragmentContainer.selector))
  );
}

export const BASE_CONFIG = {
  baseUrl: undefined,
  validParameters: [],
  modifiedParametersHeaderPrefix: undefined,
  fragmentContainer: {
    selector: undefined,
    global: false,
  },
};

export default class {
  constructor(element, config) {
    this._element = element;
    this._config = config;
    this._lastRequestParams = undefined;
    this._windowListener = undefined;
    this._disabled = false;

    this._init();
  }

  disable() {
    this._disabled = true;
  }

  enable() {
    this._disabled = false;
  }

  _getFragmentContainer() {
    if (!this._config.fragmentContainer.selector) {
      return this._element;
    }
    if (this._config.fragmentContainer.global) {
      return $.find(this._config.fragmentContainer.selector);
    } else {
      return this._handler.find(this._config.fragmentContainer.selector);
    }
  }

  _requestParamsChanged(newRequestParams) {
    if (
      (!this._lastRequestParams && newRequestParams) ||
      (this._lastRequestParams && !newRequestParams)
    ) {
      return true;
    }
    let name;
    for (name in this._lastRequestParams) {
      if (!this._lastRequestParams.hasOwnProperty(name)) {
        continue;
      }
      if (this._lastRequestParams[name] !== newRequestParams[name]) {
        return true;
      }
    }
    for (name in newRequestParams) {
      if (!newRequestParams.hasOwnProperty(name)) {
        continue;
      }
      if (this._lastRequestParams[name] !== newRequestParams[name]) {
        return true;
      }
    }
    return false;
  }

  _changeRef(requestParams) {
    if (this._disabled) {
      return;
    }
    const $fragmentContainer = this._getFragmentContainer();
    // disable underlying hashBasedFragmentHandlers so no unnecessary requests are triggered
    const $subHandlers = $fragmentContainer.find(
      ":data(" + STATE_DATA_ATTRIBUTE_NAME + ")"
    );
    $subHandlers.each(function() {
      $(this)
        .data(STATE_DATA_ATTRIBUTE_NAME)
        .instance.disable();
    });
    const requestConfig = {
      url: this._config.baseUrl,
      params: requestParams,
    };
    const that = this;
    updateTargetWithAjaxResponse(
      $fragmentContainer,
      requestConfig,
      false,
      function(jqXHR) {
        if (jqXHR.status === 200) {
          // only handle modified parameters if header prefix is given
          if (that._config.modifiedParametersHeaderPrefix) {
            let requestChanged = false;
            $.each(that._config.validParameters, function(_, validParameter) {
              const modifierParameter = jqXHR.getResponseHeader(
                that._config.modifiedParametersHeaderPrefix + validParameter
              );
              if (modifierParameter) {
                requestChanged = true;
                requestParams[validParameter] = modifierParameter;
              }
            });

            if (requestChanged) {
              // adjust state so no reload is triggered
              that._lastRequestParams = requestParams;
              const newHash =
                "#" + hashBasedFragment.requestParamsToString(requestParams);
              if (history.replaceState) {
                history.replaceState({}, "", newHash);
              }
            }
          }
        } else {
          $subHandlers.each(function() {
            $(this)
              .data(STATE_DATA_ATTRIBUTE_NAME)
              .instance.enable();
          });
        }
      }
    );
  }

  _handleHashChange(newHash) {
    const requestParams = hashBasedFragment.stringToRequestParams(
      newHash.replace(/^#/, "") || "",
      this._config.validParameters
    );
    if (this._requestParamsChanged(requestParams)) {
      this._lastRequestParams = requestParams;
      this._changeRef(requestParams);
    }
  }

  _init() {
    // validate configuration
    if (!validateHandlerConfig(this._config)) {
      throw "Invalid handler configuration";
    }

    const hash = window.location.hash;
    this._handleHashChange(hash);

    const that = this;
    this._windowListener = function() {
      that._handleHashChange(window.location.hash);
    };
    $window.on("hashchange", this._windowListener);
  }

  destroy() {
    // validate configuration
    if (!validateHandlerConfig(this._config)) {
      logger.log("Invalid configuration:", this._config);
      return;
    }

    if (this._windowListener) {
      $window.off("hashchange", this._windowListener);
      this._windowListener = undefined;
    }
  }
}
