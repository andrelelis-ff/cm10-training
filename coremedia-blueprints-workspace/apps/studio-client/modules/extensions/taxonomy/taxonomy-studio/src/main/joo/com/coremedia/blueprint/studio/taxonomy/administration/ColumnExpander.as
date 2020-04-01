package com.coremedia.blueprint.studio.taxonomy.administration {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;

import ext.Ext;

/**
 * Delayed expanding of a column on mouse over.
 * The expanding may be cancelled when another node was hovered.
 */
public class ColumnExpander {
  private var targetNode:TaxonomyNode;
  private var cancelled:Boolean = false;

  public function ColumnExpander(targetNode:TaxonomyNode) {
    this.targetNode = targetNode;
  }

  public function cancel():void {
    this.cancelled = true;
  }

  public function expand():void {
    window.setTimeout(function ():void {
      if(!cancelled) {
        var taxonomyExplorer:TaxonomyExplorerPanel = Ext.getCmp('taxonomyExplorerPanel') as TaxonomyExplorerPanel;
        taxonomyExplorer.updateColumns(targetNode);
      }
    }, 500);
  }

  /**
   * Checks if this expander expands the given node.
   * @param targetNode
   * @return
   */
  public function expands(node:TaxonomyNode):Boolean {
    return targetNode.getRef() === node.getRef();
  }
}
}