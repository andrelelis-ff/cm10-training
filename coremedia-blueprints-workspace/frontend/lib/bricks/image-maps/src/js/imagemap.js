import $ from "jquery";
import { EVENT_SRC_CHANGED, EVENT_SRC_CHANGING } from "@coremedia/brick-media";

/**
 * Transforms a comma-separated String of coords into an array of points.
 * Points are objects with 2 properties: x and y representing the point.
 *
 * @param {string} coords
 * @return {Array}
 * @ignore
 */
function coordsToRect(coords) {
  // coords = left,top,right,bottom
  // browsers also support flipped rects (so right < left and bottom < top are valid)
  const coordsAsInts = coords.split(",").map(function(i) {
    return Math.floor(parseInt(i));
  });

  let result = [];
  if (coordsAsInts.length === 4) {
    result = [
      { x: coordsAsInts[0], y: coordsAsInts[1] },
      { x: coordsAsInts[2], y: coordsAsInts[1] },
      { x: coordsAsInts[2], y: coordsAsInts[3] },
      { x: coordsAsInts[0], y: coordsAsInts[3] },
    ];
  }
  return result;
}

/**
 * Transforms an array of points into a comma-separated String.
 * Points are objects with 2 properties: x and y representing the point.
 *
 * @param {Array} points
 * @returns {string}
 * @ignore
 */
function rectToCoords(points) {
  // rect is a polygon with 4 edges, so first and third edge will define the rect
  let result = "";
  if (points.length === 4) {
    result = [points[0].x, points[0].y, points[2].x, points[2].y].join(",");
  }
  return result;
}

/**
 * Placeholder - not implemented yet
 *
 * @returns {Array}
 * @ignore
 */
function coordsToCircle() {
  return [];
}

/**
 * Placeholder - not implemented yet
 *
 * @returns {string}
 * @ignore
 */
function circleToCoords() {
  return "";
}

/**
 * Placeholder - not implemented yet
 *
 * @returns {Array}
 * @ignore
 */
function coordsToPoly() {
  return [];
}

/**
 * Transforms an array of points into a comma-separated String.
 * Points are objects with 2 properties: x and y representing the point.
 *
 * @param {Array} points
 * @returns {string}
 * @ignore
 */
function polyToCoords(points) {
  let result = [];
  for (let j = 0; j < points.length; j++) {
    result.push(points[j].x);
    result.push(points[j].y);
  }
  return result.join(",");
}

/**
 * Transforms any object passed into an array representing the default coords.
 * Introduced for consistency. The given param is ignored, function will always return an empty array.
 *
 * @returns {Array}
 * @ignore
 */
function coordsToDefault() {
  // shape default has no coords
  return [];
}

/**
 * Transforms any object into a String representing the default coords.
 * Introduced for consistency. The given param is ignored, function will always return an empty String.
 *
 * @returns {string}
 * @ignore
 */
function defaultToCoords() {
  // shape default has no coords
  return "";
}

/**
 * @typedef {Object} converterMap
 * @prop {function} rect
 * @prop {function} circle
 * @prop {function} poly
 * @prop {function} rectangle
 * @prop {function} circ
 * @prop {function} polygon
 * @prop {function} default
 */

/**
 * Maps possible values for the attribute shape of the HTML map element to converter functions (both directions).
 *
 * @member {Object} coordsConverter
 * @prop {converterMap} coordsTo
 * @prop {converterMap} toCoords
 */
export const coordsConverter = {
  coordsTo: {
    // W3C
    rect: coordsToRect,
    circle: coordsToCircle,
    poly: coordsToPoly,
    // supported in many browsers
    rectangle: coordsToRect,
    circ: coordsToCircle,
    polygon: coordsToPoly,
    // default is ignored (no transformation needed and no hotzone indicator)
    default: coordsToDefault,
  },
  toCoords: {
    // W3C
    rect: rectToCoords,
    circle: circleToCoords,
    poly: polyToCoords,
    // supported in many browsers
    rectangle: rectToCoords,
    circ: circleToCoords,
    polygon: polyToCoords,
    default: defaultToCoords,
  },
};

