package com.coremedia.blueprint.cae.web.taglib;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.layout.DynamicContainerStrategy;
import com.coremedia.blueprint.common.layout.DynamizableCMTeasableContainer;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class DynamizableCMTeasableContainerWithDynamicStrategy extends DynamizableCMTeasableContainer {
  private DynamicContainerStrategy dynamicContainerStrategy;

  public DynamizableCMTeasableContainerWithDynamicStrategy(@NonNull CMTeasable teasable, @Nullable String propertyPath, @NonNull DynamicContainerStrategy dynamicContainerStrategy) {
    super(teasable, propertyPath);
    this.dynamicContainerStrategy = dynamicContainerStrategy;
  }

  @Override
  public boolean isDynamic() {
    return dynamicContainerStrategy.isEnabled(teasable) && dynamicContainerStrategy.isDynamic(getItems());
  }
}
