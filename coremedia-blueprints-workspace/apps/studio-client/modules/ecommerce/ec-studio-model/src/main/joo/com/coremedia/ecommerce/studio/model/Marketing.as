package com.coremedia.ecommerce.studio.model {

public interface Marketing extends CatalogObject{

  /**
   * Returns a list of available marketing spots for this store.
   * @return
   */
  function getMarketingSpots():Array;

  /**
   * Return a mapping of the name of marketing spots to themselves
   *
   * @see CatalogObjectPropertyNames#CHILDREN_BY_NAME
   */
  function getChildrenByName():Object;
}
}