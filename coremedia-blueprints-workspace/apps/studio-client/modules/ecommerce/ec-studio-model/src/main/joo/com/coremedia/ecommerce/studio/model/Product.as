package com.coremedia.ecommerce.studio.model {
import com.coremedia.ui.data.Previewable;

public interface Product extends CatalogObject, Previewable{
  /**
   * Gets the current category. If it is possible to have multiple category associated to a product the first (or leading)
   * category should be returned.
   * @return the current category
   * @see CatalogObjectPropertyNames#CATEGORY
   */
  function getCategory():Category;

  /**
   * Returns the catalog of this product
   *
   * @return the catalog
   *
   * @see CatalogObjectPropertyNames#CATALOG
   */
  function getCatalog():Catalog;

  function getThumbnailUrl():String;

  function getLongDescription():String;

  function getOfferPrice():Number;

  function getListPrice():Number;

  function getCurrency():String;

  [ArrayElementType("com.coremedia.ecommerce.studio.model.ProductAttribute")]
  function getDescribingAttributes():Array;

  [ArrayElementType("com.coremedia.ecommerce.studio.model.ProductVariant")]
  function getVariants():Array;

  function getVisuals():Array;

  function getPictures():Array;

  function getDownloads():Array;

}
}