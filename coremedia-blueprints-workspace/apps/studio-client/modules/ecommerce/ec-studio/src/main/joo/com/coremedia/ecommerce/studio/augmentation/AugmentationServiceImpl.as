package com.coremedia.ecommerce.studio.augmentation {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentProperties;
import com.coremedia.cap.content.ContentProxyHelper;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.CatalogObjectPropertyNames;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Product;
import com.coremedia.ui.data.ValueExpressionFactory;

internal class AugmentationServiceImpl implements IAugmentationService {

  public function getContent(catalogObject:CatalogObject):Content {
    var content:Content = ContentProxyHelper.getContent(catalogObject);
    //load the content already so that Studio knows more about the augmenting content.
    if (content) {
      content.load();
    }
    return content;
  }

  public function getCatalogObject(content:Content):CatalogObject {
    if (!content) {
      return null;
    }
    var properties:ContentProperties = content.getProperties();
    if (properties === undefined) {
      return undefined;
    }
    var externalId:String = properties.get(CatalogObjectPropertyNames.EXTERNAL_ID);
    if (externalId === undefined) {
      return undefined;
    }
    // if external id is either null or an empty string we don't have a corresponding catalog object
    if (!externalId) {
      return null;
    }
    var catalogObject:CatalogObject = CatalogHelper.getInstance().getCatalogObject(externalId, ValueExpressionFactory.createFromValue(content));
    if (catalogObject === undefined) {
      return undefined;
    }

    return catalogObject;
  }

  public function getCategory(content:Content):Category {
    var catalogObject:CatalogObject = getCatalogObject(content);
    if (catalogObject === undefined) {
      return undefined;
    }
    return catalogObject as Category;
  }

  public function getProduct(content:Content):Product {
    var catalogObject:CatalogObject = getCatalogObject(content);
    if (catalogObject === undefined) {
      return undefined;
    }
    return catalogObject as Product;
  }
}
}
