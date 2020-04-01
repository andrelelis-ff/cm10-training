package com.coremedia.livecontext.p13n.include;

import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.layout.DynamicContainerStrategy;
import com.coremedia.blueprint.personalization.include.AbstractP13nContainerPredicate;
import org.springframework.beans.factory.annotation.Required;

import static com.coremedia.blueprint.cae.view.DynamicIncludeHelper.PLACEMENT_FRAGMENT_ROOT_INDICATOR_VIEW;

public class P13NPlacementPredicate extends AbstractP13nContainerPredicate {

  private DynamicContainerStrategy dynamicContainerStrategy;

  @Required
  public void setDynamicContainerStrategy(DynamicContainerStrategy dynamicContainerStrategy) {
    this.dynamicContainerStrategy = dynamicContainerStrategy;
  }

  @Override
  protected boolean isViewMatching(String view) {
    return super.isViewMatching(view) && !PLACEMENT_FRAGMENT_ROOT_INDICATOR_VIEW.equals(view);
  }

  @Override
  protected boolean isBeanMatching(Object bean) {
    if (!(bean instanceof ContentBeanBackedPageGridPlacement)) {
      return false;
    }
    ContentBeanBackedPageGridPlacement placement = (ContentBeanBackedPageGridPlacement) bean;
    CMNavigation navigation = placement.getNavigation();
    return dynamicContainerStrategy.isEnabled(navigation)
            && dynamicContainerStrategy.isDynamic(placement.getItems());
  }
}
