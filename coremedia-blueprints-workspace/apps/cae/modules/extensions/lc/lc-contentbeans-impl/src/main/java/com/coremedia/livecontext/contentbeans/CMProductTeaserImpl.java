package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMTeasableImpl;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.xml.Markup;
import org.springframework.beans.factory.annotation.Autowired;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.livecontext.contentbeans.ProductTeasableHelper.isNullOrBlank;
import static com.coremedia.xml.MarkupUtil.isEmptyRichtext;

public class CMProductTeaserImpl extends CMTeasableImpl implements CMProductTeaser {

  private ProductTeasableHelper productTeasableHelper;

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMProductTeaser} objects
   */
  @Override
  public CMProductTeaser getMaster() {
    return (CMProductTeaser) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMProductTeaser> getVariantsByLocale() {
    return getVariantsByLocale(CMProductTeaser.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMProductTeaser> getLocalizations() {
    return (Collection<? extends CMProductTeaser>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMProductTeaser>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMProductTeaser>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMProductTeaser>> getAspects() {
    return (List<? extends Aspect<? extends CMProductTeaser>>) super.getAspects();
  }

  @Override
  public Product getProduct() {
    return productTeasableHelper.getProduct(this);
  }

  @Override
  public boolean isShopNowEnabled(CMContext context) {
    return productTeasableHelper.isShopNowEnabled(this, context);
  }

  @Override
  public String getTeaserTitle() {
    String tt = getContent().getString(TEASER_TITLE);
    tt = productTeasableHelper.getTeaserTitleInternal(this, tt);

    //if the teaser title is still empty then use the super class behavior
    if (isNullOrBlank(tt)) {
      tt = super.getTeaserTitle();
    }
    return tt;
  }

  @Override
  public Markup getTeaserText() {
    Markup tt = getMarkup(TEASER_TEXT);
    tt = productTeasableHelper.getTeaserTextInternal(this, tt);
    //if the teaser text is still empty then use the super class behavior
    if (isEmptyRichtext(tt, true)) {
      tt = super.getTeaserText();
    }
    return tt;
  }

  @Nullable
  @Override
  public ProductInSite getProductInSite() {
    return productTeasableHelper.getProductInSite(this);
  }

  @NonNull
  @Override
  public String getExternalId() {
    String externalId = getContent().getString(EXTERNAL_ID);
    return externalId == null ? "" : externalId.trim();
  }

  @Autowired
  public void setProductTeasableHelper(ProductTeasableHelper productTeasableHelper) {
    this.productTeasableHelper = productTeasableHelper;
  }
}
