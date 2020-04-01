package com.coremedia.blueprint.elastic.social.studio.forms {

import ext.StringUtil;
import ext.Template;
import ext.window.Window;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.elastic.social.studio.ElasticSocialStudioPlugin')]
public class CMMailHelpWindowBase extends Window {

  public static const ID:String = "cmmail-help-window";

  internal static const TABLE_BODY_ID:String = "cmmail-help-table";
  internal static const TABLE:String = StringUtil.format(
          "<table><thead><tr><th>{0}</th><th>{1}</th></tr></thead><tbody id='{2}'></tbody></table><br/>",
          ResourceManager.getInstance().getString('com.coremedia.blueprint.elastic.social.studio.ElasticSocialStudioPlugin', 'cmmail_help_window_value'),
          ResourceManager.getInstance().getString('com.coremedia.blueprint.elastic.social.studio.ElasticSocialStudioPlugin', 'cmmail_help_window_description'),
          TABLE_BODY_ID
  );
  internal static const ROW:String = "<tr><td>{value}</td><td>{description}</td></tr>";
  internal static const KEY_PREFIX:String = "cmmail_help_window_value_";
  internal static const KEY_PATTERN:String = KEY_PREFIX + "(.+)";

  public function CMMailHelpWindowBase(config:* = undefined) {
    super(config);
    applyTemplate();
  }

  public function applyTemplate():void {
    var rowTpl:Template = new Template(ROW);
    rowTpl.compile();

    //noinspection JSMismatchedCollectionQueryUpdateInspection,JSMismatchedCollectionQueryUpdate
    var keys:Array = [];
    for (var key:String in resourceManager.getResourceBundle(null, 'com.coremedia.blueprint.elastic.social.studio.ElasticSocialStudioPlugin').content) {
      //noinspection JSUnfilteredForInLoop
      var match:Array = key.match(KEY_PATTERN);
      if (match) {
        keys.push(match[1]);
      }
    }
    keys.sort();
    keys.forEach(function(key:String):void {
      //noinspection JSUnusedGlobalSymbols
      rowTpl.append(TABLE_BODY_ID, {
        value: StringUtil.format("${{0}}", key),
        description: resourceManager.getString('com.coremedia.blueprint.elastic.social.studio.ElasticSocialStudioPlugin', KEY_PREFIX + key)
      });
    })
  }
}
}
