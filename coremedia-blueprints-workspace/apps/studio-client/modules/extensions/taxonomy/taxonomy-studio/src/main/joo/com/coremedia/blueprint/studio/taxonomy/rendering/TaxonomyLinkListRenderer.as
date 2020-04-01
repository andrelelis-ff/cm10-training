package com.coremedia.blueprint.studio.taxonomy.rendering {

import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.ui.util.EventUtil;

/**
 * The renderer used for the regular taxonomy link lists (property editor).
 */
public class TaxonomyLinkListRenderer extends TaxonomyRenderer {
  private var wrapperId:String;

  public function TaxonomyLinkListRenderer(nodes:Array, componentId:String) {
    super(nodes, componentId);
    this.wrapperId = componentId + "-wrapper-" + nodes[nodes.length - 1].ref.replace('/','-');
  }

  override protected function doRenderInternal(nodes:Array, callback:Function):void {
    var html:String = '<div class="' + TaxonomyBEMEntities.NODE_WRAP + '" style="text-align:left;" id="' + wrapperId + '">';

    for (var i:int = 1; i < nodes.length; i++) {
      var node:TaxonomyNode = new TaxonomyNode(nodes[i]);
      var isLeaf:Boolean = (i === nodes.length - 1);
      var addButton:Boolean = undefined;
      if(isLeaf) {
        addButton = plusMinus();
      }
      var selected:Boolean = isSelected(node, isLeaf);

      html += renderNode(node, !isLeaf, false, addButton, selected);
    }

    html += '</div>';
    callback.call(null, html);

    var that = this;
    //well...don't ask
    EventUtil.invokeLater(function():void {
      EventUtil.invokeLater(function():void {
        new NodePathEllipsis(wrapperId, that).autoEllipsis();
      });
    });
  }

  protected function isSelected(node:TaxonomyNode, isLeaf:Boolean):Boolean {
    return isLeaf;
  }

  protected function plusMinus():Boolean {
    return false;
  }
}
}
