package com.coremedia.blueprint.studio.taxonomy.rendering {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;

/**
 * Renderer is used for the leaf list in the selection dialog when there is only single selection allowed.
 * Since there are no path information shown, we only have to render
 * the leaf itself. We re-use the link list renderer again, since the leaf
 * layout matches the one of the regular taxonomy link lists.
 */
public class SingleSelectionListRenderer extends SelectionListRenderer {
  private var selected:Boolean;
  private var selectionExists:Boolean;

  public function SingleSelectionListRenderer(nodes:Array, componentId:String, selected:Boolean, selectionExists:Boolean) {
    super(nodes, componentId, selected);
    this.selected = selected;
    this.selectionExists = selectionExists;
  }

  override protected function doRenderInternal(nodes:Array, callback:Function):void {
    var html:String = "";
    var node:TaxonomyNode = nodes[0];
    var addButton:Boolean = undefined;
    if(selected) {
      addButton = false;
    }
    else if(!selectionExists) {
      addButton = true;
    }
    html += renderNode(node, false, !node.isLeaf(), addButton, selected);
    setHtml(html);
  }

}
}