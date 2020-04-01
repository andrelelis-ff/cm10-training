import $ from "jquery";
import * as logger from "@coremedia/brick-utils";
import "imagesloaded/imagesloaded.pkgd.js";

/**
 * @typedef ResponsiveMediaFormat
 * @description Provides information about an image format
 *
 * @property {String} name The name of the format
 * @property {Number} ratioWidth The normalized width
 * @property {Number} ratioHeight The normalized height
 * @property {Object} linksForWidth mapping of maxWidth to an image link
 */

/**
 * The type of the event that is triggered if the src is changing.
 *
 * @type {string}
 */
export const EVENT_SRC_CHANGING = "srcChanging";

/**
 * The type of the event that is triggered if the src was changed successfully.
 *
 * @type {string}
 */
export const EVENT_SRC_CHANGED = "srcChanged";

/**
 * Returns the {@link ResponsiveMediaFormat}s for the responsive image element.
 *
 * @param {HTMLElement} image
 * @return {Array<ResponsiveMediaFormat>} an array of formats
 */
function getResponsiveMediaFormats(image) {
  const responsiveMediaData = $(image).data("cm-responsive-media");
  return Array.isArray(responsiveMediaData) ? responsiveMediaData : [];
}

/**
 * Returns the active {@link ResponsiveMediaFormat} for the responsive image element.
 * Make sure the listen to the event type {@link EVENT_SRC_CHANGED} to get notified about changes.
 *
 * @param {HTMLElement} image
 * @return {ResponsiveMediaFormat} the active format
 */
export function getCurrentResponsiveImageFormat(image) {
  const formats = getResponsiveMediaFormats(image);
  const lastRatio = $(image).data("lastRatio");
  return formats.filter(format => format.name === lastRatio)[0];
}

/**
 * Initializes a responsive image.
 *
 * @param {HTMLElement} image
 */
