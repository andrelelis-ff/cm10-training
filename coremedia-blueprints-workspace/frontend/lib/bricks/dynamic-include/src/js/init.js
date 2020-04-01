import * as utils from "@coremedia/brick-utils";
import $ from "jquery";
import {
  addNodeDecorator,
  addNodeDecoratorByData,
  undecorateNode,
  decorateNode,
} from "@coremedia/brick-node-decoration-service";

import { EVENT_NODE_APPENDED, renderFragmentHrefs } from "./fragment";
import {
  default as Handler,
  BASE_CONFIG as HANDLER_BASE_CONFIG,
} from "./hashBasedFragment.Handler";
import {
  default as Link,
  BASE_CONFIG as LINK_BASE_CONFIG,
} from "./hashBasedFragment.Link";
import {
  default as Form,
  BASE_CONFIG as FORM_BASE_CONFIG,
} from "./hashBasedFragment.Form";

// --- DOCUMENT READY --------------------------------------------------------------------------------------------------
$(function() {
  const $document = $(document);

  // this will substitute all data-hrefs rendered by ESI
  addNodeDecorator(renderFragmentHrefs);

  // load all dynamic fragments. The special header X-Requested-With is needed by the CAE to identify
  // the request as an Ajax request
  addNodeDecoratorByData(undefined, "cm-fragment", function($fragment, url) {
    utils
      .ajax({
        url: url,
        dataType: "text",
      })
      .done(function(html) {
        const $html = $(html);
        undecorateNode($fragment);
        $fragment.replaceWith($html);
        decorateNode($html);
        $document.trigger(EVENT_NODE_APPENDED, [$html]);
      });
  });
  // handle hashBasedFragmentHandler
  addNodeDecoratorByData(
    HANDLER_BASE_CONFIG,
    "hash-based-fragment-handler",
    // decorate
    function($handler, handlerConfig, state) {
      try {
        state.instance = new Handler($handler, handlerConfig);
      } catch (error) {
        utils.log(error);
      }
    },
    // undecorate
    function($handler, handlerConfig, state) {
      state.instance && state.instance.destroy();
    }
  );

  // handle hashBasedFragmentLinks
  addNodeDecoratorByData(LINK_BASE_CONFIG, "hash-based-fragment-link", function(
    $link,
    linkConfig
  ) {
    new Link($link, linkConfig);
  });

  // handle hashBasedFragmentForms
  addNodeDecoratorByData(FORM_BASE_CONFIG, "hash-based-fragment-form", function(
    $form,
    formConfig
  ) {
    new Form($form, formConfig);
  });
});
