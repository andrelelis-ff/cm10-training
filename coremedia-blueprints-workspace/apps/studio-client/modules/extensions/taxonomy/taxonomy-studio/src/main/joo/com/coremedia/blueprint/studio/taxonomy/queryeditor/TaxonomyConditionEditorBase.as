package com.coremedia.blueprint.studio.taxonomy.queryeditor {

import com.coremedia.blueprint.base.queryeditor.conditions.ConditionEditor;

public class TaxonomyConditionEditorBase extends ConditionEditor {

  public function TaxonomyConditionEditorBase(config:TaxonomyConditionEditor = null) {
    super(config);

    // Ensures that the substruct the condition editor writes into is created as this acts as an indicator if the
    // form is shown (even if no context is specified) or not.
    var structPropertyName:String = propertyName.substring(propertyName.lastIndexOf(".")+1, propertyName.length);
    applyBaseStruct(bindTo, contentType, structPropertyName);
  }
}
}
