package com.coremedia.livecontext.fragment.pagegrid;

import com.coremedia.blueprint.common.layout.HasPageGrid;
import com.coremedia.blueprint.common.layout.PageGridPlacement;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class DefaultPageGridPlacementResolver implements PageGridPlacementResolver {
  @Nullable
  @Override
  public PageGridPlacement resolvePageGridPlacement(@NonNull HasPageGrid bean, @NonNull String placementName) {
    return bean.getPageGrid().getPlacementForName(placementName);
  }
}
