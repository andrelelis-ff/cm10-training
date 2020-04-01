package com.coremedia.livecontext.ecommerce.ibm.catalog;


import org.apache.commons.collections4.Transformer;

import java.util.Map;

abstract class AbstractDelegateLoader implements Transformer {

  private Map<String, Object> delegateFromCache;

  @Override
  public final Object transform(Object input) {
    if (null == delegateFromCache) {
      delegateFromCache = getDelegateFromCache();
    }
    //noinspection SuspiciousMethodCalls
    return delegateFromCache.get(input);
  }

  abstract Map<String, Object> getDelegateFromCache();

}
