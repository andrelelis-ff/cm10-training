package com.coremedia.blueprint.ecommerce.contentbeans.impl;

import com.coremedia.blueprint.base.ecommerce.catalog.CmsCatalogService;
import com.coremedia.blueprint.cae.contentbeans.CMTeasableImpl;
import com.coremedia.blueprint.common.contentbeans.CMDownload;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.ecommerce.contentbeans.CMProduct;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.asset.CatalogPicture;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class CMProductImpl extends CMTeasableImpl implements CMProduct {
  private CmsCatalogService catalogService;

  // --- configuration ----------------------------------------------

  @Required
  public void setCatalogService(CmsCatalogService catalogService) {
    this.catalogService = catalogService;
  }


  // --- Standard Blueprint typing overrides ------------------------

  @Override
  public CMProduct getMaster() {
    return (CMProduct) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMProduct> getVariantsByLocale() {
    return getVariantsByLocale(CMProduct.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMProduct> getLocalizations() {
    return (Collection<? extends CMProduct>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMProduct>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMProduct>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMProduct>> getAspects() {
    return (List<? extends Aspect<? extends CMProduct>>) super.getAspects();
  }


  // --- Features ---------------------------------------------------

  // --- E-Commerce API ---------------------------------------------
  @Override
  public Product getProduct() {
    return catalogService.findProductByContent(getContent());
  }

  @Override
  public String getProductName() {
    return getContent().getString(PRODUCT_NAME);
  }

  @Override
  public final CatalogPicture getProductPicture() {
    List<CatalogPicture> productPictures = getProductPictures();
    return productPictures.isEmpty() ? null : productPictures.get(0);
  }

  @NonNull
  @Override
  public List<CatalogPicture> getProductPictures() {
    List<? extends CMPicture> pictures = getPictures();
    // Alternatively we could use getProduct().getPictures() here, which also
    // ends up in the CMTeasable#pictures property.  However, getPictures()
    // filters the pictures by the validation service, and I guess that's what
    // people would expect, just like for any other Teasables.

    if (pictures != null && !pictures.isEmpty()) {
      return contentbeansAsCatalogPictures(pictures);
    }
    return Collections.emptyList();
  }

  @Override
  public String getProductCode() {
    return getContent().getString(PRODUCT_CODE);
  }

  // --- CMTeasable ----------------------------------------------------

  @Override
  public String getTitle() {
    String tt = getProductName();
    if (isBlank(tt)) {
      tt = super.getTitle();
    }
    return tt;
  }

  @Override
  public String getHtmlTitle() {
    String tt = super.getHtmlTitle();
    if (isBlank(tt)) {
      tt = getTitle();
    }
    return tt;
  }

  @Override
  public String getTeaserTitle() {
    String tt = getTitle();
    if (isBlank(tt)) {
      tt = super.getTeaserTitle();
    }
    return tt;
  }

  @Override
  public List<CMDownload> getDownloads() {
    List<Content> contents = getContent().getLinks(DOWNLOADS);
    return createBeansFor(contents, CMDownload.class);
  }

  // --- internal ---------------------------------------------------

  private static List<CatalogPicture> contentbeansAsCatalogPictures(List<? extends ContentBean> productPictures) {
    return Lists.transform(productPictures, new Function<ContentBean, CatalogPicture>() {
      @Nullable
      @Override
      public CatalogPicture apply(ContentBean input) {
        return input != null ? new CatalogPicture(null, input.getContent()) : null;
      }
    });
  }
}
