package com.coremedia.ecommerce.studio.model {

public interface Contracts extends CatalogObject{

  /**
   * Returns a list of available contracts for this user.
   * @return
   */
  function getContracts():Array;

}
}