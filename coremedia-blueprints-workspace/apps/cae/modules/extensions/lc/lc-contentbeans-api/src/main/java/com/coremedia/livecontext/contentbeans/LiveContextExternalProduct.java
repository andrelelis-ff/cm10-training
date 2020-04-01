package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.common.layout.HasPageGrid;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;

/**
 * @cm.template.api
 */
public interface LiveContextExternalProduct extends CMExternalProduct, HasPageGrid, LiveContextProductTeasable {

  Product getProduct();
  Category getCategory();
  LiveContextExternalChannel getChannel();
  Site getSite();
}
