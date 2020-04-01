package com.coremedia.blueprint.studio.topicpages.administration {
import com.coremedia.ui.data.ValueExpression;

import ext.container.Container;
import ext.event.Event;
import ext.form.field.Field;

/**
 * The super class of the filter search text field.
 */
public class FilterPanelBase extends Container {

  [Bindable]
  public var filterExpression:ValueExpression;

  [Bindable]
  public var applyFilterFunction:Function;

  public function FilterPanelBase(config:FilterPanelBase = null) {
    super(config);
    this.applyFilterFunction = config.applyFilterFunction;
  }

  /**
   * Executed when the user presses the enter key of the search area.
   * @param field The field the event was triggered from.
   * @param e The key event.
   */
  protected function applyFilterInput(field:Field, e:Event):void {
    if (e.getKey() === Event.ENTER) {
      applyFilterFunction();
      e.stopEvent();
    }
  }
}
}