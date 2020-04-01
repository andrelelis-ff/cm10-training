package com.coremedia.livecontext.p13n.studio {
import com.coremedia.ecommerce.studio.components.CommerceObjectSelector;
import com.coremedia.personalization.ui.condition.AbstractCondition;
import com.coremedia.personalization.ui.condition.OperatorSelector;
import com.coremedia.personalization.ui.util.SelectionRuleHelper;

import ext.data.Model;
import ext.form.field.ComboBox;

/**
 * Fires after the data represented by this component was modified. Intended to be used for
 * automatically saving changes.
 */
[Event(name = "modified")] // NOSONAR - no type

[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class CommerceSegmentConditionBase extends AbstractCondition{

  private static const MODIFIED_EVENT:String = "modified";
  private static const OPERATORS:Array = [SelectionRuleHelper.OP_CONTAINS];

  private var segmentSelector:CommerceObjectSelector;

  // the prefix of the properties rendered by this component
  private var propertyPrefix:String = "";

  public function CommerceSegmentConditionBase(config:CommerceSegmentCondition = null) {

    initSegmentSelector(config);

    super(config);

    // check the supplied configuration
    if (config.propertyPrefix === null || config.propertyPrefix === undefined) {
      throw new Error("config.propertyPrefix must not be null");
    }
    propertyPrefix = config['propertyPrefix'].length > 0 ? config['propertyPrefix'] + '.' : "";

    // create operator combo
    var operatorSelector:OperatorSelector = initOpSelector(null, config['operatorNames'], config['operatorEmptyText'], config['operator'],
            OPERATORS, DEFAULT_OPERATOR_DISPLAY_NAMES);

    add(operatorSelector);

    // init the segment selector
    add(segmentSelector);

  }

  private function initSegmentSelector(config:CommerceSegmentCondition):void {
    var segmentSelectorCfg:CommerceObjectSelector = CommerceObjectSelector({});
    segmentSelectorCfg.contentValueExpression = config.bindTo;
    segmentSelectorCfg.itemId = "segmentSelector";
    segmentSelectorCfg.flex = 30;
    segmentSelectorCfg.emptyText = resourceManager.getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'p13n_commerce_user_segments_selector_emptyText');
    segmentSelectorCfg.getCommerceObjectsFunction = LivecontextP13NStudioPluginBase.getSegments;
    segmentSelectorCfg.listConfig = {minWidth: 200};
    segmentSelectorCfg.forceSelection = true;
    segmentSelectorCfg.selectOnFocus = true;
    segmentSelectorCfg.typeAhead = true;
    segmentSelectorCfg.allowBlank = false;
    segmentSelectorCfg.triggerAction = "all";
    segmentSelectorCfg.queryMode = 'local';
    segmentSelectorCfg.quote = true;
    segmentSelector = new CommerceObjectSelector(segmentSelectorCfg);

    segmentSelector.addListener('change', function ():void {
      fireEvent(MODIFIED_EVENT);
    });

    segmentSelector.addListener('select', function (combo:ComboBox, record:Model, index:Number):void {
      fireEvent(MODIFIED_EVENT);
    });

    // if data changed (e.g. segment deleted), validate again
    segmentSelector.getStore().addListener('datachanged', validateStore);

    // validate again, if focus is lost (e.g. open dropdown, and click anywhere else)
    segmentSelector.addListener('blur', validateStore);

    // validate again on afterrender
    segmentSelector.addListener('afterrender', validateStore);

    // validate again on move
    segmentSelector.addListener('move', validateStore);

  }

  /**
   * Validates the comboBox entry. The comboBox will be marked as invalid if the comboBox store doesn't
   * contain the value of the comboBox.
   */
  private function validateStore():void {
    //the segmentId must be unquoted otherwise the store will not find even the valid id.
    var segmentId:String = segmentSelector.getUnquotedValue();
    if (segmentId) {
      // check if value is not in store
      if (segmentSelector.getStore().find(segmentSelector.valueField, segmentId) === -1) {
        // set segment name as rawValue to avoid checking out the document
        // mark as "invalid"
        segmentSelector.markInvalid(resourceManager.getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'p13n_context_commerce_segment_invalid'));
      } else {
        segmentSelector.clearInvalid();
      }
    }
  }

  /* ------------------------------------------

   Condition interface

   ------------------------------------------ */

  public override function getPropertyName():String {
    return propertyPrefix + 'usersegments';
  }

  public override function setPropertyName(name:String):void {
    //
  }

  public override function getPropertyValue():String {
    return segmentSelector.getValue();
  }

  public override function setPropertyValue(v:String):void {
    if (v !== null) {
      segmentSelector.setValue(v);
    }
    else {
      segmentSelector.clearValue();
    }
  }

}
}
