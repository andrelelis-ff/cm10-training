package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.cms.editor.sdk.util.LinkListUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class PictureDocumentFormBase extends PropertyFieldGroup {
  private var gridEmptyValueExpression:ValueExpression;

  public function PictureDocumentFormBase(config:PictureDocumentForm = null) {
    super(config);
  }

  public function getGridEmptyValueExpression(config:PictureDocumentForm):ValueExpression {
    if (!gridEmptyValueExpression) {
      gridEmptyValueExpression = ValueExpressionFactory.createFromFunction(
              function ():Boolean {
                return LinkListUtil.getFreeCapacity(
                                config.bindTo,
                                config.picturePropertyName,
                                config.maxCardinality) < 1;
              }
      )
    }
    return gridEmptyValueExpression;
  }
}
}
