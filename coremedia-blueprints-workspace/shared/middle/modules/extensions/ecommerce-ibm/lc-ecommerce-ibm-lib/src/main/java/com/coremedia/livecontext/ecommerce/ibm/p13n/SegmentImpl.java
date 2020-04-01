package com.coremedia.livecontext.ecommerce.ibm.p13n;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.p13n.Segment;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Map;

public class SegmentImpl extends AbstractIbmCommerceBean implements Segment {

  private Map<String, Object> delegate;
  private WcSegmentWrapperService segmentWrapperService;

  public Map<String, Object> getDelegate() {
    if (delegate == null) {
      UserContext userContext = CurrentUserContext.get();
      CommerceCache commerceCache = getCommerceCache();

      SegmentCacheKey cacheKey = new SegmentCacheKey(getId(), getContext(), userContext, getSegmentWrapperService(),
              commerceCache);

      delegate = commerceCache.find(cacheKey)
              .orElseThrow(() -> new NotFoundException(getId() + " (segment not found in catalog)"));
    }

    return delegate;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setDelegate(Object delegate) {
    this.delegate = (Map<String, Object>) delegate;
  }

  @Override
  public String getName() {
    return getStringValue(getDelegate(), "displayName.value");
  }

  @Override
  public String getDescription() {
    return getStringValue(getDelegate(), "description.value");
  }

  @Override
  public String getExternalId() {
    return getStringValue(getDelegate(), "id");
  }

  @Override
  public String getExternalTechId() {
    return getStringValue(getDelegate(), "id");
  }

  public void setSegmentWrapperService(WcSegmentWrapperService segmentWrapperService) {
    this.segmentWrapperService = segmentWrapperService;
  }

  public WcSegmentWrapperService getSegmentWrapperService() {
    return segmentWrapperService;
  }

  @Nullable
  private static String getStringValue(@NonNull Map<String, Object> map, @NonNull String key) {
    return DataMapHelper.findString(map, key).orElse(null);
  }
}
