package com.coremedia.ecommerce.studio.model {
public interface Catalog extends CatalogObject{

  function getRootCategory():Category;

  function isDefault():Boolean;
}
}