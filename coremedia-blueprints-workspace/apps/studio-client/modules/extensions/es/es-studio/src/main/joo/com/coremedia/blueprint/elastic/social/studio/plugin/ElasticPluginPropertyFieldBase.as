package com.coremedia.blueprint.elastic.social.studio.plugin {
import com.coremedia.elastic.social.studio.model.impl.ModerationImpl;

import ext.form.field.TextField;

public class ElasticPluginPropertyFieldBase extends TextField {
  public function ElasticPluginPropertyFieldBase(config:ElasticPluginPropertyField = null) {
    super(config);

    var expressionSegments:Array = config.expression.split(".");
    if (expressionSegments && expressionSegments.length > 0) {
      var property:String = expressionSegments[expressionSegments.length - 1];
      (ModerationImpl.getInstance() as ModerationImpl).registerModeratedProperties([property]);
    }
  }
}
}
