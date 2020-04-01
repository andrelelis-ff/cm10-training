package com.coremedia.ecommerce.studio.components.preferences {
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preferences.PreferencePanel;
import com.coremedia.cms.editor.sdk.util.PreferencesUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.panel.Panel;

public class CatalogPreferencesBase extends Panel implements PreferencePanel {
  public static var PREFERENCE_SHOW_CATALOG_KEY:String = "showCatalogContent";

  internal var showCatalogValueExpression:ValueExpression;

  public function CatalogPreferencesBase(config:CatalogPreferences = null) {
    super(config);
  }

  protected function getShowCatalogValueExpression():ValueExpression {
    if(!showCatalogValueExpression) {
      showCatalogValueExpression = ValueExpressionFactory.create(PREFERENCE_SHOW_CATALOG_KEY, editorContext.getPreferences());
    }
    return showCatalogValueExpression;
  }

  public function updatePreferences():void {
    var value:String = getShowCatalogValueExpression().getValue();
    PreferencesUtil.updatePreferencesJSONProperty(value, PREFERENCE_SHOW_CATALOG_KEY);
  }
}
}
