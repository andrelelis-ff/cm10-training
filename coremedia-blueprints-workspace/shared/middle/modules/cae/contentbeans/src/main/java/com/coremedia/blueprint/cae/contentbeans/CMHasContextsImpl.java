package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.objectserver.dataviews.DataViewFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Generated extension class for immutable beans of document type "CMHasContexts".
 */
public class CMHasContextsImpl extends CMHasContextsBase {
  private static final Logger LOG = LoggerFactory.getLogger(CMHasContextsImpl.class);

  private DataViewFactory dataViewFactory;

  public DataViewFactory getDataViewFactory() {
    return dataViewFactory;
  }

  @Required
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }
}
