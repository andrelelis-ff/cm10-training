/**
 * debouncing function from John Hann
 *
 * @param func {function} the callback to trigger
 * @param threshold {int} the detection period, defaults to 200
 * @param execAsap {boolean} if true executes the function at the beginning and not after the detection peroid
 *
 * @see http://unscriptable.com/index.php/2009/03/20/debouncing-javascript-methods/
 */
export const debounce = (func, threshold = 200, execAsap = false) => {
  let timeout;

  return function debounced() {
    const obj = this;
    const args = arguments;

    function delayed() {
      if (!execAsap) {
        func.apply(obj, args);
      }
      timeout = null;
    }

    if (timeout) {
      clearTimeout(timeout);
    } else if (execAsap) {
      func.apply(obj, args);
    }

    timeout = setTimeout(delayed, threshold);
  };
};
