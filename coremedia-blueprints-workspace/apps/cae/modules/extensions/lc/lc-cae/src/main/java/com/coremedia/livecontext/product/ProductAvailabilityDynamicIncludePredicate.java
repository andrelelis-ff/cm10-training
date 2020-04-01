package com.coremedia.livecontext.product;

import com.coremedia.blueprint.cae.view.DynamicIncludePredicate;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.inventory.AvailabilityInfo;
import com.coremedia.objectserver.view.RenderNode;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Predicate to determine if a node to render is an instance of {@link AvailabilityInfo}.
 */
public class ProductAvailabilityDynamicIncludePredicate implements DynamicIncludePredicate {

  public static final String VIEW_NAME_AVAILABILITY_FRAGMENT = "availabilityFragment";

  @Override
  public boolean apply(@Nullable RenderNode input) {
    if(input ==null) {
      return false;
    }
    else if(input.getBean() instanceof ProductInSite && VIEW_NAME_AVAILABILITY_FRAGMENT.equals(input.getView())) {
      return true;
    }

    return false;
  }
}
