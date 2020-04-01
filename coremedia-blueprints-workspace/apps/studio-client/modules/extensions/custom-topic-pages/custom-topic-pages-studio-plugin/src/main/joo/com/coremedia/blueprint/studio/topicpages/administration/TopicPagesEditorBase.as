package com.coremedia.blueprint.studio.topicpages.administration {

import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

import ext.panel.Panel;

/**
 * Base class of the taxonomy administration tab.
 */
public class TopicPagesEditorBase extends Panel {
  public static const TOPIC_PAGES_EDITOR_ID:String = 'topicPagesEditor';

  private var selectionExpression:ValueExpression;

  public function TopicPagesEditorBase(config:TopicPagesEditorBase = null) {
    super(config);
  }

  /**
   * Returns the value expression that contains the active selection.
   * @return
   */
  protected function getSelectionExpression():ValueExpression {
    if(!selectionExpression) {
      selectionExpression = ValueExpressionFactory.create('selection', beanFactory.createLocalBean());
    }
    return selectionExpression;
  }
}
}
