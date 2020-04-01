package com.coremedia.blueprint.studio.externalpreview {
import com.coremedia.blueprint.studio.externalpreview.dialog.ExternalPreviewWindow;

import ext.Action;
import ext.Ext;

/**
 * Action for creating a bookmark using the active document.
 */
public class ExternalPreviewActionBase extends Action {

  /**
   * @param config
   */
  public function ExternalPreviewActionBase(config:ExternalPreviewAction = null) {
    config['handler'] = showExternalPreviewDialog;
    super(config);
  }
  /**
   * The action handler for this action, checks single and multi-selection.
   */
  private function showExternalPreviewDialog():void {
    ExternalPreviewPluginBase.registerListeners();
    ExternalPreviewPluginBase.fireExternalPreviewUpdate();
    var window:ExternalPreviewWindow = Ext.getCmp('externalPreviewDialog') as ExternalPreviewWindow;
    if (!window) {
      window = new ExternalPreviewWindow();
    }
    window.show();
  }
}
}