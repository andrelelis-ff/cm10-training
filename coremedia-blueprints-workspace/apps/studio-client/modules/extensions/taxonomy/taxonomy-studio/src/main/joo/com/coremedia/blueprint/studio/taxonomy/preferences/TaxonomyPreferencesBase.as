package com.coremedia.blueprint.studio.taxonomy.preferences {
import com.coremedia.blueprint.studio.TaxonomyStudioPlugin;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preferences.PreferencePanel;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.data.ArrayStore;
import ext.data.Store;
import ext.panel.Panel;

[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class TaxonomyPreferencesBase extends Panel implements PreferencePanel {
  public static var PREFERENCE_SEMANTIC_SETTINGS_KEY:String = "semanticSettings";

  internal var previewOptionValueExpression:ValueExpression;

  private static var comboStore:Array = [];

  public function TaxonomyPreferencesBase(config:TaxonomyPreferences = null) {
    super(config);
  }

  public static function addTaggingStrategy(serviceId:String, label:String):void {
    comboStore.push([label, serviceId]);
  }

  public function getStore():Store {
    var arrayStore:Store = new ArrayStore(ArrayStore({
      data:getTaxonomyOptions(),
      fields:['name', 'value']
    }));
    return arrayStore;
  }

  protected function getSuggestionTypesValueExpression():ValueExpression {
    if (!previewOptionValueExpression) {
      previewOptionValueExpression = ValueExpressionFactory.create('taxonomyOption', editorContext.getBeanFactory().createLocalBean());
      previewOptionValueExpression.addChangeListener(persistOptionSelection);
      var valueString:String = editorContext.getPreferences().get(PREFERENCE_SEMANTIC_SETTINGS_KEY);
      if (!valueString) {
        valueString = TaxonomyStudioPlugin.DEFAULT_SUGGESTION_KEY;
      }
      previewOptionValueExpression.setValue(valueString);
    }
    return previewOptionValueExpression;
  }


  public function getTaxonomyOptions():Array {
    return comboStore;
  }

  private function persistOptionSelection(ve:ValueExpression):void {
    var previewOption:String = ve.getValue();
    editorContext.getPreferences().set(PREFERENCE_SEMANTIC_SETTINGS_KEY, previewOption);
    editorContext.getApplicationContext().set(PREFERENCE_SEMANTIC_SETTINGS_KEY, previewOption);
  }

  public function updatePreferences():void {
    persistOptionSelection(previewOptionValueExpression);
  }
}
}
