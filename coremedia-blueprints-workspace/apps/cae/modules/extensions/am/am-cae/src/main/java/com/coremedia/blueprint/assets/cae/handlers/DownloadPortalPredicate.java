package com.coremedia.blueprint.assets.cae.handlers;

import com.coremedia.blueprint.assets.cae.DownloadPortal;
import com.coremedia.blueprint.cae.view.DynamicIncludePredicate;
import com.coremedia.blueprint.cae.view.DynamicIncludeProvider;
import com.coremedia.blueprint.cae.view.HashBasedFragmentHandler;
import com.coremedia.objectserver.view.RenderNode;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;

public class DownloadPortalPredicate implements DynamicIncludePredicate, DynamicIncludeProvider {

  private static final List<String> VALID_PARAMS = Lists.newArrayList(
          DownloadPortalHandler.CATEGORY_REQUEST_PARAMETER_NAME,
          DownloadPortalHandler.ASSET_REQUEST_PARAMETER_NAME,
          DownloadPortalHandler.SUBJECT_REQUEST_PARAMETER_NAME,
          DownloadPortalHandler.SEARCH_REQUEST_PARAMETER_NAME,
          DownloadPortalHandler.DOWNLOAD_COLLECTION_REQUEST_PARAMETER_NAME
  );

  @Override
  public boolean apply(@Nullable RenderNode input) {
    return null != input
            && input.getBean() instanceof DownloadPortal
            && input.getView() == null;
  }

  @Override
  public HashBasedFragmentHandler getDynamicInclude(Object delegate, String view) {
    return new HashBasedFragmentHandler(delegate, null, VALID_PARAMS);
  }
}
