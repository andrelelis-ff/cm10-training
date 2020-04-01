package com.coremedia.blueprint.studio.taxonomy.preferences {

import ext.Component;
import ext.container.Container;
import ext.plugin.AbstractPlugin;
import ext.tab.TabPanel;

public class TaxonomyPreferenceWindowPluginBase extends AbstractPlugin {

  public function TaxonomyPreferenceWindowPluginBase(config:TaxonomyPreferenceWindowPlugin = null) {
    super(config);
  }

  override public function init(component:Component):void {
    var prefWindow:Container = component as Container;
    var tabPanel:TabPanel = prefWindow.getComponent(0) as TabPanel;

    var prevPanel:TaxonomyPreferences = new TaxonomyPreferences(TaxonomyPreferences({}));
    tabPanel.add(prevPanel);
    tabPanel.updateLayout();
  }
}
}

