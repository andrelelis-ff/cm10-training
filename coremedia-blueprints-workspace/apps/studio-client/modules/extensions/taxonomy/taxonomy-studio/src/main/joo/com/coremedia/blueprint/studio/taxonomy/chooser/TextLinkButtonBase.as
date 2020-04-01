package com.coremedia.blueprint.studio.taxonomy.chooser {

import ext.button.Button;

/**
 * A Button that displays its text only as overflowText. This Button also displays two icons as inline placed elements.
 */
public class TextLinkButtonBase extends Button {

  public function TextLinkButtonBase(config:TextLinkButton = null) {
    if(config.node) {
      var name:String = config.node.getName();
      if(config.weight) {
        name = name + " (" + config.weight + ")";
      }
      config.text = name;
    }
    super(config);
  }
}
}
