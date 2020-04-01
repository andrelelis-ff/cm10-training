package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.common.layout.DynamizableContainer;

public class P13NContainerPredicate extends AbstractP13nContainerPredicate {

  @Override
  protected boolean isBeanMatching(Object bean) {
    return bean instanceof DynamizableContainer && ((DynamizableContainer) bean).isDynamic();
  }
}
