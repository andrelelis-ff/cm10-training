package com.coremedia.blueprint.assets.studio.search {

import com.coremedia.blueprint.assets.studio.AssetConstants;
import com.coremedia.cms.editor.sdk.collectionview.search.*;
import com.coremedia.cms.editor.sdk.util.FormatUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.mixins.LazyItemsContainerMixin;

import ext.container.Container;
import ext.form.CheckboxGroup;
import ext.form.field.Checkbox;

[ResourceBundle('com.coremedia.blueprint.assets.studio.AMStudioPlugin')]
public class StringListCheckboxFilterPanelBase extends FilterPanel {
  private var checkboxContainer:Container;

  public function StringListCheckboxFilterPanelBase(config:StringListCheckboxFilterPanel = null) {
    super(config);
    addListener(LazyItemsContainerMixin.LAZY_ITEMS_ADDED_EVENT, afterLazyItemsAdded);
  }

  private function afterLazyItemsAdded():void {
    checkboxContainer = getComponent('checkboxContainer') as Container;
    availableValuesValueExpression.addChangeListener(createCheckboxes);
    createCheckboxes();
  }

  /**
   * A value expression evaluating to a list of strings. For each string one checkbox is rendered.
   * The checkbox label is localized in the file AMStudioPlugin.properties with the following pattern:
   * 'Asset_metadata_[propertyName]_[value]_text'.
   */
  [Bindable]
  public var availableValuesValueExpression:ValueExpression;

  internal native function get solrField():String;

  internal native function get propertyName():String;

  override public function buildQuery():String {
    var selectedValues:Array = getStateBean().get(getFilterId());
    if (!selectedValues || selectedValues.length === 0) {
      return '';
    }

    return FormatUtil.format('{0}:({1})', solrField || getFilterId(), selectedValues.join(" AND "));
  }

  override public function getDefaultState():Object {
    var state:Object = {};
    state[getFilterId()] = undefined;
    return state;
  }

  private function createCheckboxes():void {
    var availableValues:Array = availableValuesValueExpression.getValue();
    if (!availableValues || availableValues.length === 0) {
      return;
    }

    var checkboxConfigs:Array = availableValues.map(function (availableValue:String):Checkbox {
      var propertyKey:String = 'Asset_' + AssetConstants.PROPERTY_ASSET_METADATA + '_' + (propertyName || getFilterId()) + '_' + availableValue + '_text';
      var checkboxConfig:Checkbox = Checkbox({});
      checkboxConfig.boxLabel = resourceManager.getString('com.coremedia.blueprint.assets.studio.AMStudioPlugin', propertyKey) || availableValue;
      checkboxConfig.name = getFilterId();
      checkboxConfig.inputValue = availableValue;
      checkboxConfig.itemId = availableValue;
      checkboxConfig.hideLabel = true;
      return checkboxConfig;
    });

    var checkboxGroupConfig:CheckboxGroup = CheckboxGroup({});
    checkboxGroupConfig.items = checkboxConfigs;

    checkboxContainer.removeAll();
    checkboxContainer.add(checkboxGroupConfig);

    updateLayout();
  }

  internal function transformer(strings:Array):Object {
    var valueObject:Object = {};

    if (strings.length == 1 && strings[0]) {
      valueObject[getFilterId()] = strings[0];
    } else {
      valueObject[getFilterId()] = strings;
    }
    return valueObject;
  }

  internal function reverseTransformer(selectedCheckboxes:Object):Array {
    if (selectedCheckboxes[getFilterId()]) {
      return [].concat(selectedCheckboxes[getFilterId()]);
    }
    return undefined;
  }
}
}
