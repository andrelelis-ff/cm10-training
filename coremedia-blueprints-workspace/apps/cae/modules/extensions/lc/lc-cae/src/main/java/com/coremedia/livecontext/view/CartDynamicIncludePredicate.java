package com.coremedia.livecontext.view;

import com.coremedia.blueprint.cae.view.DynamicIncludePredicate;
import com.coremedia.livecontext.ecommerce.order.Cart;
import com.coremedia.objectserver.view.RenderNode;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Dynamically include {@link Cart} beans if they are displayed as Header items.
 */
public class CartDynamicIncludePredicate implements DynamicIncludePredicate {

  @Override
  public boolean apply(@Nullable RenderNode input) {
    return input != null && input.getBean() instanceof Cart;
  }

}
