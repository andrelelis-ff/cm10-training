package com.coremedia.ecommerce.studio.augmentation {
import com.coremedia.cap.content.Content;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Product;

/**
 * Service to deal with augmented catalog objects. Augmented catalog objects
 * have a content proxy object holding its external id.
 */
public interface IAugmentationService {

  /**
   * Return the content object holding the catalog object's external id (if any)
   */
  function getContent(catalogObject:CatalogObject):Content;

  /**
   * Lookup the catalog object with the content's external id
   */
  function getCatalogObject(content:Content):CatalogObject;

  /**
   * Lookup the catalog object with the content's external id if it is a category
   */
  function getCategory(content:Content):Category;

  /**
   * Lookup the catalog object with the content's external id if it is a product
   */
  function getProduct(content:Content):Product;
}
}