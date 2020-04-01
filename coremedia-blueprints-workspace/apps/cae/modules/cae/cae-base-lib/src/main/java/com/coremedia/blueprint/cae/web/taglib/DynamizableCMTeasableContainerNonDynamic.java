package com.coremedia.blueprint.cae.web.taglib;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.layout.DynamizableCMTeasableContainer;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class DynamizableCMTeasableContainerNonDynamic extends DynamizableCMTeasableContainer {
  DynamizableCMTeasableContainerNonDynamic(@NonNull CMTeasable teasable, @Nullable String propertyPath) {
    super(teasable, propertyPath);
  }

  @Override
  public boolean isDynamic() {
    return false;
  }
}
