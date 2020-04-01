/**
 * Constructor of an object containing Google Analytics account data.
 *
 * @param webPropertyId the ID of the Google Analytics "web property" (e.g. 'UA-23708492-1') for which the page
 *    view will be counted
 * @param domainName domainName top level domain of the tracked page view
 */
function GaAccountData(webPropertyId, domainName) {
  this.webPropertyId = webPropertyId;
  this.domainName = domainName;
}

/**
 * Constructor of an object containing the data describing the current page view.
 *
 * @param contentId numeric content ID of the CoreMedia Content to be tracked
 * @param contentType type of the CoreMedia Content to be tracked, e.g. 'CMChannel'.
 * @param navigationPath a String containing the numeric content IDs of all channels from the root channel to
 *  the current page separated by '_'
 * @param pageUrl url of the current page
 * @param queryParameter the name of the url query-parameter you've configured in Google Analytics. To track site-internal
 *   searches, Google Analytics requires the query string to be supplied as the value of a parameter of the URL of the
 *   page. We'll use this value as the name of the query parameter. That is, if you supply 'query' and the query string is
 *   'foo', we'll append '?query=foo' to the URL of the page.
 * @param query the internal search query, if any
 * @param userSegments string representing the currently active CoreMedia Personalization user segments separated
 *    by '#', if any
 * @param disableAdFeaturesPlugin to disable Google's advertising feature plugin set to 'true' (default 'false')
 */
function GaPageviewData(contentId, contentType, navigationPath, pageUrl, queryParameter, query, userSegments, disableAdFeaturesPlugin) {
  this.contentId = contentId;
  this.contentType = contentType;
  this.navigationPath = navigationPath;
  this.pageUrl = pageUrl;
  this.queryParameter = queryParameter;
  this.query = query;
  this.userSegments = userSegments;
  this.disableAdvertisingFeaturesPlugin = typeof disableAdFeaturesPlugin !== 'undefined' ? disableAdFeaturesPlugin : false;
}

/**
 * Constructor of an object containing data describing an event on a page.
 *
 * @param category name of the category of the event (e.g. 'Videos')
 * @param action name of the action that has been performed  (e.g 'Play pressed')
 * @param name (optional) additional information used to specify the
 *    tracked event (e.g. 'Video diary: About our company')
 * @param value (optional) positive integer value to associate with
 *    the event (e.g. the number of seconds the video took to download)
 */
function GaEventData(category, action, name, value) {
  this.category = category;
  this.action = action;
  this.name = name;
  this.value = value;
}

/**
 * Tracks a pageview.
 *
 * Note that the actual tracking call will only be fired if Google's tracking library ('ga.js') is completely loaded.
 *
 * @param ga the Google Analytics command queue
 * @param gaAccountData an object containing the Google Analytics account data
 * @param gaPageviewData an object containing the data about the view that is to be tracked
 * @param trackerName (optional) the symbolic name of the Google Analytics tracker to be used
 */
function gaTrackPageview(ga, gaAccountData, gaPageviewData, trackerName) {

  var t = _gaTrackerPrefix(trackerName);

  //set Account
  if (trackerName && trackerName.length > 0) {
    ga('create', gaAccountData.webPropertyId, gaAccountData.domainName, {'name': trackerName});
  } else {
    ga('create', gaAccountData.webPropertyId, gaAccountData.domainName);
  }
  ga(t.concat('set'), 'anonymizeIp', true);
  if (!gaPageviewData.disableAdvertisingFeaturesPlugin) {
    ga(t.concat('require'), 'displayfeatures');
  }

  //set custom vars
  ga(t.concat('set'), 'dimension1', gaPageviewData.contentId);
  ga(t.concat('set'), 'dimension2', gaPageviewData.contentType);
  ga(t.concat('set'), 'dimension3', gaPageviewData.navigationPath);
  ga(t.concat('set'), 'dimension4', gaPageviewData.userSegments);

  // if a search was performed on the website, retain the search query
  var query = "";
  if (gaPageviewData.query && gaPageviewData.queryParameter) {
    query = "?" + gaPageviewData.queryParameter + "=" + encodeURIComponent(gaPageviewData.query);
  }

  //send page view
  ga(t.concat('send'), 'pageview', gaPageviewData.pageUrl + query);
}

/**
 * Tracks an event.
 *
 * @param ga the Google Analytics command queue
 * @param gaAccountData an object containing the Google Analytics account data
 * @param gaPageviewData an object containing the data about the view that is to be associated with the event
 * @param gaEventData an object containg the data about the event that is to be tracked
 * @param trackerName (optional) the symbolic name of the Google Analytics tracker to be used
 */
function gaTrackEvent(ga, gaAccountData, gaPageviewData, gaEventData, trackerName) {

  var t = _gaTrackerPrefix(trackerName);

  //set Account
  if (trackerName && trackerName.length > 0) {
    ga('create', gaAccountData.webPropertyId, gaAccountData.domainName, {'name': trackerName});
  } else {
    ga('create', gaAccountData.webPropertyId, gaAccountData.domainName);
  }
  ga(t.concat('set'), 'anonymizeIp', true);
  if (!gaPageviewData.disableAdvertisingFeaturesPlugin) {
    ga(t.concat('require'), 'displayfeatures');
  }

  //set custom vars
  ga(t.concat('set'), 'dimension1', gaPageviewData.contentId);
  ga(t.concat('set'), 'dimension2', gaPageviewData.contentType);
  ga(t.concat('set'), 'dimension3', gaPageviewData.navigationPath);
  ga(t.concat('set'), 'dimension4', gaPageviewData.userSegments);

  //sent event
  ga(t.concat('send'), 'event', gaEventData.category, gaEventData.action, gaEventData.name, gaEventData.value);
}


/**
 * Helper function to create a "tracker object" to be used in commands on Google's "ga" object.
 *
 * @param trackerName the symbolic name of the "tracker object" (see link below)
 *
 * @return tracker prefix for succeeding 'ga' commands
 *
 * @see &lt;a href="https://developers.google.com/analytics/devguides/collection/analyticsjs/advanced"&gt;Reference for ga&lt;/a&gt;
 */
function _gaTrackerPrefix(trackerName) {
  return (trackerName && trackerName.length > 0)
      ? trackerName.concat('.')
      : '';
}

