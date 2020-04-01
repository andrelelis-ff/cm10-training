package com.coremedia.blueprint.personalization.editorplugin.taxonomy {

import com.coremedia.personalization.ui.condition.OperatorSelector;
import com.coremedia.personalization.ui.util.SelectionRuleHelper;

/**
 * A Condition specialized for editing <i>taxonomy conditions</i> with values from 0 - 100. The keyword represents the
 * taxonomy name and the value field represents the percentage of that taxonomy.
 *
 * @xtype com.coremedia.blueprint.studio.taxonomy.condition.PercentageTaxonomyCondition
 */
public class PercentageTaxonomyConditionBase extends AbstractTaxonomyCondition {

  private static const OPERATORS:Array = [
    SelectionRuleHelper.OP_EQUAL,
    SelectionRuleHelper.OP_GREATER_THAN,
    SelectionRuleHelper.OP_GREATER_THAN_OR_EQUAL,
    SelectionRuleHelper.OP_LESS_THAN,
    SelectionRuleHelper.OP_LESS_THAN_OR_EQUAL
  ];

  public function PercentageTaxonomyConditionBase(config:PercentageTaxonomyCondition = null) {
    // create operator combo
    var operators:* = config['operators'];
    var operator:* = config['operator'];
    var opSelector:OperatorSelector = initOpSelector(null, config['operatorNames'], config['operatorEmptyText'], operator,
            OPERATORS, DEFAULT_OPERATOR_DISPLAY_NAMES);

    super(config);


    addKeywordField();
    addTaxonomyButton();
    addOpSelector(opSelector, operators, operator);
    addValueField(config);

  }

  private function addOpSelector(opSelector:OperatorSelector, operators:*, operator:*):void {
    add(opSelector);

    const internalOperators:Array = operators != null ? convertToInternalNames(operators) : OPERATORS;
    on("afterrender", function ():void {
      opSelector.addListener('select', function ():void {
        fireEvent('modified');
      });
      // set the initial operator
      opSelector.setValue(operator ? internalFromExternalOperatorName(operator) : internalOperators[0]);
    });
  }
}
}
