package com.coremedia.blueprint.personalization.editorplugin.taxonomy {

import com.coremedia.personalization.ui.condition.OperatorSelector;
import com.coremedia.personalization.ui.util.SelectionRuleHelper;

import mx.resources.ResourceManager;

/**
 * A Condition specialized for editing <i>taxonomy conditions</i> with two possible values: 1 (taxonomy active) and
 * !1 (taxonomy not active). The keyword represents the taxonomy name. The value is set implicit by setting the operator
 * (contains =1; contains not !=1)
 *
 * @xtype com.coremedia.blueprint.studio.taxonomy.condition.BooleanTaxonomyCondition
 */
[ResourceBundle('com.coremedia.personalization.ui.Personalization')]
public class BooleanTaxonomyConditionBase extends AbstractTaxonomyCondition {


  private static const NO_VALUE_OPERATORS:Array = [
    SelectionRuleHelper.OP_EQUAL,
    SelectionRuleHelper.OP_NOTEQUAL
  ];

  protected static const NO_VALUE_OPERATOR_DISPLAY_NAMES:* = {};
  {
    NO_VALUE_OPERATOR_DISPLAY_NAMES[SelectionRuleHelper.OP_EQUAL] = ResourceManager.getInstance().getString('com.coremedia.personalization.ui.Personalization', 'p13n_op_contains');
    NO_VALUE_OPERATOR_DISPLAY_NAMES[SelectionRuleHelper.OP_NOTEQUAL] = ResourceManager.getInstance().getString('com.coremedia.personalization.ui.Personalization', 'p13n_op_contains_not');
  }

  public function BooleanTaxonomyConditionBase(config:BooleanTaxonomyCondition = null) {
    // create operator combo
    var operators:* = config['operators'];
    var operator:* = config['operator'];
    var opSelector:OperatorSelector = initOpSelector(null, config['operatorNames'], config['operatorEmptyText'], operator,
            NO_VALUE_OPERATORS, NO_VALUE_OPERATOR_DISPLAY_NAMES);

    super(config);

    addOpSelector(opSelector, operators, operator);

    // the default value is 100 (which means 100% -> 1)
    config.valueText = "100";

    addKeywordField();
    addTaxonomyButton();

    // the value field needs to be added, but should be hidden
    addValueField(config, false);

  }

  private function addOpSelector(opSelector:OperatorSelector, operators:*, operator:*):void {
    add(opSelector);

    const internalOperators:Array = operators != null ? convertToInternalNames(operators) : NO_VALUE_OPERATORS;
    on("afterrender", function ():void {
      opSelector.addListener('modified', function ():void {
        fireEvent('modified');
      });
      // set the initial operator
      opSelector.setValue(operator ? internalFromExternalOperatorName(operator) : internalOperators[0]);
    });
  }
}
}
