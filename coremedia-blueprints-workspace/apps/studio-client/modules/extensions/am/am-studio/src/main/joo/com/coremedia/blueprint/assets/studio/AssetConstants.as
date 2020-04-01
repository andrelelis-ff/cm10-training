package com.coremedia.blueprint.assets.studio {

public class AssetConstants {

  public static const ASSET_LIBRARY_PATH:String = "/Assets";
  public static const ASSET_TAXONOMY_ID:String = "Asset Download Portal";

  public static const DOCTYPE_ASSET:String = 'AMAsset';
  public static const DOCTYPE_DOCUMENT_ASSET:String = 'AMDocumentAsset';
  public static const DOCTYPE_PICTURE_ASSET:String = 'AMPictureAsset';
  public static const DOCTYPE_VIDEO_ASSET:String = 'AMVideoAsset';

  public static const DOCTYPE_ASSET_TAXONOMY:String = 'AMTaxonomy';
  public static const PROPERTY_ASSET_ORIGINAL:String = 'original';
  public static const PROPERTY_ASSET_THUMBNAIL:String = 'thumbnail';
  public static const PROPERTY_ASSET_DOWNLOAD:String = 'download';
  public static const PROPERTY_ASSET_WEB:String = 'web';
  public static const PROPERTY_ASSET_PRINT:String = 'print';
  public static const PROPERTY_ASSET_ASSETTAXONOMY:String = 'assetTaxonomy';
  public static const PROPERTY_ASSET_ASSETTAXONOMY_SEARCH:String = 'assettaxonomy';
  public static const PROPERTY_ASSET_METADATA:String = 'metadata';
  public static const PROPERTY_ASSET_METADATA_CHANNELS:String = 'channels';
  public static const PROPERTY_ASSET_METADATA_REGIONS:String = 'regions';
  public static const PROPERTY_ASSET_METADATA_COPYRIGHT:String = 'copyright';
  public static const PROPERTY_ASSET_METADATA_EXPIRATIONDATE:String = 'expirationDate';
  public static const PROPERTY_ASSET_METADATA_RENDITIONS:String = "renditions";
  public static const PROPERTY_ASSET_METADATA_PRODUCT_CODES:String = 'productIds';

  public function AssetConstants() {
    throw new Error("Utility class AssetConstants must not be instantiated");
  }
}
}