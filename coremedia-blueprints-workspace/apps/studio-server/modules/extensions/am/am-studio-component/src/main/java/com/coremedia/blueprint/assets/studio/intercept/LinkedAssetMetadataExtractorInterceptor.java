package com.coremedia.blueprint.assets.studio.intercept;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.assets.AssetConstants;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;

/**
 * A write interceptor that reacts to writes on content derived from assets
 * by providing asset metadata to subsequent write interceptors.
 */
public class LinkedAssetMetadataExtractorInterceptor extends ContentWriteInterceptorBase {
  private static final Logger LOG = LoggerFactory.getLogger(LinkedAssetMetadataExtractorInterceptor.class);

  private String assetLinkProperty;
  private String assetMetadataProperty;
  private String linkedAssetTypeProperty;

  public void intercept(ContentWriteRequest request) {
    Content writtenEntity = request.getEntity();

    Content linkedAsset = null;
    if (writtenEntity != null && writtenEntity.getType().getDescriptor(assetLinkProperty) != null) {
      linkedAsset = writtenEntity.getLink(assetLinkProperty);
    }
    if (request.getProperties().keySet().contains(assetLinkProperty)) {
      List<Content> linkedAssets = (List<Content>) request.getProperties().get(assetLinkProperty);
      if (!linkedAssets.isEmpty()) {
        linkedAsset = linkedAssets.get(0);
      }
    }

    if (linkedAsset != null && linkedAsset.getType().isSubtypeOf(linkedAssetTypeProperty) && linkedAsset.isReadable()) {
      Struct metadataStruct = linkedAsset.getStruct(assetMetadataProperty);
      List<String> assetMetadataProductIDs = Collections.emptyList();
      if (metadataStruct != null
              && metadataStruct.getType().getDescriptor(AssetConstants.METADATA_PRODUCTID_PROPERTY_NAME) != null)  {
        assetMetadataProductIDs = metadataStruct.getStrings(AssetConstants.METADATA_PRODUCTID_PROPERTY_NAME);
      }
      LOG.debug("Updated image links to asset with metadata: " + assetMetadataProductIDs);
      request.setAttribute(AssetConstants.PRODUCT_IDS_ATTRIBUTE_NAME, assetMetadataProductIDs);
    }
  }

  /**
   * The name of the property that links the derived document to the source asset.
   * @param assetLinkProperty The name of the property.
   */
  @Required
  public void setAssetLinkProperty(String assetLinkProperty) {
    this.assetLinkProperty = assetLinkProperty;
  }

  public String getAssetLinkProperty() {
    return assetLinkProperty;
  }

  /**
   * The name of the property that source picture assets keep their metadata in.
   * @param assetMetadataProperty The name of the asset metadata property.
   */
  @Required
  public void setAssetMetadataProperty(String assetMetadataProperty) {
    this.assetMetadataProperty = assetMetadataProperty;
  }

  public String getAssetMetadataProperty() {
    return assetMetadataProperty;
  }

  /**
   * The type of the asset linked in {@link #setAssetLinkProperty(String)} for which metadata should be extracted.
   * Metadata is only extracted if the linked asset's type is a subtype of the given type.
   *
   * @param linkedAssetType the type of the linked asset
   */
  @Required
  public void setLinkedAssetType(String linkedAssetType) {
    this.linkedAssetTypeProperty = linkedAssetType;
  }

  public String getLinkedAssetType() {
    return linkedAssetTypeProperty;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("assetLinkProperty", assetLinkProperty)
                      .add("assetMetadataProperty", assetMetadataProperty)
                      .add("linkedAssetTypeProperty", linkedAssetTypeProperty)
                      .add("super", super.toString())
                      .add("hash", Integer.toHexString(System.identityHashCode(this)))
                      .toString();
  }
}
