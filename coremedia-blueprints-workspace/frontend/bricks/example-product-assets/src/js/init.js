import $ from "jquery";
import {
  addNodeDecoratorByData,
  addNodeDecoratorBySelector,
} from "@coremedia/brick-node-decoration-service";
import { refreshFragment } from "@coremedia/brick-dynamic-include";
import { default as magnificPopup } from "@coremedia/brick-magnific-popup";
import { EVENT_LAYOUT_CHANGED, debounce } from "@coremedia/brick-utils";
import { getLastDevice } from "@coremedia/brick-device-detector";
import {
  EVENT_SRC_CHANGED,
  getCurrentResponsiveImageFormat,
} from "@coremedia/brick-media";
import { PRODUCT_ASSET_CAROUSEL_READY_EVENT } from "./index";
import Zoom from "./zoom";

// IBM specific code
// TODO: move to themes
function getChangeImagesFn($productAssets, shoppingActionsJS) {
  return function(catEntryId, productId) {
    //reload the fragment with selected product variants.
    //to this end we send the catEntryId, productId and the selected attributes name/value pairs
    //as ';'-separated string as "attributes" to the reloader
    const entitledItemId = "entitledItem_" + productId;
    const selectedAttributes =
      shoppingActionsJS.selectedAttributesList[entitledItemId];
    let attributes = "";
    for (let attribute in selectedAttributes) {
      if (selectedAttributes.hasOwnProperty(attribute)) {
        attributes += attribute + ";" + selectedAttributes[attribute] + ";";
      }
    }
    refreshFragment($productAssets, undefined, {
      productId: productId,
      catEntryId: catEntryId,
      attributes: attributes,
    });
  };
}

const productAssetsStateIbmId = "cm-product-assets-state-ibm";
addNodeDecoratorBySelector(".cm-product-assets", function($target) {
  const wcTopic = window.wcTopic;
  const productAssetsStateIbm = {
    onUnload: () => {}
  };
  if (typeof wcTopic !== "undefined") {
    // WCS 9
    const shoppingActionsJS = window.shoppingActionsJS;
    if (shoppingActionsJS) {
      const changeImages = getChangeImagesFn($target, shoppingActionsJS);
      const events = ["DefiningAttributes_Resolved", "DefiningAttributes_Changed"];
      wcTopic.subscribe(events, changeImages);
      productAssetsStateIbm.onUnload = () => {
        // use private API here as there is no wcTopic.unsubscribe...
        events.forEach(id => wcTopic._topics[id].unsubscribe(changeImages));
      };
    }
  } else {
    const dojo = window.dojo;
    if (typeof dojo !== "undefined") {
      // WCS 8
      dojo.addOnLoad(function () {
        const productDisplayJS = window.productDisplayJS;
        if (productDisplayJS) {
          const changeImages = getChangeImagesFn($target, productDisplayJS);
          const events = ["DefiningAttributes_Resolved", "DefiningAttributes_Changed"];
          const tokens = events.map(id => dojo.topic.subscribe(id, changeImages));
          productAssetsStateIbm.onUnload = () => {
            tokens.forEach(token => token.remove());
          };
        }
      });
    }
  }
  $target.data(productAssetsStateIbmId, productAssetsStateIbm);
}, function($target) {
  const { onUnload } = $target.data(productAssetsStateIbmId) || {};
  onUnload && onUnload();
  $target.removeData(productAssetsStateIbmId);
});

// synchronize carousel should control slideshow
// sadly we cannot use the build-in feature of slick-carousel because it has some serious issues...
// we can also not rely on slick carousel's own active class because it doesn't work as expected
const $document = $(document);

