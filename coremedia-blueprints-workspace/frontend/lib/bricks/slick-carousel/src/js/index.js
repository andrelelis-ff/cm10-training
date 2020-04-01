import $ from "jquery";
import { log } from "@coremedia/brick-utils";
import "slick-carousel-no-font-no-png/slick/slick.js";

const PORTRAIT = "portrait";
const LANDSCAPE = "landscape";

function getOrientation() {
  return window.matchMedia("(orientation: portrait)").matches
    ? PORTRAIT
    : LANDSCAPE;
}

/**
 * Generates a responsive carousel for the given carousel and a given config.
 * If the config is not set, defaults will be used.
 *
 * @param {Element|jQuery} container
 * @param {Object} config slick configuration (orientation enhanced)
 */
export default function(container, config) {
  const $carousel = $(container);
  log("Initialize slickCarousel", $carousel);

  const hasOrientationSpecificOptions =
    config.responsive &&
    config.responsive.some(
      config => [PORTRAIT, LANDSCAPE].indexOf(config.orientation) !== -1
    );

  if (hasOrientationSpecificOptions) {
    const responsiveConfigByOrientation = {
      [PORTRAIT]: config.responsive.filter(
        config => !config.orientation || config.orientation === PORTRAIT
      ),
      [LANDSCAPE]: config.responsive.filter(
        config => !config.orientation || config.orientation === LANDSCAPE
      ),
    };

    let currentOrientation = getOrientation();
    // "orientationchange" event is not sufficient as it is not triggered by
    // all devices (e.g. not on desktop)
    $(window).on("resize", () => {
      let newOrientation = getOrientation();
      if (currentOrientation !== newOrientation) {
        $carousel.slick(
          "slickSetOption",
          // don't use "responsive" parameter here as it will only add breakpoints
          {
            responsive: [].concat(
              responsiveConfigByOrientation[newOrientation]
            ),
          },
          true
        );
        currentOrientation = newOrientation;
      }
    });

    config.responsive = responsiveConfigByOrientation[currentOrientation];
  }

  // add transitioning css class during slide changes (currently only used for an IE11 fix)
  // events should be attached before initilization
  $carousel.on("beforeChange", () => {
    $carousel.addClass("cm-slick-carousel--transitioning");
  });

  $carousel.on("afterChange", () => {
    $carousel.removeClass("cm-slick-carousel--transitioning");
  });

  $carousel.slick(config);
}
