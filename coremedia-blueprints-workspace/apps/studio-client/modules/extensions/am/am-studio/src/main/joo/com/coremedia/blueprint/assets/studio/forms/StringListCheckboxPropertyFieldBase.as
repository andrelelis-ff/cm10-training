package com.coremedia.blueprint.assets.studio.forms {

import com.coremedia.cms.editor.sdk.premular.PropertyFieldPlugin;
import com.coremedia.cms.editor.sdk.validation.ShowIssuesPlugin;
import com.coremedia.ui.components.AdvancedFieldContainer;
import com.coremedia.ui.components.StatefulCheckbox;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.mixins.IValidationStateMixin;
import com.coremedia.ui.mixins.ValidationState;

import ext.form.field.Checkbox;

[ResourceBundle('com.coremedia.blueprint.assets.studio.AMStudioPlugin')]
public class StringListCheckboxPropertyFieldBase extends AdvancedFieldContainer implements IValidationStateMixin {
  private var propertyValueExpression:ValueExpression;
  private var structValueExpression:ValueExpression;


  /** @inheritDoc */
  public native function initValidationStateMixin():void;

  /** @private */
  [Bindable]
  public native function set validationState(validationState:ValidationState):void;

  /** @inheritDoc */
  [Bindable]
  public native function get validationState():ValidationState;

  /** @private */
  [Bindable]
  public native function set validationMessage(validationMessage:String):void;

  /** @inheritDoc */
  [Bindable]
  public native function get validationMessage():String;


  public function StringListCheckboxPropertyFieldBase(config:StringListCheckboxPropertyField = null) {
    super(config);
    initValidationStateMixin();
  }

  internal native function get structName():String;

  internal native function get propertyName():String;

  /**
   * A value expression evaluating to a list of strings. For each string one checkbox is rendered.
   * The checkbox label is localized in the file AMStudioPlugin.properties with the following pattern:
   * 'Asset_[structName]_[propertyName]_[value]_text'.
   */
  [Bindable]
  public var availableValuesValueExpression:ValueExpression;
  [Bindable]
  public var bindTo:ValueExpression;

  internal native function get hideIssues():Boolean;

  private function getPropertyValueExpression():ValueExpression {
    if (!propertyValueExpression) {
      propertyValueExpression = bindTo.extendBy('properties', structName, propertyName);
    }
    return propertyValueExpression;
  }

  private function getStructValueExpression():ValueExpression {
    if (!structValueExpression) {
      structValueExpression = bindTo.extendBy('properties', structName);
    }
    return structValueExpression;
  }

  internal function computeCheckboxConfigs():Object {
    var availableValues:Array = availableValuesValueExpression.getValue();
    var struct:RemoteBean = getStructValueExpression().getValue() as RemoteBean;

    // make sure everything is loaded
    if (availableValues === undefined || struct === undefined) {
      return undefined;
    }
    if (struct && !struct.isLoaded()) {
      struct.load();
      return undefined;
    }

    // extend available values with values stored in the struct (if any)
    var valuesStoredInContent:Array = getPropertyValueExpression().getValue();
    if (valuesStoredInContent) {
      var valuesOnlyStoredInContent:Array = valuesStoredInContent.filter(function (value:String):Boolean {
        return availableValues.indexOf(value) === -1;
      });
      availableValues = availableValues.concat(valuesOnlyStoredInContent);
    }
    availableValues = availableValues.filter(function (value:String):Boolean {
      return !!value;
    });

    var checkboxConfigs:Array = availableValues.map(function (availableValue:String):Checkbox {
      var propertyKey:String = 'Asset_' + structName + '_' + propertyName + '_' + availableValue + '_text';
      var checked:Boolean = valuesStoredInContent && valuesStoredInContent.indexOf(availableValue) !== -1;

      var checkboxConfig:StatefulCheckbox = StatefulCheckbox({});
      checkboxConfig.boxLabel = resourceManager.getString('com.coremedia.blueprint.assets.studio.AMStudioPlugin', propertyKey) || availableValue;
      checkboxConfig.name = propertyName;
      checkboxConfig.inputValue = availableValue;
      checkboxConfig.itemId = availableValue;
      checkboxConfig.hideLabel = true;
      checkboxConfig.checked = checked;
      var propertyFieldPluginConfig:PropertyFieldPlugin = PropertyFieldPlugin({});
      propertyFieldPluginConfig.propertyName = structName + '.' + propertyName + '.' + availableValue;
      var showIssuesPluginConfig:ShowIssuesPlugin = ShowIssuesPlugin({});
      showIssuesPluginConfig.bindTo = bindTo;
      showIssuesPluginConfig.hideIssues = hideIssues;
      showIssuesPluginConfig.propertyName = structName + '.' + propertyName + '.' + availableValue;
      checkboxConfig.plugins = [
        propertyFieldPluginConfig,
        showIssuesPluginConfig
      ];
      return checkboxConfig;
    });

    return checkboxConfigs;
  }

  internal function transformer(strings:Array):Object {
    var valueObject:Object = {};

    if (strings.length == 1 && strings[0]) {
      valueObject[propertyName] = strings[0];
    } else {
      valueObject[propertyName] = strings;
    }
    return valueObject;
  }

  internal function reverseTransformer(selectedCheckboxes:Object):Array {
    if (selectedCheckboxes[propertyName]) {
      return [].concat(selectedCheckboxes[propertyName]);
    }
    return [];
  }

}
}
