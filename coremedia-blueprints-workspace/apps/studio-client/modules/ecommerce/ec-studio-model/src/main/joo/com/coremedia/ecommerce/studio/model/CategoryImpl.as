package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/category/{siteId:[^/]+}/{catalogAlias:[^/]+}/{workspaceId:[^/]+}/{externalId:.+}")]
public class CategoryImpl extends CatalogObjectImpl implements Category {
  public function CategoryImpl(uri:String,vars:Object) {
    super(uri);
  }


  public function getChildrenByName():Object {
    return get(CatalogObjectPropertyNames.CHILDREN_BY_NAME);
  }


  public function getChildren():Array {
    return get(CatalogObjectPropertyNames.CHILDREN);
  }

  public function getSubCategories():Array {
    return get(CatalogObjectPropertyNames.SUB_CATEGORIES);
  }

  public function getThumbnailUrl():String {
    return get(CatalogObjectPropertyNames.THUMBNAIL_URL);
  }

  public function getPreviewUrl():String {
    return get(CatalogObjectPropertyNames.PREVIEW_URL);
  }

  public function getParent():Category {
    return get(CatalogObjectPropertyNames.PARENT);
  }

  public function getCatalog():Catalog {
    return get(CatalogObjectPropertyNames.CATALOG);
  }

  public function getDisplayName():String {
    return get(CatalogObjectPropertyNames.DISPLAY_NAME);
  }

  public function getProducts():Array {
    return get(CatalogObjectPropertyNames.PRODUCTS);
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

  public function getFacets():Facets {
    return get(CatalogObjectPropertyNames.FACETS);
  }

}
}