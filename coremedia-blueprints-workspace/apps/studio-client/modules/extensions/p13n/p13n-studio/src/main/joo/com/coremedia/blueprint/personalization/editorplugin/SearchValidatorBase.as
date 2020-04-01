package com.coremedia.blueprint.personalization.editorplugin {
import com.coremedia.cms.editor.sdk.messageService;
import com.coremedia.cms.editor.sdk.premular.Premular;
import com.coremedia.cms.editor.sdk.preview.PreviewIFrame;
import com.coremedia.cms.editor.sdk.preview.PreviewMessageTypes;
import com.coremedia.cms.editor.sdk.preview.PreviewPanel;
import com.coremedia.ui.data.PropertyChangeEvent;
import com.coremedia.ui.util.createComponentSelector;

import ext.Component;
import ext.StringUtil;
import ext.container.Container;
import ext.form.field.Field;
import ext.plugin.AbstractPlugin;

import js.Window;

import mx.resources.ResourceManager;

/**
 * The field that uses this plugin retrieves the status of the search from the preview panel and adapts
 * its validation state and tooltip accordingly, thus providing better error feedback to the Studio user.
 */
[ResourceBundle('com.coremedia.blueprint.personalization.editorplugin.PersonalizationPlugIn')]
public class SearchValidatorBase extends AbstractPlugin {

  // name of the data attribute used in the preview page to store the search message
  private static const P13N_SEARCHSTATUS_DATA_ATTRIBUTE:String = "cm-personalization-editorplugin-searchstatus";
  private var prevPanel:Container;
  private var field:Field;
  private var comp:Component;

  public function SearchValidatorBase(config:SearchValidator = null) {
    super(config);
  }

  override public function init(component:Component):void {
    field = component as Field;
    comp = component;
    comp.addListener("afterrender", onAfterrender);
  }

  /**
   * Performed on the afterrender event. Does stuff that requires a rendered component.
   */
  public function onAfterrender():void {
    this.prevPanel = findPreviewPanel();
    this.prevPanel.addListener('previewUrl', onPreviewUrlChange);
  }

  /**
   * Find the preview panel in the premular this field is placed in. Make sure this is only
   * called on a rendered component.
   *
   * @return the preview panel
   *
   * @throws Error if the preview panel cannot be found
   */
  private function findPreviewPanel():Container {
    if (!comp.rendered) {
      throw new Error("findPreviewPanel must only be called on a rendered component");
    }

    const prem:Container = comp.findParentByType(Premular.xtype) as Container;
    if (prem) {
      const preview:Container = prem.down(createComponentSelector()._xtype(PreviewPanel.xtype).build()) as Container;
      if (preview) {
        return preview;
      }
    }
    // didn't found the preview
    throw new Error("unable to locate Preview Panel. Has this component been rendered already?");
  }

  /**
   * Called when the contents of the preview change. Retrieves the search status object from the preview and
   * adapts the state of this field if necessary.
   *
   * @param event the 'previewUrl changed' event
   */
  public function onPreviewUrlChange(event:PropertyChangeEvent):void {
    if (event.newValue) {
      // retrieve the search message from the preview
      var targetWindow:Window = PreviewIFrame(prevPanel.down(createComponentSelector()._xtype(PreviewIFrame.xtype).build())).getContentWindow();
      var messageBody:Object = {dataAttributeName: P13N_SEARCHSTATUS_DATA_ATTRIBUTE};
      messageService.sendMessage(targetWindow, PreviewMessageTypes.RETRIEVE_DATA_ATTRIBUTE, messageBody, function(responseBody:Object):void {
        var searchStatus:Array = responseBody.value as Array;

        if (searchStatus && searchStatus.length > 0) {
          field['validator'] = function (value:*):* {
            return toTooltip(searchStatus[0]);
          };
          field.validate();
        }
        else {
          field['validator'] = function (value:*):* {
            return true;
          };
          field.validate();
        }
      }, prevPanel);
    }
  }

  /**
   * Converts the supplied status object into a string to be shown in a tooltip.
   *
   * @param status the object representing the search status
   *
   * @return tooltip representing the search status
   */
  private static function toTooltip(status:*):String {
    const code:String = status['code'];
    var msg:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.personalization.editorplugin.PersonalizationPlugIn', code);
    if (!msg) {
      msg = status['msg'];
    }
    switch (code) {
      case "ARGUMENT_VALUE":
      case "ARGUMENT_UNKNOWN":
      case "ARGUMENT_SYNTAX":
      case "ARGUMENT_MISSING":
        return StringUtil.format(msg, status['func'], status['param'], status['msg']);
      case "FUNCTION_EVALUATION":
      case "FUNCTION_UNKNOWN":
        return StringUtil.format(msg, status['func'], status['msg']);
      case "GENERAL":
      default:
        return StringUtil.format(msg, status['msg']);
    }
  }
}
}