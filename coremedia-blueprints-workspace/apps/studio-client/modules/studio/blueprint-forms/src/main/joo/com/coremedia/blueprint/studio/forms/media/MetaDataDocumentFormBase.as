package com.coremedia.blueprint.studio.forms.media {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Ext;

/**
 * Base model of the image meta data document form.
 * The base class determines the different meta data type available for an image.
 */
public class MetaDataDocumentFormBase extends PropertyFieldGroup {
  private var metaDataExpression:ValueExpression;
  private var rawMetaDataExpression:ValueExpression;
  private var sections:Array = [];

  public function MetaDataDocumentFormBase(config:MetaDataDocumentForm = null) {
    super(config);
    rawMetaDataExpression = config.bindTo.extendBy('properties', config.propertyName, 'metadata', config.metadataSectionName);
    rawMetaDataExpression.addChangeListener(transformRawData);
    transformRawData(rawMetaDataExpression);
  }

  public function addSection(item:MetaDataSection):void {
    sections.push(item);
  }

  public function getSections():Array {
    return sections;
  }

  protected function transformRawData(ve:ValueExpression):void {
    sections = [];
    var value:Array = ve.getValue();
    if(Ext.isArray(value) && value.length > 0) {
      var result:Object = {};
      value.forEach(function(item:Object):void{
        var type:String = item.section;
        var metaDataSection:MetaDataSection = null;
        if(undefined === result[type]) {
          metaDataSection = new MetaDataSection(type);
          sections.push(metaDataSection);
          result[type] = metaDataSection;
        } else {
          metaDataSection = result[type];
        }
        metaDataSection.addProperty(item.property, item.value);
      });
    }
    metaDataExpression.setValue(sections);
  }

  private function hideOrShow():void {
    setVisible(getSections().length > 0);
  }

  public function getMetaDataExpression():ValueExpression {
    if (!metaDataExpression) {
      metaDataExpression = ValueExpressionFactory.createFromValue([]);
      metaDataExpression.addChangeListener(hideOrShow);
    }
    return metaDataExpression;
  }

  protected static function getTemplateKey(mt:MetaDataSection):String {
    return mt.getMetaDataType();
  }

  override protected function onDestroy():void {
    rawMetaDataExpression.removeChangeListener(transformRawData);
    metaDataExpression.removeChangeListener(hideOrShow);
    super.onDestroy();
  }
}
}