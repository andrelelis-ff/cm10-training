import $ from "jquery";
import { ajax } from "@coremedia/brick-utils";
import { addNodeDecoratorByData } from "@coremedia/brick-node-decoration-service";
import { EVENT_CART_UPDATED } from "./index";

const $document = $(document);

/**
 * Assigns a cart update functionality to a button
 *
 * @param $button the button
 * @param link the link to post the request on activation
 * @param data the data to post to the given link
 */
function decorateCartButton($button, link, data) {
  const $icon = $button.find(".cm-button__icon");

  //button clicked
  $button.on("click", function(e) {
    // don't let the add-to-cart button trigger the teaser link
    e.preventDefault();

    //disable button and show spinner
    $button.attr("disabled", "true");
    $button.addClass("cm-button--loading");
    $icon.removeClass("cm-cart__icon-ok cm-cart__icon-warning");

    // send ajax call
    ajax({
      type: "POST",
      url: link,
      data: data,
      dataType: "text",
    })
      .done(function() {
        //show success icon
        $icon.addClass("cm-cart__icon-ok");
        window.setTimeout(function() {
          $icon.fadeOut(400, function() {
            $icon.removeClass("cm-cart__icon-ok").removeAttr("style");
          });
        }, 1500);
      })
      .fail(function() {
        $icon.addClass("cm-cart__icon-warning");
      })
      .always(function() {
        $button.removeAttr("disabled");
        $button.removeClass("cm-button--loading");
        //refresh cart
        $document.trigger(EVENT_CART_UPDATED);
      });
  });
}

// initialize add to cart buttons
addNodeDecoratorByData(
  {
    id: undefined,
    link: undefined,
    token: undefined,
  },
  "cm-cart-add-item",
  ($button, { id, link, token }) => {
    decorateCartButton($button, link, {
      action: "addOrderItem",
      externalTechId: id,
      _CSRFToken: token,
    });
  }
);

// initialize remove from cart buttons
addNodeDecoratorByData(
  {
    id: undefined,
    link: undefined,
    token: undefined,
  },
  "cm-cart-remove-item",
  ($button, { id, link, token }) => {
    decorateCartButton($button, link, {
      action: "removeOrderItem",
      orderItemId: id,
      _CSRFToken: token,
    });
  }
);