addNodeDecoratorByData(
  {
    zoom: {},
  },
  "cm-product-assets",
  function($target, { zoom }, instance) {
    const $slideshow = $target.find(".cm-product-assets__slideshow");
    const $carousel = $target.find(".cm-product-assets__carousel");

    $slideshow.on("afterChange", function(event, slick, currentSlide) {
      $carousel.slick("slickGoTo", currentSlide);
      $carousel
        .find(".slick-slide.slick-slide--active")
        .removeClass("slick-slide--active");
      $carousel
        .find(`.slick-slide[data-slick-index="${currentSlide}"]`)
        .addClass("slick-slide--active");
    });

    $carousel.on("click", ".slick-slide", function() {
      const goToSingleSlide = $(this).data("slick-index");

      $slideshow.slick("slickGoTo", goToSingleSlide);
    });

    $carousel.ready(function() {
      $carousel
        .find(".slick-slide.slick-current")
        .addClass("slick-slide--active");
    });

    // do not use zoom plugin on touch devices
    if (
      $slideshow.length > 0 &&
      (!getLastDevice().isTouch || getLastDevice().type === "desktop")
    ) {
      instance.zoom = new Zoom($slideshow[0], zoom);

      instance.updateZoomImage = () => {
        const $responsiveImage = $slideshow
          .find(".slick-current")
          .find(".cm-product-asset__media[data-cm-responsive-media]");
        // the img is not set if the slideshow shows a spinner or a video
        if ($responsiveImage && $responsiveImage.length > 0) {
          const img = $responsiveImage[0];
          const responsiveImageFormat = getCurrentResponsiveImageFormat(img);
          if (responsiveImageFormat) {
            const biggestWidth = Math.max(
              ...Object.keys(responsiveImageFormat.linksForWidth).map(key =>
                parseInt(key)
              )
            );
            instance.zoom.imageLink =
              responsiveImageFormat.linksForWidth[biggestWidth];
            return;
          }
        }
        instance.zoom.imageLink = null;
      };

      // trigger re-init after the image of the slideshow has changed
      $slideshow.on("afterChange", instance.updateZoomImage);
    }

    magnificPopup($slideshow, {
      gallery: { enabled: true },
      delegate: ".cm-product-asset[data-cm-product-asset-gallery-item]",
      callbacks: {
        elementParse: item => {
          // try to find a responsive images
          const responsiveImageFormat = getCurrentResponsiveImageFormat(
            item.el.find(".cm-product-asset__media[data-cm-responsive-media]")
          );
          if (responsiveImageFormat) {
            // if found: use item type: image
            const biggestWidth = Math.max(
              ...Object.keys(responsiveImageFormat.linksForWidth).map(key =>
                parseInt(key)
              )
            );
            item.type = "image";
            item.src = responsiveImageFormat.linksForWidth[biggestWidth];
          } else {
            // otherwise: just item type: inline and create a clone of the DOM
            item.type = "inline";
            item.src = item.el.clone();
          }
        },
        open: () => {
          $document.trigger(EVENT_LAYOUT_CHANGED);
        },
      },
    });

    const slideshowDeferrer = $.Deferred(); //indicates if the product asset's slideshow is fully initialized

    $.when(slideshowDeferrer).done(function() {
      $document.trigger(EVENT_LAYOUT_CHANGED);
      instance.updateZoomImage && instance.updateZoomImage();
      $slideshow.trigger(PRODUCT_ASSET_CAROUSEL_READY_EVENT);
    });

    function finishProductAssetsInitialization() {
      const $activeImg = $slideshow.find(".slick-current").find("img");
      if ($activeImg) {
        if ($activeImg.height() > 0) {
          // assuming that the height of the element is zero if the image has not been loaded yet
          slideshowDeferrer.resolve();
        } else {
          // if the image is not loaded yet then we need to wait for the "srcChanged" event
          $activeImg.one(EVENT_SRC_CHANGED, function() {
            slideshowDeferrer.resolve();
          });
        }
      }
    }

    // signal "fully initialized" for the slideshow
    if ($slideshow.is(".slick-initialized")) {
      finishProductAssetsInitialization();
    }
    $slideshow.on("init", finishProductAssetsInitialization);
  },
  function($target, {}, instance) {
    instance.zoom && instance.zoom.destroy();
    if (instance.updateZoomImage) {
      $target.off("afterChange", instance.updateZoomImage);
      instance.updateZoomImage = null;
    }
  }
);
