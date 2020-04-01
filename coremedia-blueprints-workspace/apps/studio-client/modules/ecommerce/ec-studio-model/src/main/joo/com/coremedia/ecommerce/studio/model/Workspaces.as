package com.coremedia.ecommerce.studio.model {

public interface Workspaces extends CatalogObject{

  /**
   * Returns a list of available workspaces for this store.
   * @return
   */
  function getWorkspaces():Array;

}
}