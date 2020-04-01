package com.coremedia.blueprint.studio.util {

import com.coremedia.blueprint.base.components.viewtypes.ViewtypeLocalizationUtil;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.ObjectUtils;
import com.coremedia.ui.util.QtipUtil;
import com.coremedia.ui.models.bem.BEMBlock;

public class ViewtypeRenderer {

  private static const BLOCK:BEMBlock = new BEMBlock("cm-viewtype-thumb");

  public static function convert(value:String, content:Content):Object {
    if (content) {
      var hasProperty:Boolean = hasContentProperty(content, 'viewtype');
      if (hasProperty === undefined) {
        return undefined;
      }
      if (hasProperty) {
        var viewtypes:Array = ObjectUtils.getPropertyAt(content, 'properties.viewtype', null);
        if (viewtypes === undefined) {
          return undefined;
        }
        if (viewtypes && viewtypes.length > 0) {
          var viewType:Content = viewtypes[0];
          var thumbUri:String = ObjectUtils.getPropertyAt(viewType, 'properties.icon.uri', null);
          var viewTypeName:String = ViewtypeLocalizationUtil.localizeText(viewType);
          if (thumbUri === undefined) {
            return undefined;
          }
          if (viewTypeName === undefined) {
            return undefined;
          }
          return {url: thumbUri, viewTypeName: viewTypeName};
        }
      }
    }
    return {url: "", viewTypeName: ""};
  }

  public static function renderer(value:Object, metaData:*, record:BeanRecord):String {
    if (value) {
      var viewTypeName:String = value.viewTypeName;
      var thumbUri:String = value.url;
      if (viewTypeName && thumbUri) {
        return '<img '+QtipUtil.formatQtip(viewTypeName)+' src="' + thumbUri + '/s;w=32;h=22/rm" width="32" height="22" class="' + BLOCK + '"/>';
      }
    }
    return '<div style="width:32px;height:22px"></div>';  // spacer
  }

  /**
   * Returns true if the given content has the property field with the give name.
   * Returns undefined if the content has not yet been loaded.
   *
   * @param content the content
   * @param name the name of the property
   * @return true if the given content has the property
   */
  private static function hasContentProperty(content:Content, name:String):Boolean {
    var contentType:ContentType = content.getType();
    if (contentType === undefined) {
      return undefined;
    }
    var desc:Array = contentType.getDescriptors();
    for(var i:int = 0; i<desc.length; i++) {
      if(desc[i].name === name) {
        return true;
      }
    }
    return false;
  }
}
}
