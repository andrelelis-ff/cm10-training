package com.coremedia.ecommerce.studio.components.preferences {
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preferences.PreferencePanel;
import com.coremedia.cms.editor.sdk.util.PreferencesUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.panel.Panel;

public class CatalogPreferencesBase extends Panel implements PreferencePanel {
  public static var PREFERENCE_SHOW_CATALOG_KEY:String = "showCatalogContent";
  public static var SORT_CATEGORIES_BY_NAME_KEY:String = "sortCategoriesByName";

  internal var showCatalogValueExpression:ValueExpression;
  internal var sortCategoriesByNameExpression:ValueExpression;

  public function CatalogPreferencesBase(config:CatalogPreferences = null) {
    super(config);
  }

  protected function getShowCatalogValueExpression():ValueExpression {
    if(!showCatalogValueExpression) {
      showCatalogValueExpression = ValueExpressionFactory.create(PREFERENCE_SHOW_CATALOG_KEY, editorContext.getPreferences());
    }
    return showCatalogValueExpression;
  }

  protected function getSortCategoriesByNameExpression():ValueExpression {
    if(!sortCategoriesByNameExpression) {
      sortCategoriesByNameExpression = ValueExpressionFactory.create(SORT_CATEGORIES_BY_NAME_KEY, editorContext.getPreferences());
    }
    return sortCategoriesByNameExpression;
  }

  public function updatePreferences():void {
    var showCatalogValue:String = getShowCatalogValueExpression().getValue();
    var sortCategoriesByNameValue:String = getSortCategoriesByNameExpression().getValue();
    PreferencesUtil.updatePreferencesJSONProperty(showCatalogValue, PREFERENCE_SHOW_CATALOG_KEY);
    PreferencesUtil.updatePreferencesJSONProperty(sortCategoriesByNameValue, SORT_CATEGORIES_BY_NAME_KEY);
  }
}
}
