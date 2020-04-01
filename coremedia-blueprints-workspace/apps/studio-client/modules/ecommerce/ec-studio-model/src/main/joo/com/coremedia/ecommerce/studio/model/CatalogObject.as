package com.coremedia.ecommerce.studio.model {
import com.coremedia.ui.data.RemoteBean;

[Event(name="name", type="com.coremedia.ui.data.PropertyChangeEvent")]
[Event(name="id", type="com.coremedia.ui.data.PropertyChangeEvent")]

public interface CatalogObject extends RemoteBean{
  function getName():String;
  function getShortDescription():String;
  function getExternalId():String;
  function getId():String;
  function getExternalTechId():String;
  function getStore():Store;
  function getSiteId():String;
  function getCustomAttributes():Object;

  /**
   * Returns a custom attribute of this CatalogObject.
   *
   * @param attribute name of the attribute
   * @return the custom attribute value or null if the custom attribute does exist
   */
  function getCustomAttribute(attribute:String):Object;
}
}