function responsiveImage(image) {
  const $image = $(image);

  function triggerSrcChanged() {
    $image.trigger({
      type: EVENT_SRC_CHANGED,
      src: $image.attr("src"),
      maxWidth: $image.data("lastMaxWidth"),
      ratio: $image.data("lastRatio"),
    });
  }

  const imagesLoadedPluginExists = typeof $.fn.imagesLoaded === "function";

  if ($image.data("cm-responsive-media-state") === undefined) {
    $image.data("cm-responsive-media-state", "initialized");
    // check if imagesLoaded plugin exists
    if (imagesLoadedPluginExists) {
      if ($image.attr("src")) {
        $image.imagesLoaded(triggerSrcChanged);
      }
    } else {
      $image.on("load", triggerSrcChanged);
    }
  }

  const formats = getResponsiveMediaFormats(image);

  if (formats.length === 0) {
    logger.warn("No responsive image data found.", image);
    return;
  }

  const $imageContainer = $image.parent();
  let containerWidth = $imageContainer.width();
  let containerHeight = $imageContainer.height();
  if (!containerWidth || !containerHeight) {
    logger.log(
      "Could not load hidden Responsive Media. The width and height of the surrounding container must be greater than zero.",
      image
    );
    return; // image is not visible, do not touch
  }

  // CMS-2905: use retina images, if enabled
  const deviceRatio = window.devicePixelRatio;
  let retinaImagesEnabled = false;
  if (deviceRatio > 1 && $image.data("cm-retina")) {
    retinaImagesEnabled = true;
    containerHeight *= deviceRatio;
    containerWidth *= deviceRatio;
  }

  const containerRatio = containerWidth / containerHeight;

  /**
   * @type {ResponsiveMediaFormat}
   */
  const bestFittingFormat = formats
    // a format is only valid if height and width are greater than zero
    .filter(format => format.ratioHeight > 0 && format.ratioWidth > 0)
    // calculate and store the difference between the container ratio and the format
    .map(format => ({
      format: format,
      difference: Math.abs(
        containerRatio - format.ratioWidth / format.ratioHeight
      ),
    }))
    // find the best fitting format (=the format with the least difference)
    .reduce(
      (currentBestFormat, nextFormat) =>
        nextFormat.difference < currentBestFormat.difference
          ? nextFormat
          : currentBestFormat,
      // initially we have not found a format an the difference is infinite (so every other format is better)
      {
        format: null,
        difference: Infinity,
      }
    ).format;

  if (!bestFittingFormat) {
    logger.warn("Found no matching aspect ratio.", image);
    return;
  }

  // find best fitting width
  const bestFittingWidth = Object.keys(bestFittingFormat.linksForWidth)
    // a key should represent the (maximum) width
    .map(key => parseInt(key))
    // make sure that a valid number is parsed and the link is not empty
    .filter(width => !isNaN(width) && bestFittingFormat.linksForWidth[width])
    // find the width with 1) the least quality loss that is 2) as small as possible
    .reduce(
      (currentBestWidth, nextWidth) => {
        if (
          // case: currentBestWidth and nextWidth are smaller than the container
          // -> take nextWidth if the image is bigger (lesser quality loss)
          (currentBestWidth < containerWidth &&
            nextWidth < containerWidth &&
            nextWidth > currentBestWidth) ||
          // case: currentBestWidth is smaller and nextWidth is bigger than the container
          // -> take nextWidth image (no quality loss is better than any quality loss)
          (currentBestWidth < containerWidth && nextWidth >= containerWidth) ||
          // case: currentBestWidth and nextWidth are bigger than the container
          // -> take nextWidth if the image is smaller (no quality loss and smaller size)
          (currentBestWidth >= containerWidth &&
            nextWidth >= containerWidth &&
            nextWidth < currentBestWidth)
        ) {
          return nextWidth;
        }
        return currentBestWidth;
      },
      // initially the width is -1 (so every other width is better)
      -1
    );

  if (bestFittingWidth === -1) {
    logger.warn(
      `Found no matching link in aspect ratio '${bestFittingFormat.name}'.`,
      image
    );
    return;
  }

  const bestFittingLink = bestFittingFormat.linksForWidth[bestFittingWidth];

  // @since 1.3
  // image can be an <img> tag
  const retinaSuffix = retinaImagesEnabled
    ? ` (Retina Images enabled with deviceRatio: ${deviceRatio})`
    : "";
  if ($image.is("img")) {
    // replace link if not the same
    if (bestFittingLink !== $image.attr("src")) {
      logger.info(
        `Change Responsive Image to aspect ratio: '${bestFittingFormat.name}' and maxWidth: '${bestFittingWidth}'${retinaSuffix}`,
        image
      );
      $image.trigger({
        type: EVENT_SRC_CHANGING,
        src: $image.attr("src"),
        maxWidth: bestFittingWidth,
        ratio: bestFittingFormat.name,
      });
      $image.data("lastMaxWidth", bestFittingWidth);
      $image.data("lastRatio", bestFittingFormat.name);
      $image.attr("src", bestFittingLink);
      if (imagesLoadedPluginExists) {
        $image.imagesLoaded(triggerSrcChanged);
      }
    }
    // or a background image via style attribute
  } else {
    // replace link if not the same
    if (
      "background-image: url('" + bestFittingLink + "');" !==
      $image.attr("style")
    ) {
      logger.info(
        `Change Responsive Background Image to aspect ratio: '${bestFittingFormat.name}' and maxWidth: '${bestFittingWidth}'${retinaSuffix}`,
        image
      );
      $image.data("lastMaxWidth", bestFittingWidth);
      $image.data("lastRatio", bestFittingFormat.name);
      $image.attr("style", "background-image: url('" + bestFittingLink + "');");
    }
  }
}

export default function(domElementOrJQueryResult) {
  if (domElementOrJQueryResult instanceof $) {
    $.each(domElementOrJQueryResult, function(index, item) {
      responsiveImage(item);
    });
  } else {
    responsiveImage(domElementOrJQueryResult);
  }
}
