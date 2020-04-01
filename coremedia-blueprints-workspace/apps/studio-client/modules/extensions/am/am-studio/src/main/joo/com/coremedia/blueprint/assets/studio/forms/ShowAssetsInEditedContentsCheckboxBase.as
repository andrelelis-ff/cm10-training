package com.coremedia.blueprint.assets.studio.forms {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.PreferencesUtil;
import com.coremedia.collaboration.controlroom.rest.CapListRepositoryImpl;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.util.ObjectUtils;

import ext.form.field.Checkbox;

public class ShowAssetsInEditedContentsCheckboxBase extends Checkbox {

  private static const CONTROL_ROOM:String = "controlRoom";
  private static const SHOW_ASSETS:String = "showAssets";
  private static const ASSET_TYPE:String = "AMAsset";
  private static const DEFAULT_VALUE:Boolean = true;

  private var checkedValueExpression:ValueExpression;

  public function ShowAssetsInEditedContentsCheckboxBase(config:ShowAssetsInEditedContentsCheckbox = null) {
    super(config);

    CapListRepositoryImpl(CapListRepositoryImpl.getInstance()).registerEditedContentsFilterFn(filterAssets);
  }

  private function filterAssets(content:Content):Boolean {
    return !getCheckedValueExpression().getValue() && content.getType().isSubtypeOf(ASSET_TYPE);
  }

  protected function getCheckedValueExpression():ValueExpression {
    if (!checkedValueExpression) {
      checkedValueExpression = ValueExpressionFactory.createFromValue(false);
      checkedValueExpression.addChangeListener(saveChecked);
      checkedValueExpression.setValue(loadShowAssets());
    }
    return checkedValueExpression;
  }

  private static function loadShowAssets():Boolean {
    return !!ObjectUtils.getPropertyAt(editorContext.getPreferences(), [CONTROL_ROOM, SHOW_ASSETS], DEFAULT_VALUE);
  }

  private static function saveChecked(source:ValueExpression):void {
    PreferencesUtil.updatePreferencesJSONProperty(source.getValue(), CONTROL_ROOM, SHOW_ASSETS);
  }

  override protected function beforeDestroy():void {
    getCheckedValueExpression() && getCheckedValueExpression().removeChangeListener(saveChecked);

    super.beforeDestroy();
  }
}
}