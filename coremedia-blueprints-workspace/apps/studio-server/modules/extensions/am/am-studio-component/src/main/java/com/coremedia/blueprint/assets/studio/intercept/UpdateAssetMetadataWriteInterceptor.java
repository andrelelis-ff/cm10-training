package com.coremedia.blueprint.assets.studio.intercept;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapObject;
import com.coremedia.cap.common.CapStruct;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.coremedia.cms.assets.AssetConstants;
import com.coremedia.ecommerce.common.ProductIdExtractor;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Map;

/**
 * A write interceptor that reacts to writes on asset blob properties (renditions)
 * by storing parsed rendition metadata.
 */
public class UpdateAssetMetadataWriteInterceptor extends ContentWriteInterceptorBase {
  private static final Logger LOG = LoggerFactory.getLogger(UpdateAssetMetadataWriteInterceptor.class);

  private String metadataProperty;
  private String metadataSourceProperty;

  /**
   * Set the name of the struct property storing asset metadata, for example,
   * data extracted from Exif sections of image files.
   *
   * @param metadataProperty the property name
   */
  public void setMetadataProperty(String metadataProperty) {
    this.metadataProperty = metadataProperty;
  }

  /**
   * Set the name of the blob property storing the rendition from which metadata
   * is extracted, typically the original rendition.
   *
   * @param metadataSourceProperty the property name
   */
  public void setMetadataSourceProperty(String metadataSourceProperty) {
    this.metadataSourceProperty = metadataSourceProperty;
  }

  static {
    // For enforcer plugin.
    AssetConstants.class.getName();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    if (metadataProperty == null) {
      LOG.error("No metadata property configured.");
      throw new IllegalStateException("metadataProperty is null");
    }

    if (metadataSourceProperty == null) {
      LOG.error("No metadata source property configured.");
      throw new IllegalStateException("metadataSourceProperty is null");
    }
  }

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    Content entity = request.getEntity();

    // The content is currently being created.
    if (entity != null) {
      updateMetadata(entity, properties);
    }
  }

  private void updateMetadata(@NonNull CapObject entity,
                              @NonNull Map<String, Object> properties) {
    if (properties.keySet().contains(metadataSourceProperty)) {
      Blob original = (Blob) properties.get(metadataSourceProperty);
      if (original == null) {
        return;
      }

      Struct metadataStruct = getStructFromRequestOrEntity(metadataProperty, properties, entity);
      if (metadataStruct != null) {
        List<String> storedProductIds = CapStructHelper.getStrings(metadataStruct,
                AssetConstants.METADATA_PRODUCTID_PROPERTY_NAME);
        if (storedProductIds != null && !storedProductIds.isEmpty()) {
          // do not override already stored productIds
          return;
        }
      }

      List<String> productIds = ProductIdExtractor.extractProductIds(original);
      if (productIds.isEmpty()) {
        return;
      }

      if (metadataStruct == null) {
        StructService structService = entity.getRepository().getConnection().getStructService();
        metadataStruct = structService.emptyStruct();
      }

      StructBuilder structBuilder = metadataStruct.builder();
      if (structBuilder.getDescriptor(AssetConstants.METADATA_PRODUCTID_PROPERTY_NAME) == null) {
        structBuilder.declareStrings(AssetConstants.METADATA_PRODUCTID_PROPERTY_NAME, Integer.MAX_VALUE, productIds);
      } else {
        structBuilder.set(AssetConstants.METADATA_PRODUCTID_PROPERTY_NAME, productIds);
      }
      properties.put(metadataProperty, structBuilder.build());
    }
  }

  private Struct getStructFromRequestOrEntity(String propertyName, Map<String, Object> properties, CapStruct entity) {
    return properties.get(propertyName) != null ?
            (Struct) properties.get(propertyName) :
            CapStructHelper.getStruct(entity, propertyName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("metadataProperty", metadataProperty)
                      .add("metadataSourceProperty", metadataSourceProperty)
                      .add("super", super.toString())
                      .add("hash", Integer.toHexString(System.identityHashCode(this)))
                      .toString();
  }
}
