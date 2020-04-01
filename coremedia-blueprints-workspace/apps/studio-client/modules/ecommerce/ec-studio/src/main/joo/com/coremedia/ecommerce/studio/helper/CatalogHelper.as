package com.coremedia.ecommerce.studio.helper {

import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ecommerce.studio.CatalogModel;
import com.coremedia.ecommerce.studio.catalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Marketing;
import com.coremedia.ecommerce.studio.model.MarketingSpot;
import com.coremedia.ecommerce.studio.model.Product;
import com.coremedia.ecommerce.studio.model.ProductAttribute;
import com.coremedia.ecommerce.studio.model.ProductVariant;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.error.NotExistsError;
import com.coremedia.ui.data.error.RemoteError;
import com.coremedia.ui.data.impl.RemoteErrorHandlerRegistryImpl;
import com.coremedia.ui.logging.Logger;

import ext.StringUtil;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class CatalogHelper {

  private static const PROFILE_EXTENSIONS:String = 'profileExtensions';
  private static const PROPERTIES:String = 'properties';
  private static const LOCAL_SETTINGS_STRUCT_NAME:String = 'localSettings';
  private static const COMMERCE_STRUCT_NAME:String = 'commerce';
  public static const REFERENCES_LIST_NAME:String = 'references';
  public static const ORIGIN_REFERENCES_LIST_NAME:String = 'originReferences';

  private static const CATEGORY_TOKEN:String = "/category/";
  private static const PRODUCT_TOKEN:String = "/product/";
  private static const SKU_TOKEN:String = "/sku/";
  private static const CATALOG_REGEX:RegExp = /(?:catalog:(\w+);(.*))/;

  public static const NO_WS:String = CatalogModel.NO_WS;

  private static const TYPE_CATEGORY:String = CatalogModel.TYPE_CATEGORY;
  private static const TYPE_MARKETING:String = CatalogModel.TYPE_MARKETING;
  public static const TYPE_PRODUCT:String = CatalogModel.TYPE_PRODUCT;
  public static const TYPE_PRODUCT_VARIANT:String = CatalogModel.TYPE_PRODUCT_VARIANT;
  public static const TYPE_MARKETING_SPOT:String = CatalogModel.TYPE_MARKETING_SPOT;

  public static const CONTENT_TYPE_CM_CATEGORY:String = "CMCategory";
  public static const CONTENT_TYPE_CM_PRODUCT:String = "CMProduct";
  public static const CONTENT_TYPE_CM_ABSTRACT_CATEGORY:String = "CMAbstractCategory";
  public static const CONTENT_TYPE_CM_EXTERNAL_CHANNEL:String = "CMExternalChannel";
  public static const CONTENT_TYPE_CM_EXTERNAL_PRODUCT:String = "CMExternalProduct";

  private static const PREFERENCES_COMMERCE_STRUCT:String = 'commerce';
  public static const COMMERCE_STRUCT_WORKSPACE:String = 'workspace';

  // lc rest error, see CatalogRestErrorCodes.java
  public static const LC_ERROR_CODE_CATALOG_ITEM_UNAVAILABLE = "LC-01000";
  public static const LC_ERROR_CODE_CONNECTION_UNAVAILABLE = "LC-01001";
  public static const LC_ERROR_CODE_CATALOG_INTERNAL_ERROR = "LC-01002";
  public static const LC_ERROR_CODE_UNAUTHORIZED = "LC-01003";
  public static const LC_ERROR_CODE_CATALOG_UNAVAILABLE = "LC-01004";
  public static const COULD_NOT_FIND_STORE_BEAN = "LC-01006";

  private var storeExpression:ValueExpression;

  {
    RemoteErrorHandlerRegistryImpl
            .initRemoteErrorHandlerRegistry()
            .registerErrorHandler(remoteErrorHandler);
  }

  public static function getInstance():CatalogHelper {
    return catalogHelper;
  }

  public function openCatalog():void {
    var store:Store = Store(getInstance().getActiveStoreExpression().getValue());
    if (store) {
      store.load(function ():void {
        store.getRootCategory().load(function ():void {
          var selectedNode:* = getCollectionViewModel().getMainStateBean().get(CollectionViewModel.FOLDER_PROPERTY);
          //if already a category is selected we don't have to change anything.
          if (!(selectedNode is Category)) {
            selectedNode = store.getRootCategory();
          }
          var model:CollectionViewModel = CollectionViewManagerInternal(editorContext.getCollectionViewManager()).getCollectionView().getCollectionViewModel();
          model.setMode(CollectionViewModel.REPOSITORY_MODE);

          CollectionViewManagerInternal(editorContext.getCollectionViewManager()).openWithAllState();
          CollectionViewManagerInternal(editorContext.getCollectionViewManager()).getCollectionView().showInRepositoryMode(selectedNode);
        });
      });
    }
  }

  public function openMarketingSpots():void {
    var store:Store = Store(getInstance().getActiveStoreExpression().getValue());
    if (store) {
      store.load(function ():void {
        store.getMarketing().load(function ():void {
          var model:CollectionViewModel = CollectionViewManagerInternal(editorContext.getCollectionViewManager()).getCollectionView().getCollectionViewModel();
          model.setMode(CollectionViewModel.REPOSITORY_MODE);

          var selectedNode:Object = store.getMarketing();
          CollectionViewManagerInternal(editorContext.getCollectionViewManager()).openWithAllState();
          CollectionViewManagerInternal(editorContext.getCollectionViewManager()).getCollectionView().showInRepositoryMode(selectedNode);
        });
      });
    }
  }

  public function getImageUrl(catalogObject:CatalogObject):String {
    if (catalogObject is Product) {
      return Product(catalogObject).getThumbnailUrl();
    } else if (catalogObject is Category) {
      return Category(catalogObject).getThumbnailUrl();
    }

    return null;
  }

  public function getType(catalogObject:CatalogObject):String {
    var beanType:String;
    if (catalogObject is Category) {
      beanType = TYPE_CATEGORY;
    } else if (catalogObject is ProductVariant) {
      beanType = TYPE_PRODUCT_VARIANT;
    } else if (catalogObject is Product) {
      beanType = TYPE_PRODUCT;
    } else if (catalogObject is MarketingSpot) {
      beanType = TYPE_MARKETING_SPOT;
    } else if (catalogObject is Marketing) {
      beanType = TYPE_MARKETING;
    } else {
      beanType = "UnknownType";
    }

    return beanType;
  }

  public function getExternalIdFromId(id:String):String {
    //External ids of category can contain '/'. See CMS-5075
    var token:String = getToken(id);
    var candidate:String;
    if (token) {
      candidate = id.substr(id.lastIndexOf(token) + token.length);
    } else {
      //we assume that the substring after the last '/' is the external id
      candidate = id.substr(id.lastIndexOf('/') + 1);
    }
    return stripCatalogFromExternalId(candidate);
  }

  static function stripCatalogFromExternalId(candidate:String):String {
    // the candidate may include the catalog alias
    var matches:Array = CATALOG_REGEX.exec(candidate);
    return matches && matches.length === 3 ? matches[2] : candidate;
  }

  private function encodeForUri(externalId:String):String {
    // First all chars in externalId are encoded.
    // After that, translate back encoded slashes ("%2F") to "/" because by default the tomcat container
    // do not allow encoded slashes for security reasons (see org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH).
    return encodeURIComponent(externalId).replace(/%2F/g, "/")
  }

  public function getCatalogAliasFromId(id:String):String {
    var groups:Array = CATALOG_REGEX.exec(id);
    if (groups && groups.length > 1) {
      return groups[1];
    }
    // the default catalog alias is 'catalog'
    return 'catalog';
  }

  public function getToken(id:String):String {
    var token:String;
    if (isCategoryId(id)) {
      return CATEGORY_TOKEN;
    } else if (isProductId(id)) {
      return PRODUCT_TOKEN;
    } else if (isSkuId(id)) {
      return SKU_TOKEN;
    }
  }
  public function isSubType(catalogObject:CatalogObject, catalogObjectType:String):Boolean {
    if (catalogObject is Category) {
      return catalogObjectType === TYPE_CATEGORY;
    } else if (catalogObject is ProductVariant) {
      return catalogObjectType === TYPE_PRODUCT || catalogObjectType === TYPE_PRODUCT_VARIANT;
    } else if (catalogObject is Product) {
      return catalogObjectType === TYPE_PRODUCT;
    } else if (catalogObject is MarketingSpot) {
      return catalogObjectType === TYPE_MARKETING_SPOT;
    } else {
      return false;
    }
  }

  /**
   * Get the catalog object for the given catalog object id.
   * If the content is specified the store of the content will be used
   * Otherwise the active store will be used.
   * @param catalogObjectId
   * @param contentExpression
   */
  public function getCatalogObject(catalogObjectId:String, contentExpression:ValueExpression = undefined):CatalogObject {

    var storeValue:Object = contentExpression ?
            getStoreForContentExpression(contentExpression).getValue() :
            getActiveStoreExpression().getValue();
    if (storeValue === undefined) {
      return undefined;
    }
    if (!storeValue) {
      return null;
    }
    var store:Store = storeValue as Store;

    var siteId:String = store.getSiteId();
    if (siteId === undefined) {
      return undefined;
    }
    if (!siteId) {
      return null;
    }

    var workspaceId:String = getExtractedWorkspaceId();
    if (!workspaceId) {
      workspaceId = NO_WS;//No workspace selected
    }

    //siteId and externalId are free text properties and therefor
    //must be uri encoded. see #encodeForUri for more details.
    var encodedSiteId:String = encodeForUri(siteId);
    var endocedExternalId = encodeForUri(getExternalIdFromId(catalogObjectId));
    var catalogAlias:String = getCatalogAliasFromId(catalogObjectId);
    var uriPath:String;
    if (isCategoryId(catalogObjectId)) {
      uriPath = "livecontext/category/" + encodedSiteId + "/" + catalogAlias + "/" + workspaceId + "/" + endocedExternalId;
    } else if (isSkuId(catalogObjectId)) {
      uriPath = "livecontext/sku/" + encodedSiteId + "/" + catalogAlias + "/" + workspaceId + "/" + endocedExternalId;
    } else if (isProductId(catalogObjectId)) {
      uriPath = "livecontext/product/" + encodedSiteId + "/" + catalogAlias + "/" + workspaceId + "/" + endocedExternalId;
    } else if (isSegmentId(catalogObjectId)) {
      uriPath = "livecontext/segment/" + encodedSiteId + "/" + workspaceId + "/" + endocedExternalId;
    } else if (isContractId(catalogObjectId)) {
      uriPath = "livecontext/contract/" + encodedSiteId + "/" + workspaceId + "/" + endocedExternalId;
    } else if (isMarketingSpot(catalogObjectId)) {
      uriPath = "livecontext/marketingspot/" + encodedSiteId + "/" + workspaceId + "/" + endocedExternalId;
    } else if (isMarketing(catalogObjectId)) {
      uriPath = "livecontext/marketing/" + encodedSiteId + "/" + workspaceId + "/";
    } else if (isFacets(catalogObjectId)) {
      uriPath = "livecontext/facets/" + encodedSiteId + "/" + catalogAlias + "/" + workspaceId + "/" + endocedExternalId;
    }

    if(uriPath) {
      return CatalogObject(beanFactory.getRemoteBean(uriPath));
    }
  }

  /**
   *
   * @param bindTo value expression pointing to a document of which 'externalId' property as the id of a catalog object
   */
  public function getCatalogExpression(bindTo:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():* {
      var catalogObjectId:String = bindTo.extendBy('properties').extendBy('externalId').getValue();
      if (catalogObjectId === undefined) {
        return undefined;
      }
      if (!catalogObjectId || catalogObjectId.length === 0) {
        return null;
      } else {
        return getCatalogObject(catalogObjectId, bindTo);
      }
    });
  }

  /**
   *
   * @param bindTo value expression pointing to a document of which 'externalId' property as the product id
   * @param productPropertyName
   */
  public function getProductPropertyExpression(bindTo:ValueExpression, productPropertyName:String):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():* {
      var product:Product;
      var catalogObjectId:String = bindTo.extendBy('properties').extendBy('externalId').getValue();
      if (!catalogObjectId || catalogObjectId.length === 0) {
        return null;
      } else {
        product = getCatalogObject(catalogObjectId, bindTo) as Product;
        if (product) {
          return product.get(productPropertyName);
        } else {
          return null;
        }
      }
    });
  }

  public function isStoreId(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf('livecontext/store') >= 0;
  }

  private function isSkuId(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf(SKU_TOKEN) !== -1;
  }

  private function isProductId(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf(PRODUCT_TOKEN) !== -1;
  }

  private function isCategoryId(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf(CATEGORY_TOKEN) !== -1;
  }

  private function isSegmentId(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf("//catalog/segment/") !== -1;
  }

  private function isContractId(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf("//catalog/contract/") !== -1;
  }

  public static function isMarketingSpot(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf("/marketingspot/") !== -1;
  }

  public static function isMarketing(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf("/marketing/") !== -1;
  }

  public static function isFacets(catalogObjectId:String):Boolean {
    return catalogObjectId.indexOf("/facets/") !== -1;
  }

  public function getActiveStoreExpression():ValueExpression {
    if (storeExpression) {
      return storeExpression;
    }
    storeExpression = ValueExpressionFactory.createFromFunction(StoreUtil.getActiveStore);
    return storeExpression;
  }

  public function isActiveCoreMediaStore():Boolean {
    var store:Store = getActiveStoreExpression().getValue();
    return isCoreMediaStore(store);
  }

  public function belongsToCoreMediaStore(items:Array):Boolean {
    if(items.length === 0) {
      return undefined;
    }

    var store:Store = items[0].getStore();
    return isCoreMediaStore(store);
  }

  public function isCoreMediaStore(store:Store):Boolean {
    return isVendor(store, "coremedia");
  }

  public function isVendor(store:Store, vendorName:String):Boolean {
    return vendor(store, vendorName, true);
  }

  public function isNotVendor(store:Store, vendorName:String):Boolean {
    return vendor(store, vendorName, false);
  }

  private function vendor(store:Store, vendorName:String, isBelongsTo:Boolean): Boolean {
    if (store === undefined) {
      return undefined;
    }
    if (!store) {
      return false;
    }
    return store.getVendorName() && isBelongsTo === (store.getVendorName().toLowerCase() === vendorName.toLowerCase());
  }

  public function getExtractedWorkspaceId():String {
    var workspaceFullId:String = getCommerceWorkspaceExpression().getValue();
    if (!workspaceFullId) {
      return NO_WS;//No workspace selected;
    }
    return getExternalIdFromId(workspaceFullId);
  }

  public function getCommerceWorkspaceExpression():ValueExpression {
    return ValueExpressionFactory.create(null, editorContext.getPreferences()).
            extendBy(PREFERENCES_COMMERCE_STRUCT).extendBy(COMMERCE_STRUCT_WORKSPACE);
  }

  public function getStoreForContentExpression(contentExpression:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Store {
      var content:Content = contentExpression.getValue();
      if (content === undefined) {
        return undefined;
      }
      var siteId:String = editorContext.getSitesService().getSiteIdFor(content);
      return StoreUtil.getValidatedStore(siteId);
    });
  }

  public function getStoreForContent(content:Content, callback:Function):void {
    if (!content) {
      callback.call(null, undefined);
    }
    getStoreForContentExpression(ValueExpressionFactory.createFromValue(content)).loadValue(
            function (store:Store):void {
              callback.call(null, store);
            }
    );
  }

  private static function remoteErrorHandler(error:RemoteError, source:Object):void {
    var catalogObject:CatalogObject = source as CatalogObject;
    if (catalogObject) {
      var errorCode:String = error.errorCode;
      var errorMsg:String = error.message;
      // only process livecontext errors
      if (errorCode === LC_ERROR_CODE_CATALOG_ITEM_UNAVAILABLE) {
        doHandleError(error, source);
      } else if (errorCode === LC_ERROR_CODE_CONNECTION_UNAVAILABLE) {
        MessageBoxUtil.showError(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceConnectionError_title'),
                StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceConnectionError_message'), errorMsg));
        doHandleError(error, source);
      } else if (errorCode === LC_ERROR_CODE_CATALOG_INTERNAL_ERROR) {
        MessageBoxUtil.showError(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceCatalogError_title'),
                StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceCatalogError_message'), errorMsg));
        doHandleError(error, source);
      } else if (errorCode === LC_ERROR_CODE_UNAUTHORIZED) {
        MessageBoxUtil.showError(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceUnauthorizedError_title'),
                StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceUnauthorizedError_message'), errorMsg));
        doHandleError(error, source);
      } else if (errorCode === LC_ERROR_CODE_CATALOG_UNAVAILABLE) {
        MessageBoxUtil.showError(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceCatalogNotFoundError_title'),
                StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceCatalogNotFoundError_message'), errorMsg));
        doHandleError(error, source);
      } else if (errorCode === COULD_NOT_FIND_STORE_BEAN) {
        trace("[WARN]", StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'commerceStoreItemNotFoundError_message'), errorMsg));
        doHandleError(error, source);
      }
    }
  }

  private static function doHandleError(error:RemoteError, source:Object):void {
    // do not call error.setHandled(true) to allow the RemoteBeanImpl to clean up
    // if we would do the library freezes
    trace('[DEBUG]', 'Handled commerce error ' + error + ' raised by ' + source);
  }

  public function getChildren(catalogObject:CatalogObject):Array {
    if (!catalogObject) {
      return [];
    }
    else if (catalogObject is Marketing) {
      return Marketing(catalogObject).getMarketingSpots();
    }
    else if (catalogObject is Store) {
      return [];
    }
    else if (catalogObject is Category) {
      return Category(catalogObject).getChildren();
    }
    else {
      return [];
    }
  }

  /**
   *
   * @param productVariant
   * @return the defining attributes as comma-separated list. E.g. (Red, XL)
   */
  private function getDefiningAttributesString(productVariant:ProductVariant):String {
    var attributesStr:String = undefined;
    var definingAttributes:Array = productVariant.getDefiningAttributes();
    if (definingAttributes) {
      for each (var attribute:ProductAttribute in  definingAttributes) {
        if (!attributesStr) {
          attributesStr = '(';
        } else {
          attributesStr += ', ';
        }
        attributesStr +=attribute.value;
      }
      if (attributesStr) {
        attributesStr += ')';
      }
    }
    return attributesStr;
  }

  /**
   *
   * @param catalogObject
   * @return the name and for variants the defining attributes as comma-separated list. E.g. (Red, XL)
   */
  public function getDecoratedName(catalogObject:CatalogObject):String {
    var name:String = catalogObject.getName();
    if (catalogObject is ProductVariant) {
      var attributes:String = getDefiningAttributesString(catalogObject as ProductVariant);
      if (attributes) {
        name += ' ' + attributes;
      }
    }
    return name;
  }

  /**
   *
   * @param catalogObject
   * @return the name and for variants the defining attributes as comma-separated list. E.g. (Red, XL)
   */
  public function getDisplayName(catalogObject:CatalogObject):String {
    var name:String = catalogObject is Category ? (catalogObject as Category).getDisplayName() : getDecoratedName(catalogObject);
    return name;
  }

  public function getPriceWithCurrencyExpression(bindTo:ValueExpression, priceProperty:String):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function():String {
      var priceWithCurrency:String = undefined;
      var product:Product = bindTo.getValue() as Product;
      if (product && product.get(priceProperty)) {
          priceWithCurrency = product.get(priceProperty) + ' ' + product.getCurrency();
      }
      return priceWithCurrency;
    });

  }

  public function getIsVariantExpression(bindTo:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function():Boolean {
      return bindTo.getValue() is ProductVariant;
    });
  }

  public function getIsNotVariantExpression(bindTo:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function():Boolean {
      return !(bindTo.getValue() is ProductVariant);
    });
  }

  public function createOrUpdateProductListStructs(bindTo:ValueExpression, product:Product = undefined):void {
    var localSettingsStructExpression:ValueExpression = bindTo.extendBy(PROPERTIES, LOCAL_SETTINGS_STRUCT_NAME);
    localSettingsStructExpression.loadValue(function():void {
      var localSettingsStruct:Struct = localSettingsStructExpression.getValue();
      RemoteBean(localSettingsStruct).load(function():void {
        if (!localSettingsStruct.get(COMMERCE_STRUCT_NAME)) {
          localSettingsStruct.getType().addStructProperty(COMMERCE_STRUCT_NAME);
        }
        var commerceStruct:Struct = localSettingsStruct.get(COMMERCE_STRUCT_NAME);
        if (!commerceStruct.get(REFERENCES_LIST_NAME)) {
          commerceStruct.getType().addStringListProperty(REFERENCES_LIST_NAME, 1000000);
        }
        //avoid duplicates
        if (product && (commerceStruct.get(REFERENCES_LIST_NAME) as Array).indexOf(product.getId()) < 0) {
          commerceStruct.addAt(REFERENCES_LIST_NAME, -1, product.getId());
        }
      });
    });
  }

  public static function getCatalogObjectsExpression(contentExpression:ValueExpression,
                                                     catalogObjectIdListName:String,
                                                     invalidMessage:String,
                                                     catalogObjectIdsExpression:ValueExpression = undefined):ValueExpression {

    var idsExpression:ValueExpression = catalogObjectIdsExpression || getCatalogObjectIdsExpression(contentExpression, catalogObjectIdListName);
    return ValueExpressionFactory.createFromFunction(function ():Array {
      var catalogObjects:Array = idsExpression.getValue();
      if (!catalogObjects) {
        return undefined;
      }
      catalogObjects = catalogObjects.map(function (id:String):Bean {
                var externalId:String = CatalogHelper.getInstance().getExternalIdFromId(id);
                try {
                  var catalogObject:CatalogObject = CatalogHelper.getInstance().getCatalogObject(id, contentExpression) as CatalogObject;
                  if (catalogObject && catalogObject.getName()) {
                    return catalogObject;
                  } else {
                    // no catalog object or name found : probably wrong catalog object id
                    //use local bean to display the id instead
                    return beanFactory.createLocalBean({
                      id: id,
                      externalId: externalId,
                      name: StringUtil.format(invalidMessage, externalId)
                    });
                  }
                } catch (e:NotExistsError) {
                  // if remote bean could not be loaded (404) local bean shall be displayed
                  return beanFactory.createLocalBean({
                    id: id,
                    externalId: externalId,
                    name: StringUtil.format(invalidMessage, externalId)
                  });
                }
              }
      );

      //todo: the catalog objects may be null (why?)
      catalogObjects = catalogObjects.filter(function (obj:*):Boolean {
        return obj;
      });
      return  catalogObjects;
    })
  }

  public static function addCatalogObject(contentExpression:ValueExpression, catalogObjectIdListName:String,
                                          catlogObjectId:String, catalogObjectIdsExpression:ValueExpression = undefined):void {
    var idsExpression:ValueExpression = catalogObjectIdsExpression || getCatalogObjectIdsExpression(contentExpression, catalogObjectIdListName);
    if (idsExpression.isLoaded()) {
      doAddCatalogObject(contentExpression, catalogObjectIdListName, catlogObjectId, catalogObjectIdsExpression);
    } else {
      idsExpression.loadValue(function ():void {
        doAddCatalogObject(contentExpression, catalogObjectIdListName, catlogObjectId, catalogObjectIdsExpression);
      });
    }
  }

  private static function doAddCatalogObject(contentExpression:ValueExpression, catalogObjectIdListName:String,
                                             catlogObjectId:String, catalogObjectIdsExpression:ValueExpression = undefined):void {
    var idsExpression:ValueExpression = catalogObjectIdsExpression || getCatalogObjectIdsExpression(contentExpression, catalogObjectIdListName);
    var catalogObjectIds:Array = idsExpression.getValue();
    if (!catalogObjectIds) {
      createStructsIfNecessary(contentExpression, catalogObjectIdListName, catalogObjectIdsExpression);
      addCatalogObject(contentExpression, catalogObjectIdListName, catlogObjectId, catalogObjectIdsExpression);
    } else {
      if (catalogObjectIds.indexOf(catlogObjectId) >= 0 ) return;
      idsExpression.setValue(catalogObjectIds.concat([catlogObjectId]));
    }
  }

  private static function createStructsIfNecessary(contentExpression:ValueExpression,
                                                   catalogObjectIdListName:String,
                                                   catalogObjectIdsExpression:ValueExpression = undefined):void {
    if (catalogObjectIdsExpression) return;
    var structProperty:Struct = contentExpression.extendBy(PROPERTIES, PROFILE_EXTENSIONS).getValue();

    var propertiesStruct:Struct = structProperty.get(PROPERTIES);
    if (!propertiesStruct) {
      structProperty.getType().addStructProperty(PROPERTIES);
      propertiesStruct = structProperty.get(PROPERTIES);
    }

    var commerceStruct:Struct = propertiesStruct.get(COMMERCE_STRUCT_NAME);
    if (!commerceStruct) {
      propertiesStruct.getType().addStructProperty(COMMERCE_STRUCT_NAME);
      commerceStruct = propertiesStruct.get(COMMERCE_STRUCT_NAME);
    }

    if (!commerceStruct.get(catalogObjectIdListName)) {
      commerceStruct.getType().addStringListProperty(catalogObjectIdListName, 1000000, []);
    }
  }

  public static function removeCatalogObject(contentExpression:ValueExpression,
                                             catalogObjectIdListName:String, catlogObjectId:String,
                                             catalogObjectIdsExpression:ValueExpression = undefined):void {
    var idsExpression:ValueExpression = catalogObjectIdsExpression || getCatalogObjectIdsExpression(contentExpression, catalogObjectIdListName);
    idsExpression.loadValue(function(catalogObjectIds:Array):void {
      if (catalogObjectIds) {
        if (catalogObjectIds.indexOf(catlogObjectId) < 0 ) return;

        var newCatalogObjects:Array = catalogObjectIds.filter(function(oldCatalogObjectId:String):Boolean {
          return oldCatalogObjectId !== catlogObjectId;
        });

        idsExpression.setValue(newCatalogObjects);
      }
    })
  }

  private static function getCatalogObjectIdsExpression(contentExpression:ValueExpression, catalogObjectIdListName:String):ValueExpression {
    return contentExpression.extendBy(PROPERTIES, PROFILE_EXTENSIONS, PROPERTIES, COMMERCE_STRUCT_NAME, catalogObjectIdListName);
  }

  private static function getCollectionViewModel():CollectionViewModel {
    return EditorContextImpl(editorContext).getCollectionViewModel();
  }

}
}
