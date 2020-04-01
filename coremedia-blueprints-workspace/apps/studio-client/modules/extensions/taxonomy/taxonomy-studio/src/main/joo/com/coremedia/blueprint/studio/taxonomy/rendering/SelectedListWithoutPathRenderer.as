package com.coremedia.blueprint.studio.taxonomy.rendering {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;

/**
 * The renderer used for the taxonomy filter in the library.
 */
public class SelectedListWithoutPathRenderer extends SelectedListRenderer {

  public function SelectedListWithoutPathRenderer(nodes:Array, componentId:String, scrolling:Boolean) {
    super(nodes, componentId, scrolling);
  }


  override protected function doRenderInternal(nodes:Array, callback:Function):void {
    var html:String = "";
    var nodeObject:Object = nodes[nodes.length-1];
    var node:TaxonomyNode = new TaxonomyNode(nodeObject);
    html += renderNode(node, false, false, false, false);
    callback.call(null, html);
  }
}
}