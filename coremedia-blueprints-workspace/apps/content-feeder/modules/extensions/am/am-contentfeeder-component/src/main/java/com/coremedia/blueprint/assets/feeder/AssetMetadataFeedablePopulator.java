package com.coremedia.blueprint.assets.feeder;

import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.cap.feeder.TextParameters;
import com.coremedia.cap.feeder.populate.FeedablePopulator;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.assets.AssetConstants;

import java.util.Calendar;
import java.util.List;

/**
 * Feeds the product ids for assets to the content feeder solr collection.
 * This is needed to find Assets by product id within the studio library search.
 */
public class AssetMetadataFeedablePopulator implements FeedablePopulator<Content> {
  public static final String ASSET_DOCTYPE = "AMAsset";
  public static final String PRODUCT_CODES_FIELD = "assetProductCodes";
  public static final String EXPIRATION_DATE_FIELD = "expirationDate";
  public static final String RIGHTS_CHANNELS_FIELD = "rightsChannels";
  public static final String RIGHTS_REGIONS_FIELD = "rightsRegions";

  private String metadataProperty;

  public void setMetadataProperty(String metadataProperty) {
    this.metadataProperty = metadataProperty;
  }

  @Override
  public void populate(MutableFeedable feedable, Content content) {
    if (feedable == null || content == null) {
      throw new IllegalArgumentException("mutableFeedable and source must not be null");
    }


    if (content.getType().isSubtypeOf(ASSET_DOCTYPE)) {
      Struct metadata = content.getStruct(metadataProperty);
      if (metadata != null) {
        addStringListField(feedable, metadata, AssetConstants.METADATA_PRODUCTID_PROPERTY_NAME, PRODUCT_CODES_FIELD);
        addStringListField(feedable, metadata, AssetConstants.METADATA_CHANNELS_PROPERTY_NAME, RIGHTS_CHANNELS_FIELD);
        addStringListField(feedable, metadata, AssetConstants.METADATA_REGIONS_PROPERTY_NAME, RIGHTS_REGIONS_FIELD);
        addExpirationDate(feedable, metadata);
      }
    }
  }

  private static void addExpirationDate(MutableFeedable feedable, Struct metadata) {
    Calendar expirationDate = CapStructHelper.getDate(metadata, AssetConstants.METADATA_EXPIRATIONDATE_PROPERTY_NAME);
    if (expirationDate != null) {
      feedable.setElement(EXPIRATION_DATE_FIELD, expirationDate, TextParameters.NONE.asMap());
    }
  }

  private static void addStringListField(MutableFeedable feedable, Struct metadata, String propertyName, String solrField) {
    List<String> stringList = CapStructHelper.getStrings(metadata, propertyName);
    if (stringList != null && !stringList.isEmpty()) {
      feedable.setElement(solrField, stringList, TextParameters.NONE.asMap());
    }
  }

}
