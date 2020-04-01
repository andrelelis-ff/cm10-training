package com.coremedia.ecommerce.studio.model {

public interface MarketingSpot extends CatalogObject{

  /**
   * Returns the parent marketing bean.
   * @return
   */
  function getMarketing():Marketing;
}
}