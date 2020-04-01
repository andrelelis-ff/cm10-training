package com.coremedia.blueprint.studio.taxonomy.rendering {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;

/**
 * Factory class for creating new renderers for the different taxonomy renderings.
 */
public class TaxonomyRenderFactory {

  public static function createSearchComboRenderer(nodes:Array, componentId:String):TaxonomyRenderer {
    return new TaxonomySearchComboRenderer(nodes, componentId);
  }

  public static function createLinkListRenderer(nodes:Array, componentId:String):TaxonomyRenderer {
    return new TaxonomyLinkListRenderer(nodes, componentId);
  }

  public static function createSelectedListRenderer(nodes:Array, componentId:String, scrolling:Boolean):TaxonomyRenderer {
    return new SelectedListRenderer(nodes, componentId, scrolling);
  }

  public static function createSelectedListWithoutPathRenderer(nodes:Array, componentId:String, scrolling:Boolean):TaxonomyRenderer {
    return new SelectedListWithoutPathRenderer(nodes, componentId, scrolling);
  }

  public static function createSuggestionsRenderer(nodes:Array, componentId:String, weight:String):TaxonomyRenderer {
    return new SuggestionsRenderer(nodes, componentId, weight);
  }

  public static function createSelectionListRenderer(node:TaxonomyNode, componentId:String, selected:Boolean):TaxonomyRenderer {
    return new SelectionListRenderer([node], componentId, selected);
  }

  public static function createSingleSelectionListRenderer(node:TaxonomyNode, componentId:String, selected:Boolean, selectionExists:Boolean):TaxonomyRenderer {
    return new SingleSelectionListRenderer([node], componentId, selected, selectionExists);
  }
}
}