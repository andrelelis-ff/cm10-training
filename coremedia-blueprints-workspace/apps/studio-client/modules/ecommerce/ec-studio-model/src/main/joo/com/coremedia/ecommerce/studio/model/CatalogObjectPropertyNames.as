package com.coremedia.ecommerce.studio.model {
public class CatalogObjectPropertyNames {

  /**
   * @eventType name
   * @see CatalogObject#getName()
   */
  public static const NAME:String = 'name';

  /**
   * @eventType shortDescription
   * @see CatalogObject#getShortDescription()
   */
  public static const SHORT_DESCRIPTION:String = 'shortDescription';
  /**
   * @eventType externalId
   * @see CatalogObject#getExternalId()
   */
  public static const EXTERNAL_ID:String = 'externalId';

  /**
   * @eventType externalTechId
   * @see CatalogObject#getExternalTechId()
   */
  public static const EXTERNAL_TECH_ID:String = 'externalTechId';

  /**
   * @eventType store
   * @see CatalogObject#getStore()
   */
  public static const STORE:String = 'store';

  /**
   * @eventType catalog
   * @see Product#getCatalog() or Category#getCatalog()
   */
  public static const CATALOG:String = 'catalog';

  /**
   * @eventType default
   * @see Catalog#isDefault()
   */
  public static const DEFAULT:String = 'default';

  /**
   * @eventType defaultCatalog
   * @see Store#getDefaultCatalog()
   */
  public static const DEFAULT_CATALOG:String = 'defaultCatalog';

  /**
   * @eventType catalogs
   * @see Store#getCatalogs()
   */
  public static const CATALOGS:String = 'catalogs';

  /**
   * @eventType multiCatalog
   * @see Store#isMultiCatalog()
   */
  public static const MULTI_CATALOG:String = 'multiCatalog';

  /**
   * @eventType id
   * @see CatalogObject#getId()
   */
  public static const ID:String = 'id';

  /**
   * @eventType displayName
   * @see Category#getDisplayName()
   */
  public static const DISPLAY_NAME:String = 'displayName';

  /**
   * @eventType children
   * @see Category#getChildren()
   */
  public static const CHILDREN:String = 'children';

  /**
   * @eventType product
   * @see Category#getProducts()
   */
  public static const PRODUCTS:String = 'products';

  /**
   * @eventType category
   * @see Product#getCategory()
   */
  public static const CATEGORY:String = 'category';

  /**
   * @eventType thumbnailUrl
   * @see Product#getThumbnailUrl()
   */
  public static const THUMBNAIL_URL:String = 'thumbnailUrl';

  /**
   * @eventType visuals
   * @see Product#getVisuals
   */
  public static const VISUALS:String = 'visuals';

  /**
   * @eventType pictures
   * @see Product#getPictures
   */
  public static const PICTURES:String = 'pictures';

  /**
   * @eventType downloads
   * @see Product#getDownloads
   */
  public static const DOWNLOADS:String = 'downloads';

  /**
   * @eventType previewUrl
   * @see Product#getPreviewUrl() and Category#getPreviewUrl()
   */
  public static const PREVIEW_URL:String = 'previewUrl';

  /**
   * @eventType marketingSpots
   * @see Store#getMarketingSpots()
   */
  public static const MARKETING_SPOTS:String = 'marketingSpots';

  /**
   * @eventType segments
   * @see Store#getSegments()
   */
  public static const SEGMENTS:String = 'segments';

  /**
   * @eventType segments
   * @see Store#getContracts()
   */
  public static const CONTRACTS:String = 'contracts';

  /**
   * @eventType workspaces
   * @see Store#getWorkspaces()
   */
  public static const WORKSPACES:String = 'workspaces';

  /**
   * @eventType childrenByName
   * @see Category#getChildrenByName()
   */
  public static const CHILDREN_BY_NAME:String = 'childrenByName';

  /**
   * @eventType subCategories
   * @see Category#getSubCategories()
   */
  public static const SUB_CATEGORIES:String = 'subCategories';

  /**
   * @eventType parent
   * @see Category#getParent()
   * @see ProductVariant#getParent()
   */
  public static const PARENT:String = 'parent';

  /**
   * @eventType storeId
   * @see Store#getStoreId()
   */
  public static const STORE_ID:String = 'storeId';

  public static const ROOT_CATEGORY:String = 'rootCategory';

  public static const MARKETING:String = 'marketing';

  public static const VENDOR_URL:String = 'vendorUrl';
  public static const VENDOR_NAME:String = 'vendorName';
  public static const VENDOR_VERSION:String = 'vendorVersion';

  /**
   * @eventType longDescription
   * @see Product#getLongDescription()
   */
  public static const LONG_DESCRIPTION:String = 'longDescription';

  /**
   * @eventType content
   * @see CatalogObjectImpl#getContent
   */
  public static const CONTENT:String = "content";

  /**
   * Name of the custom attributes property.
   *
   * @eventType customAttributes
   * @see CatalogObject#getCustomAttributes()
   */
  public static const CUSTOM_ATTRIBUTES:String = "customAttributes";

  /**
   * Name of the facets attribute
   */
  public static const FACETS:String = 'facets';

  /**
   * @private
   * This class only defines constants.
   */
  public function CatalogObjectPropertyNames() {
  }

}
}
