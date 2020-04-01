package com.coremedia.blueprint.studio.googleanalytics {

import ext.form.field.VTypes;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.studio.googleanalytics.GoogleAnalyticsStudioPlugin')]
public class GoogleAnalyticsWebPropertyIdValidator {

  public static var WEB_PROPERTY_ID_KEY :String = "webPropertyId";

  VTypes['webPropertyIdVal'] = new RegExp(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.googleanalytics.GoogleAnalyticsStudioPlugin', 'googleanalytics_webpropertyid_val'));
  VTypes['webPropertyIdMask'] = new RegExp(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.googleanalytics.GoogleAnalyticsStudioPlugin', 'googleanalytics_webpropertyid_mask'));
  VTypes['webPropertyIdText'] = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.googleanalytics.GoogleAnalyticsStudioPlugin', 'googleanalytics_webpropertyid_text');
  VTypes['webPropertyId'] = function(v:*):* {
    return VTypes['webPropertyIdVal'].test(v);
  };
}
}