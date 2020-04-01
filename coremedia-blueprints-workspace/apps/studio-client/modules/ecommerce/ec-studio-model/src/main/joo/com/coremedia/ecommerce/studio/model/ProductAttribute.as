package com.coremedia.ecommerce.studio.model {
/**
 * This interface is used to access the describing and defining attributes in a typed manner
 */
public interface ProductAttribute {

  function get name():String;

  function get displayName():String;

  function get type():String;

  function get unit():String;

  function get description():String;

  function get externalId():String;

  function get value():Object;

  function get values():Array;

  function get defining():Boolean;

}
}