package com.coremedia.blueprint.studio.taxonomy.rendering {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;

/**
 * The renderer used for rendering the suggestions.
 * The rendering is almost the same like for regular link lists, so we
 * extend the link list renderer and modify the leaf rendering.
 */
public class SuggestionsRenderer extends TaxonomyLinkListRenderer {
  private var weight:String;
  private var leaf:Object;

  public function SuggestionsRenderer(nodes:Array, componentId, weight:String) {
    super(nodes, componentId);
    this.weight = weight;
    this.leaf = nodes[nodes.length-1];
  }

  override protected function doRenderInternal(nodes:Array, callback:Function):void {
    super.doRenderInternal(nodes, callback);
  }


  override protected function isSelected(node:TaxonomyNode, isLeaf:Boolean):Boolean {
    return false;
  }

  override protected function plusMinus():Boolean {
    return true;
  }

  /**
   * Overwrites the name rendering to add the weight
   * information calculated by the suggestions plugin.
   * @param node The node to render the name for.
   * @return
   */
  override protected function getLeafName(node:TaxonomyNode):String {
    var name:String = super.getLeafName(node);
    if(weight && node.getRef() === leaf.ref) {
      name+= ' (' + weight + ')';
    }
    return name;
  }
}
}