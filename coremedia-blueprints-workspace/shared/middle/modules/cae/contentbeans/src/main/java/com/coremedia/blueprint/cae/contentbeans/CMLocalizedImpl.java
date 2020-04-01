package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLocalized;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.beans.ContentBeanCollections;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Generated extension class for immutable beans of document type "CMLocalized".
 */
public abstract class CMLocalizedImpl extends CMLocalizedBase {
  private SitesService sitesService;
  private ContentBeanCollections contentBeanCollections;

  @Required
  public void setSitesService(SitesService sitesService) {
    Objects.requireNonNull(sitesService);
    this.sitesService = sitesService;
  }

  @Required
  public void setContentBeanCollections(ContentBeanCollections contentBeanCollections) {
    Objects.requireNonNull(sitesService);
    this.contentBeanCollections = contentBeanCollections;
  }

  protected SitesService getSitesService() {
    return sitesService;
  }

  @Override
  public Locale getLocale() {
    return getSitesService().getContentSiteAspect(getContent()).getLocale();
  }

  @Override
  public String getLang() {
    Locale locale = getLocale();
    return locale != null ? locale.getLanguage() : null;
  }

  @Override
  public String getCountry() {
    Locale locale = getLocale();
    return locale != null ? locale.getCountry() : null;
  }

  public CMLocalized getVariant(Locale locale) {
    return getVariantsByLocale().get(locale);
  }

  @Override
  public Map<Locale, ? extends CMLocalized> getVariantsByLocale() {
    return getVariantsByLocale(CMLocalized.class);
  }

  protected <T extends CMLocalized> Map<Locale, T> getVariantsByLocale(Class<T> type) {
    Map<Locale, Content> variantsByLocale = getSitesService().getContentSiteAspect(getContent()).getVariantsByLocale();
    return contentBeanCollections.contentBeanMap(variantsByLocale, type);
  }

  @Override
  public Collection<? extends CMLocalized> getLocalizations() {
    return getVariantsByLocale().values();
  }
}
