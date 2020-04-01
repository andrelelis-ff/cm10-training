package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;

import ext.Component;
import ext.form.CheckboxGroup;

public class PlayerSettingsPropertyGroupBase extends PropertyFieldGroup {

  public function PlayerSettingsPropertyGroupBase(config:PlayerSettingsPropertyGroup = null) {
    // this needs to be done to prevent hidden items from being rendered.
    // Otherwise hidden checkboxes would still use space in a column of the checkboxGroup
    var checkboxGroup:CheckboxGroup = config.items[0] as CheckboxGroup;
    if (checkboxGroup && checkboxGroup.items && checkboxGroup.items.length > 0) {
      checkboxGroup.items = checkboxGroup.items.filter(function(cmp:Component):Boolean {
        return !cmp.hidden;
      });
    }
    super(config);
  }
}
}
