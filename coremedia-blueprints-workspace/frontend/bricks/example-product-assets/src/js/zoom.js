class Point {
  /**
   * @param {Number} left
   * @param {Number} top
   */
  constructor(left, top) {
    this.left = left;
    this.top = top;
  }
}

class Rect {
  /**
   * @param {Number} left
   * @param {Number} top
   * @param {Number} width
   * @param {Number} height
   */
  constructor(left, top, width, height) {
    this.left = left;
    this.top = top;
    this.width = width;
    this.height = height;
    Object.freeze(this);
  }

  /**
   * @returns {Number}
   */
  get right() {
    return this.left + this.width;
  }

  /**
   * @returns {Number}
   */
  get bottom() {
    return this.top + this.height;
  }

  /**
   * @returns {Point}
   */
  get center() {
    return new Point(this.left + this.width / 2, this.top + this.height / 2);
  }

  /**
   *
   * @param {Point} centerPoint
   * @param {Number} width
   * @param {Number} height
   * @returns {Rect}
   */
  static fromCenter(centerPoint, width, height) {
    const halfWidth = width / 2;
    const halfHeight = height / 2;
    return new Rect(
      centerPoint.left - halfWidth,
      centerPoint.top - halfHeight,
      width,
      height
    );
  }
}

/**
 * @param {HTMLElement} element
 * @returns {Rect}
 */
function getRect(element) {
  const boundingRect = element.getBoundingClientRect();
  return new Rect(
    boundingRect.left +
      (document.documentElement.scrollLeft || document.body.scrollLeft),
    boundingRect.top +
      (document.documentElement.scrollTop || document.body.scrollTop),
    boundingRect.width,
    boundingRect.height
  );
}

// positioning the zoom window requires incorporating the border size
const offsetX = 5;

class Zoom {
  /**
   * @param {HTMLElement} target
   * @param {Object} config
   */
  constructor(target, { containerSelector, imageLink }) {
    this.target = target;
    this.containerSelector = containerSelector || "body";
    this.zoomBase = 1;

    // create zoom elements
    this.zoomLens = document.createElement("div");
    this.zoomLens.classList.add("cm-zoom-lens");
    document.body.appendChild(this.zoomLens);

    this.zoomWindow = document.createElement("div");
    this.zoomWindow.classList.add("cm-zoom-window");
    document.body.appendChild(this.zoomWindow);

    this.imagePreload = document.createElement("img");

    // events
    this._boundOnWheel = event => this._onWheel(event);
    this.target.addEventListener("wheel", this._boundOnWheel, {
      passive: false,
    });
    this._boundMouseEnter = event => this._onMouseEnter(event);
    this.target.addEventListener("mouseenter", this._boundMouseEnter);
    this._boundMouseMove = event => this._onMouseMove(event);
    this.target.addEventListener("mousemove", this._boundMouseMove);
    this._boundMouseLeave = event => this._onMouseLeave(event);
    this.target.addEventListener("mouseleave", this._boundMouseLeave);

    this._boundLoad = event => this._onLoad(event);
    this.imagePreload.addEventListener("load", this._boundLoad);

    this.imageLink = imageLink;
  }

  _onWheel(event) {
    event.preventDefault();
    const factor = event.deltaY > 0 ? 0.1 : -0.1;
    this.zoomBase = Math.max(0.1, Math.min(1, this.zoomBase + factor));
    this._updatePosition(new Point(event.pageX, event.pageY));
  }

  _onMouseEnter(event) {
    this._updatePosition(new Point(event.pageX, event.pageY));
  }

  _onMouseMove(event) {
    this._updatePosition(new Point(event.pageX, event.pageY));
  }

  _onMouseLeave() {
    this._updatePosition(null);
  }

  _onLoad() {
    this.zoomWindow.classList.remove("cm-zoom-window--loading");
    this.zoomWindow.style.backgroundImage = `url(${encodeURI(
      this.imagePreload.src
    )})`;
  }

  _activate() {
    this.zoomLens.classList.add("cm-zoom-lens--active");
    this.zoomWindow.classList.add("cm-zoom-window--active");
  }

  _deactivate() {
    this.zoomWindow.classList.remove("cm-zoom-window--active");
    this.zoomLens.classList.remove("cm-zoom-lens--active");
  }

