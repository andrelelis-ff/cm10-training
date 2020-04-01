package com.coremedia.blueprint.studio.taxonomy.action {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.administration.TaxonomyExplorerPanel;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Component;
import ext.Ext;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class PasteKeywordActionBase extends Action {

  private var selectionExpression:ValueExpression;
  private var clipboardValueExpression:ValueExpression;

  internal native function get items():Array;

  public function PasteKeywordActionBase(config:PasteKeywordAction = null) {
    config.handler = pasteNodes;
    config.text = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyExplorerPanel_paste_button_label');
    config.disabled = true;
    super(config);
    clipboardValueExpression = config.clipboardValueExpression;
    selectionExpression = config.selectionExpression;
    selectionExpression.addChangeListener(updateDisabled);
    clipboardValueExpression.addChangeListener(updateDisabled);
  }

  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    updateDisabled();
  }

  private function updateDisabled():void {
    var selection:Array = selectionExpression.getValue();
    var disabled:Boolean = !clipboardValueExpression.getValue() || clipboardValueExpression.getValue().length === 0;

    for each(var node:TaxonomyNode in selection) {
      if (isNotPasteable(node)) {
        disabled = true;
        break;
      }

    }
    setDisabled(disabled);
  }

  private function isNotPasteable(node:TaxonomyNode):Boolean {
    if (clipboardValueExpression.getValue()) {
      for each(var clipboardNode:TaxonomyNode in clipboardValueExpression.getValue()) {
        if (clipboardNode.getRef() === node.getRef()) {
          return true;
        }
        if (clipboardNode.getTaxonomyId() !== node.getTaxonomyId()) {
          return true
        }
      }
    }
    return false;
  }

  /**
   * Copies the cutted node as a child of the selected node.
   */
  protected function pasteNodes():void {
    var targetNodes:Array = selectionExpression.getValue();
    var sourceNodes:Array = clipboardValueExpression.getValue();
    if (sourceNodes.length > 0 && targetNodes.length > 0) {
      var taxonomyExplorer:TaxonomyExplorerPanel = Ext.getCmp('taxonomyExplorerPanel') as TaxonomyExplorerPanel;
      taxonomyExplorer.moveNodes(sourceNodes, targetNodes[0]);
    }
  }

  override public function removeComponent(comp:Component):void {
    super.removeComponent(comp);
    if (items && items.length === 0) {
      selectionExpression && selectionExpression.removeChangeListener(updateDisabled);
      clipboardValueExpression && clipboardValueExpression.removeChangeListener(updateDisabled);
    }
  }
}
}