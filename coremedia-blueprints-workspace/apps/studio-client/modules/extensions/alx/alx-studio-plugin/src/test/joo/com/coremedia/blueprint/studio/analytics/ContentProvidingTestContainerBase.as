package com.coremedia.blueprint.studio.analytics {
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.container.Container;

public class ContentProvidingTestContainerBase extends Container {

  [Bindable]
  public var contentValueExpression:ValueExpression;

  public function ContentProvidingTestContainerBase(config:Container = null) {
    super(config);
  }

  public function setContent(content:Object):void {
    contentValueExpression.setValue(content);
  }

}
}