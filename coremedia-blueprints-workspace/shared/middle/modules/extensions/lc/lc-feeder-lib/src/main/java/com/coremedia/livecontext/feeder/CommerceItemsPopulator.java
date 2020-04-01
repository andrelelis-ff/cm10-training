package com.coremedia.livecontext.feeder;

import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.cap.feeder.TextParameters;
import com.coremedia.cap.feeder.populate.FeedablePopulator;

import java.util.Collections;
import java.util.List;

public class CommerceItemsPopulator implements FeedablePopulator<Content> {

  static final String TYPE_PRODUCT_TEASER = "CMProductTeaser";
  static final String TYPE_MARKETING_SPOT = "CMMarketingSpot";
  static final String TYPE_PRODUCT_LIST = "CMProductList";
  static final String TYPE_LINKABLE = "CMLinkable";
  static final String TYPE_EXTERNAL_CHANNEL = "CMExternalChannel";

  static final String PROPERTY_PRODUCT_TEASER_EXTERNAL_ID = "externalId";
  private static final String COMMERCE_ITEMS_INDEX_FIELD = SearchConstants.FIELDS.COMMERCE_ITEMS.toString();

  @Override
  public void populate(MutableFeedable feedable, Content content) {
    ContentType contentType = content.getType();
    String contentTypeName = contentType.getName();
    if (contentTypeName.equals(TYPE_PRODUCT_TEASER)
            || contentTypeName.equals(TYPE_MARKETING_SPOT)
            || contentTypeName.equals(TYPE_PRODUCT_LIST)
            || contentTypeName.equals(TYPE_EXTERNAL_CHANNEL)) {
      String externalId = content.getString(PROPERTY_PRODUCT_TEASER_EXTERNAL_ID);
      if (externalId != null && !externalId.isEmpty()) {
        feedable.setElement(COMMERCE_ITEMS_INDEX_FIELD, Collections.singleton(externalId), TextParameters.NONE.asMap());
      }
    } else if (contentType.isSubtypeOf(TYPE_LINKABLE)) {
      List<String> assignedExternalIds = CommerceReferenceHelper.getExternalIds(content);
      if (!assignedExternalIds.isEmpty()){
        feedable.setElement(COMMERCE_ITEMS_INDEX_FIELD, assignedExternalIds, TextParameters.NONE.asMap());
      }
    }
  }


}
