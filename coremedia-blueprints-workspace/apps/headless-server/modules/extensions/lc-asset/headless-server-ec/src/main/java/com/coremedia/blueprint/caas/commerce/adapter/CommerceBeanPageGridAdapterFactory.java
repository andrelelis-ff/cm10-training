package com.coremedia.blueprint.caas.commerce.adapter;

import com.coremedia.blueprint.base.caas.model.adapter.PageGridAdapter;
import com.coremedia.blueprint.base.caas.model.adapter.PageGridAdapterFactory;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.tree.ExternalChannelContentTreeRelation;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public class CommerceBeanPageGridAdapterFactory extends PageGridAdapterFactory {

  public static final String PDP_PAGEGRID_PROPERTY_NAME = "pdpPagegrid";

  private final ExternalChannelContentTreeRelation externalChannelContentTreeRelation;
  private final SitesService sitesService;
  private final String propertyName;
  private final AugmentationService augmentationService;

  public CommerceBeanPageGridAdapterFactory(String propertyName,
                                            AugmentationService augmentationService,
                                            ExternalChannelContentTreeRelation externalChannelContentTreeRelation,
                                            ContentBackedPageGridService contentBackedPageGridService,
                                            SitesService sitesService) {
    super(contentBackedPageGridService);
    this.propertyName = propertyName;
    this.augmentationService = augmentationService;
    this.externalChannelContentTreeRelation = externalChannelContentTreeRelation;
    this.sitesService = sitesService;
  }

  /**
   *
   * @throws IllegalStateException if the page grid could not be loaded
   */
  public PageGridAdapter to(CommerceBean commerceBean) {
    return to(getContent(commerceBean), propertyName);
  }

  private String getSiteId(CommerceBean commerceBean) {
    return commerceBean.getContext().getSiteId();
  }

  private Site getSite(CommerceBean commerceBean) {
    return sitesService.getSite(getSiteId(commerceBean));
  }

  private Content getContent(CommerceBean commerceBean) {
    Content content = augmentationService.getContent(commerceBean);
    if (content != null) {
      return content;
    }
    if (commerceBean instanceof ProductVariant) {
      content = augmentationService.getContent(((ProductVariant) commerceBean).getParent());
    }
    if (content != null) {
      return content;
    }
    Category category = commerceBean instanceof Category
            ? (Category)commerceBean
            : commerceBean instanceof Product
            ? ((Product) commerceBean).getCategory()
            : illegalState(commerceBean);
    content = externalChannelContentTreeRelation.getNearestContentForCategory(category, getSite(commerceBean));
    if (content != null) {
      return content;
    }
    return illegalState("cannot find content for " + commerceBean);
  }

  private <T> T illegalState(CommerceBean commerceBean) {
    return illegalState("commerce bean must be instanceof Product or Category, but is " + commerceBean.getClass().getName());
  }

  private <T> T illegalState(String msg) {
    throw new IllegalArgumentException(msg);
  }
}
