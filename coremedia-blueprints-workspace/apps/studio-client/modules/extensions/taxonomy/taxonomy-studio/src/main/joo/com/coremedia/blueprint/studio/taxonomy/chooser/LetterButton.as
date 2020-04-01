package com.coremedia.blueprint.studio.taxonomy.chooser {

import ext.Ext;
import ext.button.Button;

/**
 * A Button that displays its text only as overflowText. This Button also displays two icons as inline placed elements.
 */
public class LetterButton extends Button {

  public function LetterButton(config:Button = null) {
    super(Button(Ext.apply({
      cls: 'cm-taxonomy-letter-button',
      buttonSelector: 'a'
    }, config)));
    this['xtype'] = this['xtype'] || Button['xtype']; // set to default xtype when created through Action!
    this["overflowText"] = this["text"];
  }

}
}