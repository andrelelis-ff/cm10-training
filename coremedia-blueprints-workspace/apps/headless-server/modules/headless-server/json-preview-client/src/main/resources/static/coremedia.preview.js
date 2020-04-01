/**
 *  CoreMedia Namespace
 */
var coremedia = (function (module) {
  return module;
}(coremedia || {}));

/**
 * CoreMedia preview module
 *
 * Initializes the communication between Studio and CAE to provide the PBE feature.
 *
 * The script is robust against multiple loading of webresources but it is assumed that on duplicate loading
 * the whole list of webresources is loaded multiple times (e.g. 2 x jQuery -> 2 x coremedia.preview).
 */
coremedia.preview = (function (module) {

  var hasMultipleInstances = typeof module.initialized !== typeof undefined;

  // trigger warning if preview is loaded multiple times (checked twice to allow better error reporting)
  if (hasMultipleInstances) {
    warn("Preview webresources are attached to DOM multiple times. Consider removing duplicates.");
  }

  // assuming that its enough to check if JQuery is set and has a function noConflict
  var jQueryExists = typeof window.jQuery !== typeof undefined && jQuery.noConflict;

  if (!jQueryExists) {
    warn("Cannot initialize preview: jQuery was not loaded");
    return module;
  }

  if (!window.JSON) {
    warn("Cannot initialize preview: JSON not supported");
  }

  if (!window.JSON || hasMultipleInstances) {
    // at least remove jQuery from global namespace, so non JSON supported browsers work as expected
    jQuery.noConflict(true);
    return module;
  }

  module.initialized = false;
  // remove jQuery from global namespace and attach it to the module
  module.$ = jQuery.noConflict(true);

  module.studioUrlWhitelist = module.studioUrlWhitelist || [];

  // easier access to jQuery through a local variable
  var $ = module.$;
  var $window = $(window);

  module.init = function () {
    if (!module.initialized) {
      if (window.parent && window.parent != window) {
        // Enable post message handling
        $window.on("message", module.initHandler);

        // Register at parent window
        var msg = JSON.stringify({
          type: "init",
          body: {
            windowType: "preview"
          }
        });
        window.parent.postMessage(msg, "*");
      }
      module.initialized = true;
    }
  };

  module.initHandler = function (event) {
    var msg = event.originalEvent.data;
    var origin = event.originalEvent.origin;
    var msgJson = undefined;
    try {
      msgJson = JSON.parse(msg);
    } catch(err) {}

    if (msgJson && msgJson.type === "initConfirm") {
      var parserOrigin = document.createElement("a");
      parserOrigin.href = origin;

      var parser = document.createElement("a");
      if (module.studioUrlWhitelist.length > 0) {
        $.each(module.studioUrlWhitelist, function (index, element) {
          parser.href = element;
          var wlProtocol = parser.protocol;
          var wlHost = parser.hostname;
          var wlPort = parser.port;

          if (wlProtocol === parserOrigin.protocol &&
                  wlHost === parserOrigin.hostname &&
                  wlPort === parserOrigin.port) {
            window.com_coremedia_pbe_studioUrl = origin;
            return false;
          }
        });
      } else {
        window.com_coremedia_pbe_studioUrl = "*";
      }

      if (window.com_coremedia_pbe_studioUrl && msgJson.body.url) {
        var script = document.createElement("script");
        script.type = "text/javascript";
        script.src = msgJson.body.url;
        // assuming that at least one script (this one) will be loaded via script tag
        var firstScript = document.getElementsByTagName("script")[0];
        firstScript.parentNode.insertBefore(script, firstScript);
      } else {
        warn("Preview received initConfirm message from origin " + origin + ". This does not match any of the given whitelist URLs (see 'pbe.studioUrlWhitelist')");
      }

      $window.off("message", module.initHandler);
    }
  };

  function warn(warnString) {
    if (window.console && window.console.warn) {
      window.console.warn(warnString);
    }
  }

  return module;
}(coremedia.preview || {}));

coremedia.preview.$ && coremedia.preview.$(function () {
  // init preview
  coremedia.preview.init();
});
