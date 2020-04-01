import $ from "jquery";
import * as logger from "@coremedia/brick-utils";

function threeSixtySpinner(spinner) {
  const $document = $(document);
  const $container = $(spinner);
  const images = $container.find(".cm-spinner__image");

  // Number of  total frames used in this spinner
  const totalFrames = images.length;

  // Defaults parameters:
  // Tells if the app is ready for user interaction
  let ready = false;
  // Tells the app if the user is dragging the pointer
  let dragging = false;
  // Tells the app if the user is pressing a key
  let moved = false;
  // Stores the pointer starting X position for the pointer tracking
  let pointerStartPosX = 0;
  // Stores the pointer ending X position for the pointer tracking
  let pointerEndPosX = 0;
  // Stores the distance between the starting and ending pointer X position in each time period we are tracking the pointer
  let pointerDistance = 0;
  // The starting time of the pointer tracking period
  let monitorStartTime = 0;
  // The pointer tracking time duration
  const monitorInt = 40;
  // A setInterval instance used to call the rendering function
  let ticker = 0;
  // Sets the speed of the image sliding animation
  const speedMultiplier = 0.5;
  // Stores the total amount of images we have in the sequence
  // The current frame value of the image slider animation
  let currentFrame = 0;
  // Stores all the loaded image objects
  const frames = [];
  // The value of the end frame which the currentFrame will be tweened to during the sliding animation
  let endFrame = 0;

  // only run if there is at least two image in this spinner
  if (totalFrames > 1) {
    logger.log("Initialize 360° Spinner with " + totalFrames + " frames");

    // Inititialize frames (array of image objects)
    images.each(function() {
      frames.push($(this));
    });

    // Show first frame
    frames[0].addClass("current-image").fadeIn();

    //Start
    render();

    /* --- Events ----------------------------------------------------------------------------------------------- */

    /**
     * Adds the jQuery "mousedown" and "touchstart" event to the image slider wrapper.
     */
    $container.on("mousedown touchstart", function(event) {
      // leftclick
      if (event.type === "mousedown" && event.which === 1) {
        ready = true;
        event.preventDefault();
        event.stopPropagation(); //w3c
        event.cancelBubble = true; //ie
        // touch
      } else if (event.type === "touchstart") {
        ready = true;
        // stop swipe on touch devices
        event.stopPropagation();
      }
      // start
      if (ready) {
        // Stores the pointer x position as the starting position
        pointerStartPosX = getPointerEvent(event).pageX;
        logger.log("360° Spinner: start dragging by " + event.type);
        // Remove Icon
        hideIcon();
      }
    });

    /**
     * Add the jQuery "mousemove" and "touchmove" event handler, if started dragging inside the $container.
     */
    $document.on("mousemove touchmove", function(event) {
      if (ready) {
        dragging = true;

        event.preventDefault();
        event.stopPropagation();
        // Starts tracking the pointer X position changes
        trackPointer(event);
      }
    });

    /**
     * Adds the jQuery "mouseup" event to the document for stopping, if started dragging inside the $container.
     */
    $document.on("mouseup touchend", function(event) {
      ready = false;
      if (dragging) {
        dragging = false;
        event.preventDefault();
        event.stopImmediatePropagation();
        logger.log("360° Spinner: stop dragging");
      }
    });

    $container.on("click touch", function(event) {
      // avoid that surrounding links will be activated if 360 spinner was used
      event.preventDefault();
      event.stopPropagation(); //w3c
      event.cancelBubble = true; //ie
    });

    /**
     * Adds the jQuery "keydown" event to the document. You can move the spinner by pressing left or right on the keyboard.
     */
    $document.on("keydown", function(event) {
      // only if spinner is visible
      if ($container.css("visibility") !== "hidden") {
        const key = event.keyCode || event.which;
        switch (key) {
          // left key, go one frame to the left
          case 37:
            endFrame--;
            moved = true;
            break;
          // right key, go one frame to the right
          case 39:
            endFrame++;
            moved = true;
            break;
        }
        if (moved) {
          logger.log("360° Spinner: Moved with keyboard");
          hideIcon();
          render();
          moved = false;
        }
      }
    });
  } else {
    logger.log(
      "Error: Found 360° Spinner without frames, can't initialize it."
    );
  }

  /* --- internal functions ------------------------------------------------------------------------------------- */

  /**
   * Renders the image slider frame animations.
   */
  function render() {
    // The rendering function only runs if the "currentFrame" value hasn't reached the "endFrame" one
    if (currentFrame !== endFrame) {
      // Calculates the 10% of the distance between the "currentFrame" and the "endFrame".
      // By adding only 10% we get a nice smooth and eased animation.
      // If the distance is a positive number, we have to ceil the value, if its a negative number, we have to floor it to make sure
      // that the "currentFrame" value surely reaches the "endFrame" value and the rendering doesn't end up in an infinite loop.
      const frameEasing =
        endFrame < currentFrame
          ? Math.floor((endFrame - currentFrame) * 0.1)
          : Math.ceil((endFrame - currentFrame) * 0.1);
      // Sets the current image to be hidden
      hidePreviousFrame();
      // Increments / decrements the "currentFrame" value by the 10% of the frame distance
      currentFrame += frameEasing;
      // Sets the current image to be visible
      showCurrentFrame();
    } else {
      // If the rendering can stop, we stop and clear the ticker
      window.clearInterval(ticker);
      ticker = 0;
    }
  }

  /**
   * Creates a new setInterval and stores it in the "ticker"
   * By default I set the FPS value to 60 which gives a nice and smooth rendering in newer browsers
   * and relatively fast machines, but obviously it could be too high for an older architecture.
   */
  function refresh() {
    // If the ticker is not running already...
    if (ticker === 0) {
      // Let's create a new one!
      ticker = self.setInterval(render, Math.round(1000 / 60));
    }
  }

  /**
   * Hides the previous frame
   * It calls the "getNormalizedCurrentFrame" method to translate the "currentFrame" value to the "totalFrames" range
   */
  function hidePreviousFrame() {
    frames[getNormalizedCurrentFrame()].removeClass("current-image");
  }

  /**
   * Displays the current frame
   * It calls the "getNormalizedCurrentFrame" method to translate the "currentFrame" value to the "totalFrames" range
   */
  function showCurrentFrame() {
    frames[getNormalizedCurrentFrame()].addClass("current-image");
  }

  /**
   * Returns the "currentFrame" value translated to a value inside the range of 0 and "totalFrames"
   */
  function getNormalizedCurrentFrame() {
    let c = Math.ceil(currentFrame % totalFrames);
    if (c < 0) {
      c += totalFrames - 1;
    }
    return c;
  }

  /**
   * Returns a simple event regarding the original event is a mouse event or a touch event.
   */
  function getPointerEvent(event) {
    return event.originalEvent.targetTouches
      ? event.originalEvent.targetTouches[0]
      : event;
  }

  /**
   * Tracks the pointer X position changes and calculates the "endFrame" for the image slider frame animation.
   * This function only runs if the application is ready and the user really is dragging the pointer; this way we
   * can avoid unnecessary calculations and CPU usage.
   */
  function trackPointer(event) {
    const userDragging = ready && dragging;
    if (userDragging) {
      // Stores the last x position of the pointer
      pointerEndPosX = getPointerEvent(event).pageX;
      // Checks if there is enough time past between this and the last time period of tracking
      if (monitorStartTime < new Date().getTime() - monitorInt) {
        // Calculates the distance between the pointer starting and ending position during the last tracking time period
        pointerDistance = pointerEndPosX - pointerStartPosX;
        // Calculates the endFrame using the distance between the pointer X starting and ending positions and the "speedMultiplier" values
        if (pointerDistance > 0) {
          endFrame =
            currentFrame +
            Math.ceil(
              (totalFrames - 1) *
                speedMultiplier *
                (pointerDistance / $document.width())
            );
        } else {
          endFrame =
            currentFrame +
            Math.floor(
              (totalFrames - 1) *
                speedMultiplier *
                (pointerDistance / $document.width())
            );
        }
        // Updates the image slider frame animation
        refresh();
        // restarts counting the pointer tracking period
        monitorStartTime = new Date().getTime();
        // Stores the the pointer X position as the starting position (because we started a new tracking period)
        pointerStartPosX = getPointerEvent(event).pageX;
      }
    }
  }

  /**
   * Fade out overlay icon, if displayed
   */
  function hideIcon() {
    const $icon = $container.find(".cm-spinner__icon");
    if ($icon.length) {
      $icon.fadeOut();
    }
  }
}

export default function(domElementOrJQueryResult) {
  if (domElementOrJQueryResult instanceof $) {
    $.each(domElementOrJQueryResult, function(index, item) {
      threeSixtySpinner(item);
    });
  } else {
    threeSixtySpinner(domElementOrJQueryResult);
  }
}
