package com.coremedia.ecommerce.studio.model {
import com.coremedia.ui.data.RemoteBean;

[Event(name="childrenByName", type="com.coremedia.ui.data.PropertyChangeEvent")]

[Event(name="children", type="com.coremedia.ui.data.PropertyChangeEvent")]


public interface Store extends CatalogObject {

  function getTopLevel():Array;

  function getMarketing():Marketing;

  function isMarketingEnabled():Boolean;

  function getRootCategory():Category;

  function getSegments():Segments;

  function getContracts():Contracts;

  function getWorkspaces():Workspaces;

  function getCatalogs():Array;

  function isMultiCatalog():Boolean;

  function getDefaultCatalog():Catalog;

  function getCurrentWorkspace():Workspace;

  /**
   * Return a mapping of the name of top level categories to the categories themselves
   *
   * @see CatalogObjectPropertyNames#CHILDREN_BY_NAME
   */
  function getChildrenByName():Object;

  function getStoreId():String;

  function getVendorName():String;

  function getVendorUrl():String;

  function getVendorVersion():String;

  function getTimeZoneId():String;

  function resolveShopUrlForPbe(url:String):RemoteBean;

}
}