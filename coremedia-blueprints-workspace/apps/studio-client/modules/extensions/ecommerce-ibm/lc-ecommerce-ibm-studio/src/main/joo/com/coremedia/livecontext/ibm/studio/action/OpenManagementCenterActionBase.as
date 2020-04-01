package com.coremedia.livecontext.ibm.studio.action {
import com.coremedia.cms.editor.sdk.actions.ActionConfigUtil;
import com.coremedia.livecontext.ibm.studio.mgmtcenter.ManagementCenterUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Action;
import ext.Component;

import mx.resources.ResourceManager;

/**
 * This action is intended to be used from within EXML, only.
 *
 */
[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class OpenManagementCenterActionBase extends Action {

  private var disabledExpression:ValueExpression;

  /**
   * @param config the configuration object
   */
  public function OpenManagementCenterActionBase(config:OpenManagementCenterAction = null) {
    super(OpenManagementCenterAction(ActionConfigUtil.extendConfiguration(ResourceManager.getInstance().getResourceBundle(null, 'com.coremedia.livecontext.studio.LivecontextStudioPlugin').content, config, 'openManagementCenter',
      {handler: function():void{
        ManagementCenterUtil.openManagementCenterView();
      }})));
    disabledExpression = ValueExpressionFactory.createFromFunction(calculateDisabled);
    disabledExpression.addChangeListener(updateDisabledStatus);
    updateDisabledStatus();

  }

  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    //broadcast the disable state after the add of a component
    updateDisabledStatus();
  }

  private function updateDisabledStatus():void {
    var value:* = disabledExpression.getValue();
    var disabled:Boolean = value === undefined || value;

    setDisabled(disabled);
  }

  private function calculateDisabled():Boolean {
    if (!ManagementCenterUtil.isSupportedBrowser()) {
      return true;
    }

    return  !ManagementCenterUtil.getUrl();
  }

}
}
