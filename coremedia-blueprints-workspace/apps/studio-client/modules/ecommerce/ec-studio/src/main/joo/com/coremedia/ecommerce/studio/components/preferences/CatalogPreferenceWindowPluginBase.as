package com.coremedia.ecommerce.studio.components.preferences {
import ext.Component;
import ext.container.Container;
import ext.plugin.AbstractPlugin;
import ext.tab.TabPanel;

public class CatalogPreferenceWindowPluginBase extends AbstractPlugin {

  public function CatalogPreferenceWindowPluginBase(config:CatalogPreferenceWindowPlugin = null) {
    super(config);
  }

  override public function init(component:Component):void {
    var prefWindow:Container = component as Container;
    var tabPanel:TabPanel = prefWindow.getComponent(0) as TabPanel;

      var prevPanel:CatalogPreferences = new CatalogPreferences(CatalogPreferences({}));
      tabPanel.add(prevPanel);
      tabPanel.updateLayout();
  }
}
}

