/** Depending on JQuery library
 *
 * The regular expression to extract the content ID of each image link URL refers to the standard implementation
 * of the TransformedBlobHandler which delivers specified image URL pattern.
 * This can be overwritten by any other project specifications, so the expression 'expr' below has to be readjusted too.
 */
$(document).ready(function () {
  $('.lightboxGallery, .lightbox').on('mfpChange', function (e /*, params */) {
    if (arguments && arguments[1]) {
      var contentID = extractContentId(arguments[1].src);
      if (contentID && contentID[1]) {
        alxTrackEvent('ImageInteractions', 'maximize', contentID[1]);
      }
    }
  });

  $('a.download').click(function (eventData) {
    var target = eventData.target.href;
    var name;
    var contentID;
    var s = target.split("/");
    // e.g. /blob/126/4fb7741a1080d02953ac7d79c76c955c/document.pdf
    if (s.length > 3) {
      name = s[s.length - 1];
      contentID = s[s.length - 3];
    }
    if (name && contentID) {
      alxTrackEvent('Download', name, contentID);
    }
  });
});

function extractContentId(imageUrl) {
  if (imageUrl.indexOf("elastic") != -1) {
    return null;
  }
  var expr = new RegExp("image/(\\d+)");
  return expr.exec(imageUrl);
}

if (!window.__alxEventHandlers) {
  window.__alxEventHandlers = [];
  function registerAlxEventHandler(handler) {
    window.__alxEventHandlers.push(handler);
  }

  /**
   * Tracks an event for all active Analytics Integrations.
   *
   * @param eventCategory name of the category of the event (which "group of events"; e.g. 'Videos')
   * @param eventAction name of the action that has been performed  (e.g 'Play pressed')
   * @param eventName ID used to specify the tracked event (e.g. 'Video diary: About our company')
   * @param eventValue (optional) some arbitrary value to to associate with the event (e.g. the number of seconds the video took to download)
   */
  function alxTrackEvent(eventCategory, eventAction, eventName, eventValue) {
    var handlerCount = window.__alxEventHandlers.length;
    var handlerFunc;
    var i;

    if (handlerCount == 0) {
      if (console && console.log) {
        console.log("no analytics event handler registered");
      }
    } else {
      for (i = 0; i < handlerCount; ++i) {
        handlerFunc = window.__alxEventHandlers[i];
        handlerFunc(eventCategory, eventAction, eventName, eventValue);
      }
    }
  }
}
