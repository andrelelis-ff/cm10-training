package com.coremedia.blueprint.studio.taxonomy.rendering {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.ui.util.EventUtil;

/**
 * Renders the search result displayed in the drop down of full text search of link list
 * and in the taxonomy administration.
 */
public class TaxonomySearchComboRenderer extends TaxonomyRenderer {
  private var wrapperId:String;
  public static const LIST_WIDTH:Number = 580;

  public function TaxonomySearchComboRenderer(nodes:Array, componentId:String) {
    super(nodes, null);
    this.wrapperId = componentId + "-wrapper-" + nodes[nodes.length - 1].ref;
  }

  override protected function doRenderInternal(nodes:Array, callback:Function):void {
    var html:String = '<div class="' + TaxonomyBEMEntities.NODE_WRAP + '" id="' + wrapperId + '">';

    for (var i:int = 1; i < nodes.length; i++) {
      var node:TaxonomyNode = new TaxonomyNode(nodes[i]);
      var isLeaf:Boolean = i === (nodes.length - 1);
      html += renderNode(node, !isLeaf, false, undefined, false);
    }

    html += '</div>';
    setHtml(html);

    var that = this;
    EventUtil.invokeLater(function ():void {
      new NodePathEllipsis(wrapperId, that, 580).autoEllipsis();
    });
  }
}
}