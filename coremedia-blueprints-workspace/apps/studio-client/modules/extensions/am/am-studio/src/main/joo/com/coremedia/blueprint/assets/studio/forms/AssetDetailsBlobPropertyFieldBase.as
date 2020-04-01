package com.coremedia.blueprint.assets.studio.forms {

import com.coremedia.cms.editor.sdk.premular.fields.BlobPropertyField;
import com.coremedia.cms.editor.sdk.util.ImageUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Component;
import ext.container.Container;
import ext.form.field.Checkbox;

public class AssetDetailsBlobPropertyFieldBase  extends BlobPropertyField {

  private var blobPropertyVE:ValueExpression;

  private var checkboxVisibleVE:ValueExpression;

  public const CHECKBOX_ITEM_ID:String = "checkBoxItemId";

  public function AssetDetailsBlobPropertyFieldBase(config:AssetDetailsBlobPropertyField = null) {
    super(config);

    getBlobPropertyVE(config.bindTo, config.propertyName).addChangeListener(updateCheckbox);
  }

  protected function getBlobPropertyVE(bindTo:ValueExpression, propertyName:String):ValueExpression {
    if (!blobPropertyVE) {
      blobPropertyVE = bindTo.extendBy('properties', propertyName);
    }
    return blobPropertyVE;
  }

  private function updateCheckbox():void {
    if (!blobPropertyVE.getValue()) {
      var checkbox:Checkbox = findCheckBox();
      if (checkbox) {
        checkbox.setValue(false);
      }
    }
  }

  protected function getCheckboxVisibleVE(visiblePropertyName:String, bindTo:ValueExpression, propertyName:String):ValueExpression {
    if (!checkboxVisibleVE) {
      checkboxVisibleVE = ValueExpressionFactory.createFromFunction(visible, visiblePropertyName, getBlobPropertyVE(bindTo, propertyName));
    }

    return checkboxVisibleVE;
  }

  /**
   * If no property or no blob exist, hide component.
   *
   * @param property
   * @param blobVE
   * @return false if no property or blob exist.
   */
  protected static function visible(property:String, blobVE:ValueExpression):Boolean {
    if (!property) {
      return false;
    }

    return blobVE && blobVE.getValue() && blobVE.getValue().getSize() !== undefined;
  }

  protected static function findBlobDetailsContainer(container:Container):Component {
    return container.queryById(BLOB_DETAILS_ITEM_ID);
  }

  private function findCheckBox():Checkbox {
    var checkbox:Checkbox = queryById(CHECKBOX_ITEM_ID) as Checkbox;
    return (checkbox) ? checkbox : null;
  }

  override protected function onDestroy():void {
    blobPropertyVE.removeChangeListener(updateCheckbox);

    super.onDestroy();
  }

  override protected function getBlobImage(uri:String, width:int, height:int):String {
    if (uri) {
      var url:String = ImageUtil.getCroppingUri(uri, width, height);
      if (url) {
        return url;
      }
    }
    return uri;
  }

}
}
