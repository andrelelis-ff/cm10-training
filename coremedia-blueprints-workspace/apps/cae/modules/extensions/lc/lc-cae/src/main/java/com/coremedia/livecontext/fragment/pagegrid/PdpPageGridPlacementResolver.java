package com.coremedia.livecontext.fragment.pagegrid;

import com.coremedia.blueprint.common.layout.HasPageGrid;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class PdpPageGridPlacementResolver implements PageGridPlacementResolver {
  @Nullable
  @Override
  public PageGridPlacement resolvePageGridPlacement(@NonNull HasPageGrid bean, @NonNull String placementName) {
    if (bean instanceof CMExternalChannel) {
      return ((CMExternalChannel) bean).getPdpPagegrid().getPlacementForName(placementName);
    }
    return bean.getPageGrid().getPlacementForName(placementName);
  }
}
