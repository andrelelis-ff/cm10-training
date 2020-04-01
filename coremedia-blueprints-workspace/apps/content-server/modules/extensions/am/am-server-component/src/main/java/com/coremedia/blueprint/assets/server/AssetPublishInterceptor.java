package com.coremedia.blueprint.assets.server;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapStruct;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.server.plugins.PublishInterceptorBase;
import com.coremedia.cms.server.plugins.PublishRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.coremedia.cap.common.CapStructHelper.getStruct;
import static java.util.Objects.requireNonNull;

/**
 * Interceptor which determines if certain blobs shall be published or not.
 */
public class AssetPublishInterceptor extends PublishInterceptorBase {
  private static final Logger LOG = LoggerFactory.getLogger(AssetPublishInterceptor.class);

  private static final String METADATA_RENDITION_SHOW_PROPERTY_NAME = "show";
  private static final String METADATA_RENDITIONS_PROPERTY_NAME = "renditions";

  private String assetMetadataProperty;
  private boolean defaultHiddenState;
  private final Map<String, Boolean> removeOverride = new HashMap<>();

  /**
   * Set content property which defines if a blob shall be published or not.
   *
   * @param assetMetadataProperty property to use
   */
  @Required
  public void setAssetMetadataProperty(@NonNull final String assetMetadataProperty) {
    this.assetMetadataProperty = requireNonNull(assetMetadataProperty, "assetMetadataProperty must not be null.");
  }

  /**
   * If nothing is specified for a given blob, what is the default to take.
   *
   * @param removeDefault {@code true} to remove blob by default; {@code false} otherwise
   */
  public void setRemoveDefault(final boolean removeDefault) {
    defaultHiddenState = removeDefault;
  }

  /**
   * Set renditions where to override the removal behavior. Keys are the rendition names, values are the overridden
   * values for removal. Thus a key with value {@code true} will be removed always,
   * a key with value {@code false} will never be removed.
   *
   * @param removeOverride overrides map
   */
  public void setRemoveOverride(@Nullable Map<String, Boolean> removeOverride) {
    this.removeOverride.clear();
    if (removeOverride != null) {
      this.removeOverride.putAll(removeOverride);
    }
  }

  @Override
  public void intercept(@NonNull final PublishRequest request) {
    final Version version = request.getVersion();

    final Map<String, Object> properties = request.getProperties();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      if (entry.getValue() instanceof Blob) {
        final String rendition = entry.getKey();
        if (isRenditionHidden(version, rendition)) {
          // Either no renditions are selected for download or this specific
          // rendition is not selected. Do not publish it.
          entry.setValue(null);
        }
      }
    }
  }

  private boolean isRenditionHidden(final CapStruct version, final String rendition) {
    if (removeOverride.containsKey(rendition)) {
      return removeOverride.get(rendition);
    }
    try {
      return isRenditionHidden(getRenditionStruct(version, rendition));
    } catch (Exception e) {
      LOG.warn("exception while determining hidden state for rendition '{}', hiding rendition", rendition, e);
      return defaultHiddenState;
    }
  }

  private boolean isRenditionHidden(@Nullable CapStruct renditionStruct) {
    if (renditionStruct == null
            || !CapStructHelper.hasProperty(renditionStruct, METADATA_RENDITION_SHOW_PROPERTY_NAME)) {
      return defaultHiddenState;
    }

    return !renditionStruct.getBoolean(METADATA_RENDITION_SHOW_PROPERTY_NAME);
  }

  @Nullable
  private CapStruct getRenditionStruct(final CapStruct version, final String rendition) {
    Struct metadataStruct = getStruct(version, assetMetadataProperty);
    if (metadataStruct == null) {
      return null;
    }

    Struct allRenditionsStruct = getStruct(metadataStruct, METADATA_RENDITIONS_PROPERTY_NAME);
    if (allRenditionsStruct == null) {
      return null;
    }

    return getStruct(allRenditionsStruct, rendition);
  }

}