  /**
   *
   * @param {Point} cursorPosition the absolute position of the cursor, if null the zoom lens and window will be hidden
   * @private
   */
  _updatePosition(cursorPosition) {
    if (this.imageLink && cursorPosition) {
      const image = this.target;
      const imageRect = getRect(image);
      const container = image.closest(this.containerSelector) || document.body;
      const containerRect = getRect(container);

      // calculate dimensions of the zoom window

      // - positioned right to the image, taken the offsetX into account
      // - aligned horizontally to the image
      // - the width is limited by the surrounding container
      // - the height limited by the container height, the viewport and the actual image dimensions
      let zoomWindowWidth =
        containerRect.width + containerRect.left - imageRect.right - offsetX;
      const zoomWindowRect = new Rect(
        imageRect.right + offsetX,
        imageRect.top,
        zoomWindowWidth,
        Math.min(
          window.pageYOffset + (window.innerHeight - offsetX) - imageRect.top,
          containerRect.height,
          (imageRect.height * zoomWindowWidth) / imageRect.width
        )
      );

      // basically the lens should be centered around the cursor
      let lensRect = Rect.fromCenter(
        cursorPosition,
        imageRect.width * this.zoomBase,
        Math.min(
          imageRect.height,
          Math.ceil(
            ((imageRect.width * this.zoomBase) / zoomWindowRect.width) *
              zoomWindowRect.height
          )
        )
      );

      // make sure that lens is not positioned outside of the image (left/top)
      lensRect = new Rect(
        Math.max(lensRect.left, imageRect.left),
        Math.max(lensRect.top, imageRect.top),
        lensRect.width,
        lensRect.height
      );

      // make sure that lens is not positioned outside of the image (right/bottom)
      lensRect = new Rect(
        Math.min(lensRect.right, imageRect.right) - lensRect.width,
        Math.min(lensRect.bottom, imageRect.bottom) - lensRect.height,
        lensRect.width,
        lensRect.height
      );

      // apply calculations to elements

      // position lens element
      Object.assign(this.zoomLens.style, {
        left: `${lensRect.left}px`,
        top: `${lensRect.top}px`,
        width: `${lensRect.width}px`,
        height: `${lensRect.height}px`,
      });

      // calculate the required value of the background-position style
      // we will use a percentage value which is defined by the available container dimensions without taking the
      // display dimensions into account (which also means that if the difference is zero we just use 50%)
      const backgroundPositionDimensions = {
        width: imageRect.width - lensRect.width,
        height: imageRect.height - lensRect.height,
      };
      const percentageX =
        backgroundPositionDimensions.width === 0
          ? "50%"
          : ((lensRect.center.left - imageRect.left - lensRect.width / 2) /
              backgroundPositionDimensions.width) *
              100 +
            "%";
      const percentageY =
        backgroundPositionDimensions.height === 0
          ? "50%"
          : ((lensRect.center.top - imageRect.top - lensRect.height / 2) /
              backgroundPositionDimensions.height) *
              100 +
            "%";

      // position zoom window
      Object.assign(this.zoomWindow.style, {
        left: `${zoomWindowRect.left}px`,
        top: `${zoomWindowRect.top}px`,
        width: `${zoomWindowRect.width}px`,
        height: `${zoomWindowRect.height}px`,
        backgroundPosition: `${percentageX} ${percentageY}`,
        backgroundSize: (imageRect.width / lensRect.width) * 100 + "%",
      });
      this._activate();
    } else {
      // in case the cursor position is not passed we will hide the lens and the zoom window
      this._deactivate();
    }
  }

  set imageLink(newImageLink) {
    if (newImageLink !== this._imageLink) {
      this.zoomWindow.style.backgroundImage = null;
      if (newImageLink) {
        this.zoomWindow.classList.add("cm-zoom-window--loading");
        this.imagePreload.src = newImageLink;
        // do not activate yet...
      } else {
        // always hide zoom if there is no picture
        this._deactivate();
        this.imagePreload.src = "";
        this.zoomWindow.classList.remove("cm-zoom-window--loading");
      }
      this._imageLink = newImageLink;
    }
  }

  get imageLink() {
    return this._imageLink;
  }

  destroy() {
    this.imagePreload.removeEventListener("load", this._boundLoad);

    this.target.removeEventListener("wheel", this._boundOnWheel);
    this.target.removeEventListener("mouseenter", this._boundMouseEnter);
    this.target.removeEventListener("mousemove", this._boundMouseMove);
    this.target.removeEventListener("mouseleave", this._boundMouseLeave);

    if (this.imagePreload) {
      delete this["imagePreload"];
    }
    if (this.zoomWindow) {
      document.body.removeChild(this.zoomWindow);
      delete this["zoomWindow"];
    }
    if (this.zoomLens) {
      document.body.removeChild(this.zoomLens);
      delete this["zoomLens"];
    }
    delete this["target"];
  }
}

export default Zoom;
