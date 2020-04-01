package com.coremedia.ecommerce.studio.model {
import com.coremedia.ui.data.Previewable;

[Event(name="children", type="com.coremedia.ui.data.PropertyChangeEvent")]
[Event(name="childrenByName", type="com.coremedia.ui.data.PropertyChangeEvent")]


public interface Category extends CatalogObject, Previewable {

  /**
   * Return a list of child categories and products
   *
   * @see CatalogObjectPropertyNames#CHILDREN
   */
  function getChildren():Array/* Vector.<CatalogObject> */;

  /**
   * Return a mapping of the name of child categories/products to the child catalog objects themselves.
   *
   * @see CatalogObjectPropertyNames#CHILDREN_BY_NAME
   */
  function getChildrenByName():Object;

  /**
   * Return list of child categories, sorted by name (case insensitive)
   *
   * @see CatalogObjectPropertyNames#SUB_CATEGORIES
   */
  function getSubCategories():Array/* Vector.<Category> */;

  /**
   * Return list of direct child Products, sorted by name (case insensitive)
   *
   * @see CatalogObjectPropertyNames#SUB_CATEGORIES
   */
  function getProducts():Array/* Vector.<Product> */;


  function getThumbnailUrl():String;

  /**
   * Returns the parent category
   * Returns null, if this is the top category
   *
   * @return the parent category
   *
   * @see CatalogObjectPropertyNames#PARENT
   */
  function getParent():Category;

  /**
   * Returns the catalog of this category
   *
   * @return the catalog
   *
   * @see CatalogObjectPropertyNames#CATALOG
   */
  function getCatalog():Catalog;

  /**
   * @return The display name for a category
   * @see CatalogObjectPropertyNames#DISPLAY_NAME
   */
  function getDisplayName():String;

  function getVisuals():Array;

  function getPictures():Array;

  function getDownloads():Array;

  function getFacets():Facets;

}
}