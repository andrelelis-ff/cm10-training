package com.coremedia.blueprint.studio.taxonomy.rendering {

/**
 * The renderer used for the taxonomy link lists in the selection dialog (upper list).
 */
public class SelectedListRenderer extends TaxonomyLinkListRenderer {
  private var scrolling:Boolean;

  public function SelectedListRenderer(nodes:Array, componentId:String, scrolling:Boolean) {
    super(nodes, componentId);
    this.scrolling = scrolling;
  }


  override public function isScrollable():Boolean {
    return true;
  }
}
}