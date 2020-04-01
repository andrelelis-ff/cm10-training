package com.coremedia.ecommerce.studio.model {

public interface Segments extends CatalogObject{

  /**
   * Returns a list of available segments for this store.
   * @return
   */
  function getSegments():Array;

}
}