/**
 * Calculates a bounding box for given points.
 * Points are objects with 2 properties: x and y representing the point.
 *
 * @function calculateBoundingBox
 * @param {Array} coordsAsPoints
 * @return {Object}
 */
export function calculateBoundingBox(coordsAsPoints) {
  let result = {
    x1: undefined,
    y1: undefined,
    x2: undefined,
    y2: undefined,
  };
  for (let i = 0; i < coordsAsPoints.length; i++) {
    const point = coordsAsPoints[i];
    result = {
      x1: Math.min(result.x1 !== undefined ? result.x1 : point.x, point.x),
      x2: Math.max(result.x2 !== undefined ? result.x2 : point.x, point.x),
      y1: Math.min(result.y1 !== undefined ? result.y1 : point.y, point.y),
      y2: Math.max(result.y2 !== undefined ? result.y2 : point.y, point.y),
    };
  }
  return result;
}

/**
 * Recalculates all areas of the imagemap for the actual dimensions the imagemap has.
 *
 * @function update
 * @param {jQuery} $imagemap - The imagemap element to update.
 * @param {String} [newRatio] - If there was an aspect ratio switch, this is the new ratio to be used.
 */
export function update($imagemap, newRatio) {
  // get configuration of imagemap
  const config = $.extend(
    { coordsBaseWidth: 1 },
    $imagemap.data("cm-imagemap")
  );
  const $areas = $imagemap.find(".cm-imagemap__areas");
  const $image = $imagemap.find(".cm-imagemap__picture");
  const $wrapper = $imagemap.find(".cm-imagemap__wrapper");
  newRatio = newRatio || undefined;

  const width = $image.width();
  const height = $image.height();
  // width for relative positioning (wrapper is assumed to be the last dom element with position != static)
  const rWidth = $wrapper.width();
  const rHeight = $wrapper.height();

  // calculate the needed transformation base
  const fraction = width / config.coordsBaseWidth;

  // iterate over all areas having shape and data-coords set
  $areas.find("area[data-coords][shape]").each(function() {
    const $area = $(this);
    let coords = $area.data("current-coords");
    const shape = $area.attr("shape");
    if (newRatio !== undefined) {
      coords = $area.data("coords")[newRatio];
    }
    $area.data("current-coords", coords);
    if (!coords) {
      // there are no coordinates to recompute
      return;
    }

    // transform the coordinates given as String into an array of Point
    let coordsAsPoints = [];
    if (typeof coordsConverter.coordsTo[shape] === "function") {
      coordsAsPoints = coordsConverter.coordsTo[shape](coords);
    }

    // There have to be at least 3 points, otherwise no shape can be drawn
    if (coordsAsPoints.length >= 3) {
      let i;
      // transform and normalize coordinates
      // smooth normalization needed taking left and right coordinate into account (for polygons)
      for (i = 0; i < coordsAsPoints.length; i++) {
        coordsAsPoints[i].x = Math.min(
          Math.max(coordsAsPoints[i].x * fraction, 0),
          width
        );
        coordsAsPoints[i].y = Math.min(
          Math.max(coordsAsPoints[i].y * fraction, 0),
          height
        );
      }

      const hotzoneBox = calculateBoundingBox(coordsAsPoints);

      // check visibility of hotzone:
      // surface area of bounding box must be greater than zero
      let visible =
        Math.abs(hotzoneBox.x1 - hotzoneBox.x2) *
          Math.abs(hotzoneBox.y1 - hotzoneBox.y2) >
        0;

      // hotzone indicator must fit into image
      const hotzoneCenter = {
        x: (hotzoneBox.x1 + hotzoneBox.x2) / 2,
        y: (hotzoneBox.y1 + hotzoneBox.y2) / 2,
      };
      const $hotzoneIndicator = $area.next(".cm-imagemap__hotzone");
      const hotzoneIndicatorWidth = Math.abs($hotzoneIndicator.width());
      const hotzoneIndicatorHeight = Math.abs($hotzoneIndicator.height());
      const hotzoneIndicatorBox = {
        x1: hotzoneCenter.x - hotzoneIndicatorWidth / 2,
        x2: hotzoneCenter.x + hotzoneIndicatorWidth / 2,
        y1: hotzoneCenter.y - hotzoneIndicatorHeight / 2,
        y2: hotzoneCenter.y + hotzoneIndicatorHeight / 2,
      };

      // short formular, assuming x1 <= x2, y1 <= y2
      /* jshint ignore:start */
      visible =
        visible &&
        hotzoneIndicatorBox.x1 >= 0 &&
        hotzoneIndicatorBox.x2 < width &&
        hotzoneIndicatorBox.y1 >= 0 &&
        hotzoneIndicatorBox.y2 < height;
      /* jshint ignore:end */

      if (visible) {
        // set new hot zone coordinates
        const strCoords = coordsConverter.toCoords[shape](coordsAsPoints);
        if (strCoords !== "") {
          $area.attr("coords", strCoords);
        } else {
          $area.removeAttr("coords");
        }
        $area.removeClass("cm-imagemap__area--disabled");

        $hotzoneIndicator.each(function() {
          const $hotzoneIndicator = $(this);
          // the area's marker div must be repositioned if ratio has changed
          if (
            newRatio !== undefined ||
            $hotzoneIndicator.data("cm-hotzone-indicator-disabled")
          ) {
            $hotzoneIndicator.data("cm-hotzone-indicator-disabled", false);
            $hotzoneIndicator.removeClass(
              "cm-imagemap__hotzone--loading cm-imagemap__hotzone--disabled"
            );
            $hotzoneIndicator.css({
              top: (hotzoneCenter.y * 100) / $wrapper.height() + "%",
              left: (hotzoneCenter.x * 100) / $wrapper.width() + "%",
              //"display": "",
              transform: "",
            });
          }
        });
      } else {
        // move everything out of viewport of wrapper
        $area.attr("coords", [rWidth, rHeight, rWidth, rHeight].join(","));
        $area.addClass("cm-imagemap__area--disabled");
        $hotzoneIndicator.data("cm-hotzone-indicator-disabled", true);
        $hotzoneIndicator.removeClass("cm-imagemap__hotzone--loading");
        $hotzoneIndicator.addClass("cm-imagemap__hotzone--disabled");
      }
    }
  });
}

