package com.coremedia.blueprint.studio.forms.media {

/**
 * Model that represents one meta data type of a media item.
 */
public class MetaDataSection {
  private var metaDataType:String;
  private var data:Array = [];

  public function MetaDataSection(type:String) {
    this.metaDataType = type;
  }

  public function getMetaDataType():String {
    return metaDataType
  }

  public function length():Number {
    return data.length;
  }

  public function getData():Array {
    return data;
  }

  public function addProperty(property:String, value:String):void {
    var formattedValue:String = value;
    if(value) {
      formattedValue = value;
    }
    else {
      value = undefined;
    }
    data.push({property:property, value:value, formattedValue:formattedValue});
  }
}
}