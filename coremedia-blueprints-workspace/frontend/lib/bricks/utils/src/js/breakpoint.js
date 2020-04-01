/**
 * Polyfill for window.matchMedia
 * @see https://github.com/paulirish/matchMedia.js/
 * @returns {function}
 * @private
 */
const polyfill = () => {
  //noinspection JSUnresolvedVariable
  let styleMedia = window.styleMedia || window.media;

  // For those that don't support matchMedium
  if (!styleMedia) {
    const style = document.createElement("style");
    const script = document.getElementsByTagName("script")[0];
    let info = null;

    style.type = "text/css";
    style.id = "matchmediajs-test";

    if (!script) {
      document.head.appendChild(style);
    } else {
      script.parentNode.insertBefore(style, script);
    }

    // "style.currentStyle" is used by IE <= 8 and "window.getComputedStyle" for all other browsers
    info =
      ("getComputedStyle" in window && window.getComputedStyle(style, null)) ||
      style.currentStyle;

    styleMedia = {
      matchMedium: media => {
        const text = `@media ${media}{ #matchmediajs-test { width: 1px; } }`;

        // "style.styleSheet" is used by IE <= 8 and "style.textContent" for all other browsers
        if (style.styleSheet) {
          style.styleSheet.cssText = text;
        } else {
          style.textContent = text;
        }

        // Test if media query is true or false
        return info.width === "1px";
      },
    };
  }

  return media => ({
    matches: styleMedia.matchMedium(media || "all"),
    media: media || "all",
  });
};

//noinspection JSUnresolvedVariable
/**
 * Polyfill for window.matchMedia
 * @see https://github.com/paulirish/matchMedia.js/
 * @function
 */
const breakpoint = window.matchMedia || polyfill();

export default breakpoint;
