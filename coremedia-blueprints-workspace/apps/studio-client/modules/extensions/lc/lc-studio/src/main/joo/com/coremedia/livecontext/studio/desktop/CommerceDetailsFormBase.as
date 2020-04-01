package com.coremedia.livecontext.studio.desktop {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Ext;

public class CommerceDetailsFormBase extends PropertyFieldGroup {

  [Bindable]
  public var contentBindTo:ValueExpression;

  public function CommerceDetailsFormBase(config:CommerceDetailsForm = null) {
    //need to use a different stateSaveExpression for ecommerce objects as the default impl in PropertyFieldGroupBase expects catalogObject
    var formType:String = getFormType(config);
    if (formType) {
      stateSaveExpression = ValueExpressionFactory.create('forms.' + formType + '.' + config.itemId, editorContext.getPreferences());
    }
    super(config);
  }


  override protected function getCollapsedStateFromPremularConfig(config:PropertyFieldGroup):Boolean {
    if (stateSaveExpression && stateSaveExpression.isLoaded()) {
      var value:Boolean = stateSaveExpression.getValue() as Boolean;
      if (value !== undefined && value !== null) {
        return value;
      }
    }
    return config.collapsed;
  }

  private function getFormType(config:CommerceDetailsForm):String {
    if (config.contentBindTo) {
      var content:Content = config.contentBindTo.getValue() as Content;
      if (content) {
        return content.getType().getName();
      }
    }
    var catalogObject:CatalogObject = config.bindTo.getValue() as CatalogObject;
    if (catalogObject) {
      return Ext.getClassName(catalogObject).replace(/\./g, "_");
    }
    return null;
  }
}
}
