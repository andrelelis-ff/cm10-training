package com.coremedia.blueprint.assets.studio {

import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;

/**
 * Utility class for easy access to all asset doctypes.
 */
public class AssetDoctypeUtil {
  private static var amAssetContentType:ContentType;
  [ArrayElementType("com.coremedia.cap.content.ContentType")]
  private static var allAssetContentTypes:Array;
  [ArrayElementType("String")]
  private static var allAssetContentTypeNames:Array;

  public static function getAssetContentType():ContentType {
    if (!amAssetContentType) {
      amAssetContentType = getRepository().getContentType(AssetConstants.DOCTYPE_ASSET);
    }
    return amAssetContentType;
  }

  public static function getAllAssetContentTypes():Array {
    if (!allAssetContentTypes) {
      allAssetContentTypes = getRepository().getContentTypes().filter(
              function (contentType:ContentType):Boolean {
                return contentType.isSubtypeOf(getAssetContentType());
              });
    }
    return allAssetContentTypes;
  }

  public static function getAllAssetContentTypeNames():Array {
    if (!allAssetContentTypeNames) {
      allAssetContentTypeNames = getAllAssetContentTypes().map(
              function (contentType:ContentType):String {
                return contentType.getName();
              });
    }
    return allAssetContentTypeNames;
  }

  private static function getRepository():ContentRepository {
    return SESSION.getConnection().getContentRepository();
  }
}
}