/**
 * Initializes an imagemap element.
 *
 * @function init
 * @param {jQuery} $imagemap - The imagemap element to initialize.
 */
export function init($imagemap) {
  // get configuration of imagemap
  const $image = $imagemap.find(".cm-imagemap__picture");

  // Handle responsive and non-responsive images
  if ($image.data("cm-responsive-media-state") !== undefined) {
    $image.on(EVENT_SRC_CHANGING, function() {
      // hide hotzones if src is changing
      $imagemap.find(".cm-imagemap__hotzone").css("display", "none");
    });
    $image.on(EVENT_SRC_CHANGED, function(event) {
      // display hotzones if src has changed (and is fully loaded)
      $imagemap.find(".cm-imagemap__hotzone").css("display", "");
      update($imagemap, event.ratio);
    });
  } else {
    // set "uncropped"
    update($imagemap, "uncropped");
  }

  function mouseenter() {
    const $this = $(this);
    const $button = $this.is(".cm-imagemap__hotzone")
      ? $this
      : $this.next(".cm-imagemap__hotzone");
    $button.addClass("cm-imagemap__hotzone--hover");
  }
  function mouseleave() {
    const $this = $(this);
    const $button = $this.is(".cm-imagemap__hotzone")
      ? $this
      : $this.next(".cm-imagemap__hotzone");
    $button.removeClass("cm-imagemap__hotzone--hover");
  }

  // delegate click to button
  $imagemap.find(".cm-imagemap__area").click(function() {
    $(this)
      .next(".cm-imagemap__hotzone")
      .trigger("click");
    return false;
  });

  $imagemap
    .find(".cm-imagemap__area, " + ".cm--imagemap__hotzone")
    .hover(mouseenter, mouseleave);
}
