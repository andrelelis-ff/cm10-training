package com.coremedia.blueprint.personalization.editorplugin.taxonomy {
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.personalization.ui.util.RuleXMLCoDec;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.util.IdUtil;

import joo.debug;

public class TaxonomyConditionUtil {

  public static function getTaxonomyId4Chooser(propertyPrefix:String):String {
    if (propertyPrefix.indexOf("subject") !== -1 || propertyPrefix.indexOf("explicit") !== -1) {
      return 'Subject';
    }
    else if (propertyPrefix.indexOf("location") !== -1) {
      return 'Location';
    }
    else if (propertyPrefix.indexOf("queryLoc") !== -1) {
      return 'QueryLocation';
    }
    else if (propertyPrefix.indexOf("queryTax") !== -1) {
      return 'Query';
    }
    return propertyPrefix;
  }

  public static function formatPropertyValue4Store(value:String):String {
    var propertyValue:Number = new Number(value);
    return '' + propertyValue / 100;
  }


  public static function formatPropertyValue4Textfield(value:String):String {
    var propertyValue:Number = new Number(value)*100;
    return ''+propertyValue;
  }

  public static function formatPropertyName(prefix:String, taxonomy:Content):String {
    var id:int = IdUtil.MISSING_CONTENT_ID;
    if (taxonomy) {
      id = IdHelper.parseContentId(taxonomy);
    }
    return prefix + RuleXMLCoDec.INTERNAL_CONTENT_ID_PREFIX + id;
  }

  public static function getTaxonomyContent(property:String):Content {
    var split:Array = property.split(RuleXMLCoDec.INTERNAL_CONTENT_ID_PREFIX);
    if(split && split.length > 0) {
      var contentId:String = split[1];
      if(contentId > 0) {
        return beanFactory.getRemoteBean('content/' + contentId) as Content;
      }
    }
    if(debug) {
      trace("unable to retrieve content for taxonomy",property);
    }
    return null;
  }
}
}