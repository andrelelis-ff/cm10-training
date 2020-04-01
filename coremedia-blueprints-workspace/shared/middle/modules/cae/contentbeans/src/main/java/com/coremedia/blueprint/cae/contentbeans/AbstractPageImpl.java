package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.AbstractPage;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cache.Cache;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.dataviews.AssumesIdentity;
import com.coremedia.objectserver.dataviews.DataViewHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractPageImpl implements AbstractPage, AssumesIdentity {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractPageImpl.class);

  private static final String RIGHT_TO_LEFT = "rtl";
  private static final String LEFT_TO_RIGHT = "ltr";
  private static final Collection<String> RTL_LANGUAGES = Collections.unmodifiableCollection(Arrays.asList(
          "ar", "dv", "fa", "ha", "he", "iw", "ji", "ps", "ur", "yi"
  ));

  private Navigation navigation;
  private Object content;
  private boolean developerMode;
  private SitesService sitesService;

  private String title;
  private String description;
  private String contentId;
  private String contentType;
  private String keywords;
  private Calendar validFrom;
  private Calendar validTo;
  private Cache cache;

  protected AbstractPageImpl() {
  }

  @SuppressWarnings("ConstantConditions")
  protected AbstractPageImpl(boolean developerMode, @NonNull SitesService sitesService, @NonNull Cache cache) {
    checkArgument(sitesService != null, "siteService is null");
    this.developerMode = developerMode;
    this.sitesService = sitesService;
    this.cache = cache;
  }

  @SuppressWarnings("ConstantConditions")
  protected AbstractPageImpl(
          @NonNull Navigation navigation,
          @NonNull Object content,
          boolean developerMode,
          @NonNull SitesService sitesService,
          @NonNull Cache cache) {
    this(developerMode, sitesService, cache);
    setNavigation(navigation);
    setContent(content);
  }

  void setDeveloperMode(boolean developerMode) {
    this.developerMode = developerMode;
  }

  public void setNavigation(Navigation navigation) {
    checkArgument(navigation!=null, "navigation parameter is null");
    // Make sure to not use a DataView instance here but the original bean.
    // This is necessary for collecting dependencies properly.
    this.navigation = DataViewHelper.getOriginal(navigation);
  }

  public void setContent(Object content) {
    checkArgument(content!=null, "content parameter is null");
    // Make sure to not use a DataView instance here but the original bean.
    // This is necessary for collecting dependencies properly.
    this.content = DataViewHelper.getOriginal(content);
  }

  @Override
  public Navigation getNavigation() {
    return navigation;
  }

  @Override
  public Object getContent() {
    return content;
  }

  @Override
  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public void setValidFrom(Calendar validFrom) {
    this.validFrom = validFrom;
  }

  public void setValidTo(Calendar validTo) {
    this.validTo = validTo;
  }

  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  protected SitesService getSitesService() {
    return sitesService;
  }

  @Override
  public PageGrid getPageGrid() {
    return getContext().getPageGrid();
  }

  /**
   * Implementation of the getContext() of the Page interface.
   */
  public CMContext getContext() {
    return getNavigation().getContext();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<AbstractPage>> getAspectByName() {
    return (Map<String, ? extends Aspect<AbstractPage>>) getNavigation().getContext().getAspectsProvider().getAspects(this);
  }

  @Override
  public boolean isDetailView() {
    return !(getContent() instanceof Navigation);
  }

  @Override
  public Locale getLocale() {
    CMContext context = getContext();
    if (context != null) {
      return getSitesService().getContentSiteAspect(context.getContent()).getLocale();
    }

    LOG.warn("returning empty locale because site of page {} cannot be determined", this);
    return Locale.ROOT;
  }

  @Override
  public String getDirection() {
    return RTL_LANGUAGES.contains(getLocale().getLanguage())
            ? RIGHT_TO_LEFT
            : LEFT_TO_RIGHT;
  }

  /**
   * SEO: meta data title
   *  @deprecated use {@link CMLinkable#getHtmlTitle()}  instead}
   */
  @Override
  @Deprecated
  public String getTitle() {
    return title==null || title.isEmpty() ? "No Page Title" : title;
  }

  /**
   * SEO: meta data keywords
   */
  @Override
  public String getKeywords() {
    if (StringUtils.hasText(keywords)) {
      return keywords;
    }
    Navigation context = getNavigation();
    while (context != null) {
      String keywords = context.getKeywords();
      if (StringUtils.hasText(keywords)) {
        return keywords;
      }
      context = context.getParentNavigation();
    }
    return null;
  }

  /**
   *  SEO: meta data description
   *  @deprecated use {@link CMLinkable#getHtmlDescription()}  instead}
   */
  @Override
  @Deprecated
  public String getDescription() {
    if (StringUtils.hasText(description)) {
      return description;
    }
    Navigation context = getNavigation();
    while (context != null) {
      String description = context.getKeywords();
      if (StringUtils.hasText(description)) {
        return description;
      }
      context = context.getParentNavigation();
    }
    return null;
  }

  /**
   * Returns the combined valid from value of content and navigation.
   *
   * @return the combined valid from value of content and navigation.
   */
  @Override
  public Calendar getValidFrom() {
    Calendar contentsValidFrom = validFrom;
    Calendar navigationsValidFrom = getValidFrom(getNavigation());
    if (contentsValidFrom==null || navigationsValidFrom==null) {
      return contentsValidFrom==null ? navigationsValidFrom : contentsValidFrom;
    }
    return contentsValidFrom.after(navigationsValidFrom) ? contentsValidFrom : navigationsValidFrom;
  }

  /**
   * Returns the combined valid to value of content and navigation.
   *
   * @return the combined valid to value of content and navigation.
   */
  @Override
  public Calendar getValidTo() {
    // this method is used by the ContentValidityInterceptor
    Calendar contentsValidTo = validTo;
    Calendar navigationsValidTo = getValidTo(getNavigation());
    if (contentsValidTo==null || navigationsValidTo==null) {
      return contentsValidTo==null ? navigationsValidTo : contentsValidTo;
    }
    return contentsValidTo.before(navigationsValidTo) ? contentsValidTo : navigationsValidTo;
  }

  private Calendar getValidFrom(Linkable linkable) {
    //todo pull getValidFrom() up to Linkable?
    if (!(linkable instanceof CMLinkable)) {
      return null;
    }
    return ((CMLinkable) linkable).getValidFrom();
  }

  private Calendar getValidTo(Linkable linkable) {
    //todo pull getValidTo() up to Linkable?
    if (!(linkable instanceof CMLinkable)) {
      return null;
    }
    return ((CMLinkable) linkable).getValidTo();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends AbstractPage>> getAspects() {
    List<Aspect<AbstractPage>> result = new ArrayList<>();
    for (Map.Entry<String, ? extends Aspect<? extends AbstractPage>> entry : getAspectByName().entrySet()) {
      Aspect<AbstractPage> value = (Aspect<AbstractPage>) entry.getValue();
      result.add(value);
    }
    return result;
  }

  @Override
  public boolean isDeveloperMode() {
    return developerMode;
  }

  protected Cache getCache() {
    return cache;
  }

  @Override
  public void assumeIdentity(Object bean) {
    AbstractPageImpl other = (AbstractPageImpl) bean;
    this.navigation = other.getNavigation();
    this.content = other.getContent();
    this.developerMode = other.isDeveloperMode();
    this.title = other.title;
    this.description = other.description;
    this.contentId = other.contentId;
    this.contentType = other.contentType;
    this.keywords = other.keywords;
    this.validFrom = other.validFrom;
    this.validTo = other.validTo;
    this.sitesService = other.sitesService;
    this.cache = other.cache;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractPageImpl that = (AbstractPageImpl) o;

    if (developerMode != that.developerMode) {
      return false;
    }
    if (cache != that.cache) {
      return false;
    }
    if (content != null ? !content.equals(that.content) : that.content != null) {
      return false;
    }
    if (contentId != null ? !contentId.equals(that.contentId) : that.contentId != null) {
      return false;
    }
    if (contentType != null ? !contentType.equals(that.contentType) : that.contentType != null) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
      return false;
    }
    if (keywords != null ? !keywords.equals(that.keywords) : that.keywords != null) {
      return false;
    }
    if (navigation != null ? !navigation.equals(that.navigation) : that.navigation != null) {
      return false;
    }
    if (title != null ? !title.equals(that.title) : that.title != null) {
      return false;
    }
    if (validFrom != null ? !validFrom.equals(that.validFrom) : that.validFrom != null) {
      return false;
    }
    //noinspection RedundantIfStatement
    if (validTo != null ? !validTo.equals(that.validTo) : that.validTo != null) {
      return false;
    }

    return true;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public int hashCode() {
    int result = navigation != null ? navigation.hashCode() : 0;
    result = 31 * result + (content != null ? content.hashCode() : 0);
    result = 31 * result + (developerMode ? 1 : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (contentId != null ? contentId.hashCode() : 0);
    result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
    result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
    result = 31 * result + (validFrom != null ? validFrom.hashCode() : 0);
    result = 31 * result + (validTo != null ? validTo.hashCode() : 0);
    result = 31 * result + (cache != null ? cache.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" +
            "navigation=" + navigation +
            ", content=" + content +
            ']';
  }
}
