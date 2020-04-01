package com.coremedia.blueprint.studio.taxonomy.action {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.administration.TaxonomyExplorerPanel;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Component;
import ext.Ext;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class DeleteKeywordActionBase extends Action {

  private var selectionExpression:ValueExpression;
  internal native function get items():Array;

  public function DeleteKeywordActionBase(config:CutKeywordAction = null) {
    config.handler = deleteNodes;
    config.disabled = true;
    config.text = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyExplorerPanel_delete_button_label');
    super(config);
    selectionExpression = config.selectionExpression;
    selectionExpression.addChangeListener(updateDisabled);
  }

  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    updateDisabled();
  }

  private function updateDisabled():void {
    var selections:Array = selectionExpression.getValue();
    var disable:Boolean = selections.length === 0;
    for each(var node:TaxonomyNode in selections) {
      if(node.isRoot()) {
        disable = true;
        break;
      }
    }
    setDisabled(disable);
  }

  /**
   * Remembers the node for a cut'n paste action
   */
  private function deleteNodes():void {
    var taxonomyExplorer:TaxonomyExplorerPanel = Ext.getCmp('taxonomyExplorerPanel') as TaxonomyExplorerPanel;
    taxonomyExplorer.deleteNodes();
    updateDisabled();
  }

  override public function removeComponent(comp:Component):void {
    super.removeComponent(comp);
    if (items && items.length === 0) {
      selectionExpression && selectionExpression.removeChangeListener(updateDisabled);
    }
  }
}
}