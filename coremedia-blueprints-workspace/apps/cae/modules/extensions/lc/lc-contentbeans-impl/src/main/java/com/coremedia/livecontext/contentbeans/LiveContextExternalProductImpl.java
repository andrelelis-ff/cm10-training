package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.layout.PageGridService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.tree.ExternalChannelContentTreeRelation;
import com.coremedia.xml.Markup;
import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.util.Optional;

import static com.coremedia.livecontext.contentbeans.ProductTeasableHelper.isNullOrBlank;
import static com.coremedia.xml.MarkupUtil.isEmptyRichtext;

/**
 * A LiveContext product which is backed by a CMExternalProduct content in the CMS repository.
 */
public class LiveContextExternalProductImpl extends CMExternalProductBase implements LiveContextExternalProduct {

  private PageGridService pageGridService;
  private ExternalChannelContentTreeRelation externalChannelContentTreeRelation;
  private ProductTeasableHelper productTeasableHelper;
  private CommerceConnectionSupplier commerceConnectionSupplier;

  @NonNull
  @Override
  public PageGrid getPageGrid() {
    return pageGridService.getContentBackedPageGrid(this);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(LiveContextExternalProduct.class)
            .add("contentId", getContent().getId())
            .add("externalId", getExternalId())
            .toString();
  }

  @Override
  @Nullable
  public Category getCategory() {
    Product product = getProduct();
    if (product == null) {
      return null;
    }
    return product.getCategory();
  }

  @Override
  public LiveContextExternalChannel getChannel() {
    Content nearestContentForCategory = externalChannelContentTreeRelation.getNearestContentForCategory(getCategory(), getSite());
    return getContentBeanFactory().createBeanFor(nearestContentForCategory, LiveContextExternalChannel.class);
  }

  @Override
  public Site getSite() {
    return getSitesService().getSiteAspect(getContent()).getSite();
  }

  @Override
  @Nullable
  public Product getProduct() {
    String productId = getExternalId();
    if (StringUtils.isEmpty(productId)) {
      return null;
    }

    Optional<CommerceConnection> commerceConnection = commerceConnectionSupplier.findConnection(getContent());

    if (!commerceConnection.isPresent()) {
      return null;
    }

    CommerceConnection connection = commerceConnection.get();

    CommerceId commerceId = CommerceIdParserHelper.parseCommerceIdOrThrow(productId);
    return (Product) connection.getCommerceBeanFactory().createBeanFor(commerceId, connection.getStoreContext());
  }

  @Override
  public Markup getTeaserText() {
    Markup tt = getMarkup(TEASER_TEXT);
    productTeasableHelper.getTeaserTextInternal(this, tt);
    //if the teaser text is still empty then use the super class behavior
    if (isEmptyRichtext(tt, true)) {
      tt = super.getTeaserText();
    }
    return tt;
  }

  @Override
  public String getTeaserTitle() {
    String tt = getContent().getString(TEASER_TITLE);
    productTeasableHelper.getTeaserTitleInternal(this, tt);

    //if the teaser title is still empty then use the super class behavior
    if (isNullOrBlank(tt)) {
      tt = super.getTeaserTitle();
    }
    return tt;
  }

  @Override
  public ProductInSite getProductInSite() {
    return productTeasableHelper.getProductInSite(this);
  }

  @Override
  public boolean isShopNowEnabled(CMContext context) {
    return productTeasableHelper.isShopNowEnabled(this, context);
  }

  @Override
  public CMNavigation getContext() {
    return getChannel();
  }

  @Required
  public void setCommerceConnectionSupplier(CommerceConnectionSupplier commerceConnectionSupplier) {
    this.commerceConnectionSupplier = commerceConnectionSupplier;
  }

  @Required
  public void setPageGridService(PageGridService pageGridService) {
    this.pageGridService = pageGridService;
  }

  @Autowired
  public void setExternalChannelContentTreeRelation(ExternalChannelContentTreeRelation externalChannelContentTreeRelation) {
    this.externalChannelContentTreeRelation = externalChannelContentTreeRelation;
  }

  @Autowired
  public void setProductTeasableHelper(ProductTeasableHelper productTeasableHelper) {
    this.productTeasableHelper = productTeasableHelper;
  }
}
