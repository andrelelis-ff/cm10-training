package com.coremedia.blueprint.personalization.editorplugin.property {
import ext.Ext;
import ext.data.ArrayStore;
import ext.form.field.ComboBox;

public class CMPersonaFormComboBoxBase extends ComboBox {

  /**
   * @cfg {Object} properties the enumeration of possible properties and their display names. See below
   * @param config
   */
  public function CMPersonaFormComboBoxBase(config:CMPersonaFormComboBox = null) {

    super(ComboBox(Ext.apply(config, {

      store: new ArrayStore(ArrayStore({
        id: 0,
        fields: [
          'myId',
          'displayText'
        ],
        data: config['properties']
      })),
      valueField: 'myId',
      displayField: 'displayText'
    })));
  }
}
}
