package com.coremedia.blueprint.studio.taxonomy.rendering {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;

/**
 * Renderer is used for the leaf list in the selection dialog.
 * Since there are no path information shown, we only have to render
 * the leaf itself.
 */
public class SelectionListRenderer extends TaxonomyRenderer {
  private var selected:Boolean;

  public function SelectionListRenderer(nodes:Array, componentId:String, selected:Boolean) {
    super(nodes, componentId);
    this.selected = selected;
  }

  override protected function doRenderInternal(nodes:Array, callback:Function):void {
    var html:String = "";
    var node:TaxonomyNode = nodes[0];
    var addButton:Boolean = !selected;
    html += renderNode(node, false, !node.isLeaf(), addButton, selected);
    setHtml(html);
  }
}
}