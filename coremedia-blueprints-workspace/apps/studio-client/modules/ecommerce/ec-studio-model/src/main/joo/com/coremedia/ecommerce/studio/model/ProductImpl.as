package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/product/{siteId:[^/]+}/{catalogAlias:[^/]+}/{workspaceId:[^/]+}/{externalId:.+}")]
public class ProductImpl extends CatalogObjectImpl implements Product {
  public function ProductImpl(uri:String) {
    super(uri);
  }

  public function getCategory():Category {
    return get(CatalogObjectPropertyNames.CATEGORY);
  }

  public function getCatalog():Catalog{
    return get(CatalogObjectPropertyNames.CATALOG);
  }

  public function getThumbnailUrl():String {
    return get(CatalogObjectPropertyNames.THUMBNAIL_URL);

  }

  public function getPreviewUrl():String {
    return get(CatalogObjectPropertyNames.PREVIEW_URL);
  }

  public function getOfferPrice():Number {
    return get(ProductPropertyNames.OFFER_PRICE);
  }

  public function getListPrice():Number {
    return get(ProductPropertyNames.LIST_PRICE);
  }

  public function getCurrency():String {
    return get(ProductPropertyNames.CURRENCY);
  }

  public function getVariants():Array {
    return get(ProductPropertyNames.VARIANTS);
  }

  public function getVisuals():Array {
    return get(CatalogObjectPropertyNames.VISUALS);
  }

  public function getPictures():Array {
    return get(CatalogObjectPropertyNames.PICTURES);
  }

  public function getDownloads():Array {
    return get(CatalogObjectPropertyNames.DOWNLOADS);
  }

  public function getLongDescription():String {
    return get(CatalogObjectPropertyNames.LONG_DESCRIPTION);
  }

  public function getDescribingAttributes():Array {
    return get(ProductPropertyNames.DESCRIBING_ATTRIBUTES);
  }

  override public function invalidate(callback:Function = null):void {
    if (!hasAnyListener()) {
      super.invalidate();
      return;
    }

    var thiz:* = this;
    super.invalidate(function():void {
      callback && callback(thiz);
      //all product variants need to be invalidated as well
      var variants:Array = getVariants() || [];
      for each (var variant:ProductVariant in variants) {
        variant.invalidate();
      }
    });
  }
}
}