package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_9_0;


class SeoSegmentHelper {

  private static final Logger LOG = LoggerFactory.getLogger(SeoSegmentHelper.class);

  private SeoSegmentHelper() {
  }

  static String getSeoSegment(Map<String, Object> delegate, StoreContext storeContext) {
    String seoSegment = getCmSeoSegment(delegate, storeContext);

    if (seoSegment == null) {
      return getWcsSeoSegment(delegate, storeContext);
    }

    return seoSegment;
  }

  @Nullable
  private static String getWcsSeoSegment(Map<String, Object> delegate, StoreContext storeContext) {
    String localizedSeoSegment = getStringValue(delegate, "seo_token_ntk");

    if (localizedSeoSegment != null) {
      String[] localizedSeoSegments = localizedSeoSegment.split(";");
      List<String> localizedSeoSegmentList = Arrays.asList(localizedSeoSegments);
      localizedSeoSegment = processLocalizedSeoSegmentList(storeContext, localizedSeoSegmentList).orElse(null);
      if (localizedSeoSegment == null && localizedSeoSegmentList.size() > 0) {
        localizedSeoSegment = localizedSeoSegmentList.get(0);
      }
    }

    return localizedSeoSegment;
  }

  @Nullable
  private static String getCmSeoSegment(Map<String, Object> delegate, StoreContext storeContext) {
    List<String> localizedSeoSegmentList = new ArrayList<>();
    if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_9_0)) {
      String localizedSeoSegment = getStringValue(delegate, "cm_seo_token_ntk");
      if (localizedSeoSegment != null) {
        String[] localizedSeoSegments = localizedSeoSegment.split(";");
        localizedSeoSegmentList = Arrays.asList(localizedSeoSegments);
      }
    } else {
      localizedSeoSegmentList = DataMapHelper.getList(delegate, "x_cm_seo_token_ntk");
    }

    if (localizedSeoSegmentList.isEmpty()) {
      return null;
    }

    return processLocalizedSeoSegmentList(storeContext, localizedSeoSegmentList).orElse(null);
  }

  private static Optional<String> processLocalizedSeoSegmentList(StoreContext storeContext, List<String> localizedSeoSegmentList) {
    Optional<String> localizedSeoSegment;

    if (localizedSeoSegmentList.size() == 1) {
      localizedSeoSegment = localizedSeoSegmentList
              .stream()
              .findFirst()
              .map(s -> s.substring(s.indexOf('_') + 1));
    } else {
      String storeId = storeContext.getStoreId();
      if (storeId == null) {
        LOG.warn("No store id available in store context {}", storeContext);
        return Optional.empty();
      }
      localizedSeoSegment = localizedSeoSegmentList
              .stream()
              .filter(s -> s.startsWith(storeId + '_'))
              .collect(Collectors.toList())
              .stream()
              .findFirst()
              .map(s -> s.substring(storeId.length() + 1));
    }
    return localizedSeoSegment;
  }

  @Nullable
  private static String getStringValue(@NonNull Map<String, Object> map, @NonNull String key) {
    return DataMapHelper.findString(map, key).orElse(null);
  }
}
