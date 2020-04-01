package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.common.contentbeans.CMPicture;

/**
 * Generated base class for beans of document type "CMALXEventList".
 */
public abstract class CMALXEventListBase extends CMALXBaseListImpl<CMPicture> implements CMALXEventList {

  @Override
  public String getCategory() {
    return getContent().getString(RetrievalUtil.DOCUMENT_PROPERTY_CATEGORY);
  }

  @Override
  public String getAction() {
    return getContent().getString(RetrievalUtil.DOCUMENT_PROPERTY_ACTION);
  }

  @Override
  public CMALXEventList getMaster() {
    return (CMALXEventList) super.getMaster();
  }

}