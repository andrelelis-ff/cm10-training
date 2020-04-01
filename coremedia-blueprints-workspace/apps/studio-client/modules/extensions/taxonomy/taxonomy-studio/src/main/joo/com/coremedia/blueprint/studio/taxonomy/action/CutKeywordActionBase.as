package com.coremedia.blueprint.studio.taxonomy.action {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.administration.TaxonomyExplorerPanel;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Component;
import ext.Ext;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class CutKeywordActionBase extends Action {

  private var selectionExpression:ValueExpression;
  private var clipboardValueExpression:ValueExpression;
  internal native function get items():Array;

  public function CutKeywordActionBase(config:CutKeywordAction = null) {
    config.handler = cutNode;
    config.disabled = true;
    config.text = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyExplorerPanel_cut_button_label');
    super(config);
    clipboardValueExpression = config.clipboardValueExpression;
    selectionExpression = config.selectionExpression;
    selectionExpression.addChangeListener(updateDisabled);
  }

  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    updateDisabled();
  }

  private function updateDisabled():void {
    var selections:Array = selectionExpression.getValue();
    var disable = selections.length === 0;
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
  private function cutNode():void {
    var taxonomyExplorer:TaxonomyExplorerPanel = Ext.getCmp('taxonomyExplorerPanel') as TaxonomyExplorerPanel;

    var previousSelection:Array = clipboardValueExpression.getValue();
    var selections:Array = selectionExpression.getValue();
    clipboardValueExpression.setValue(selections);

    if (previousSelection) {
      for each(var prevSelection:TaxonomyNode in previousSelection) {
        if (taxonomyExplorer.getColumnContainer(prevSelection)) {
          taxonomyExplorer.getColumnContainer(prevSelection).updateNode(prevSelection);
        }
      }
    }


    for each(var selection:TaxonomyNode in selections) {
      taxonomyExplorer.getColumnContainer(selection).updateNode(selection);
    }

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