import $ from "jquery";
import { getLastDevice } from "@coremedia/brick-device-detector";
import { refreshFragment } from "@coremedia/brick-dynamic-include";
import { addNodeDecoratorBySelector } from "@coremedia/brick-node-decoration-service";
import { EVENT_CART_UPDATED } from "@coremedia-examples/brick-cart/src/js";

const $document = $(document);
const MOBILE_DEVICE_TYPE = "mobile";

addNodeDecoratorBySelector("[data-cm-cart-control]", $target => {
  const $button = $target.find(".cm-cart-icon");
  const $cartPopup = $target.find(".cm-cart-popup");
  $button.on("click", event => {
    if (getLastDevice().type !== MOBILE_DEVICE_TYPE) {
      event.preventDefault();
      $cartPopup.toggleClass("cm-cart-popup--active");
    }
  });
});

const cartControlIbmId = "cm-cart-control-state-ibm";
addNodeDecoratorBySelector("[data-cm-cart-control]", function($target) {
  const wcTopic = window.wcTopic;
  const cartControlStateIbm = {
    onUnload: () => {}
  };
  const cartListener = () => $document.trigger(EVENT_CART_UPDATED);
  if (typeof wcTopic !== "undefined") {
    // WCS 9
    const events = ["AddOrderItem", "AjaxAddOrderItem", "AjaxDeleteOrderItem", "AjaxUpdateOrderItem"];
    wcTopic.subscribe(events, cartListener);
    cartControlStateIbm.onUnload = () => {
      // use private API here as there is no wcTopic.unsubscribe...
      events.forEach(id => wcTopic._topics[id].unsubscribe(cartListener));
    };
  }
  // WCS 8 has a different implementation in Footer.jsp in cmStorefrontAssetStore

  $target.data(cartControlIbmId, cartControlStateIbm);
}, function($target) {
  const { onUnload } = $target.data(cartControlIbmId) || {};
  onUnload && onUnload();
  $target.removeData(cartControlIbmId);
});

$document.on(EVENT_CART_UPDATED, () => {
  $("[data-cm-cart-control][data-cm-refreshable-fragment]").each(function() {
    refreshFragment($(this));
  });
});
