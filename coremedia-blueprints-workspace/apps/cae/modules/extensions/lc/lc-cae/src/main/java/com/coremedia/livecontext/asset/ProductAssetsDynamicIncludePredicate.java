package com.coremedia.livecontext.asset;

import com.coremedia.blueprint.cae.view.DynamicIncludePredicate;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.objectserver.view.RenderNode;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Predicate to determine if a node to render is dynamic include of product assets.
 */
public class ProductAssetsDynamicIncludePredicate implements DynamicIncludePredicate {

  public static String VIEW_NAME = "asDynaAssets";

  @Override
  public boolean apply(@Nullable RenderNode input) {
    if (input == null) {
      return false;
    } else if (input.getBean() instanceof Product && VIEW_NAME.equals(input.getView())) {
      return true;
    }
    return false;
  }
}
