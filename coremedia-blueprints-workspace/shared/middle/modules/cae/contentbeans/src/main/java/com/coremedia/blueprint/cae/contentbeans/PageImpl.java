package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.coderesources.CodeResourcesCacheKey;
import com.coremedia.blueprint.coderesources.CodeResourcesModel;
import com.coremedia.blueprint.common.contentbeans.CMAbstractCode;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PageImpl extends AbstractPageImpl implements Page {
  private static final Logger LOG = LoggerFactory.getLogger(PageImpl.class);

  private TreeRelation<Content> contentTreeRelation;
  private ContentBeanFactory contentBeanFactory;
  private DataViewFactory dataViewFactory;
  private User developer;
  private Boolean mergeCodeResources;

  /**
   * Do not call this constructor yourself, this is only for {@link com.coremedia.objectserver.dataviews.DataView} usage,
   * which will afterwards call {@link #assumeIdentity(Object)} with the originating uncached bean instance.
   */
  public PageImpl() {
    super();
  }

  /**
   * Return true if the code resources of a page (JavaScript and CSS) should be
   *  merged into a single web resource, respectively. If null (which is the
   * default), code resources are only merged when development mode is off.
   */
  public Boolean getMergeCodeResources() {
    return mergeCodeResources;
  }

  /**
   * This setting provides explicit control whether the code resources of a page (JavaScript and CSS)
   * are merged into a single web resource, respectively. If not set or set to null (which is the
   * default), code resources are only merged when development mode is off.
   */
  public void setMergeCodeResources(Boolean mergeCodeResources) {
    this.mergeCodeResources = mergeCodeResources;
  }

  /**
   * Prepare a PageImpl by dependency injection.
   * <p>
   * Using this constructor, you must set content and navigation afterwards.
   */
  @VisibleForTesting  // ... and for subclasses. Otherwise, use the "cmPage" factory bean
  public PageImpl(boolean developerMode,
                  SitesService sitesService,
                  Cache cache,
                  TreeRelation<Content> contentTreeRelation,
                  ContentBeanFactory contentBeanFactory,
                  DataViewFactory dataViewFactory) {
    super(developerMode, sitesService, cache);
    this.contentTreeRelation = contentTreeRelation;
    this.contentBeanFactory = contentBeanFactory;
    this.dataViewFactory = dataViewFactory;
  }

  @VisibleForTesting  // use the "cmPage" factory bean
  public PageImpl(Navigation navigation,
                  Object content,
                  boolean developerMode,
                  SitesService sitesService,
                  Cache cache,
                  TreeRelation<Content> contentTreeRelation,
                  ContentBeanFactory contentBeanFactory,
                  DataViewFactory dataViewFactory) {
    super(navigation, content, developerMode, sitesService,cache);
    this.contentTreeRelation = contentTreeRelation;
    this.contentBeanFactory = contentBeanFactory;
    this.dataViewFactory = dataViewFactory;
  }

  /**
   * Set a specialized tree relation.
   * <p>
   * The Blueprint's standard tree relation, which is based on the
   * CMNavigation#children property, is default.  For special pages you can
   * override it.
   */
  public void setContentTreeRelation(TreeRelation<Content> contentTreeRelation) {
    this.contentTreeRelation = contentTreeRelation;
  }

  /**
   * Set a developer.
   * <p>
   * Some Blueprint features support developer's work in progress, which
   * becomes effective by this property.
   * <p>
   * Should be used only in preview applications.
   */
  public void setDeveloper(@Nullable User developer) {
    this.developer = developer;
    if (developer!=null) {
      setDeveloperMode(true);
    }
  }

  @Override
  public Blob getFavicon() {
    CMNavigation rootNavi = getNavigation().getRootNavigation();
    return rootNavi.getFavicon();
  }

  /**
   * @return the CSS contents for this page.
   */
  @Override
  public List<?> getCss() {
    return codeResourcesAsBeans(CMNavigationBase.CSS, CodeResourcesModel.MODE_BODY);
  }

  @Override
  public List<?> getInternetExplorerCss() {
    return codeResourcesAsBeans(CMNavigationBase.CSS, CodeResourcesModel.MODE_IE);
  }

  /**
   * @return the JS contents for this page.
   */
  @Override
  public List<?> getJavaScript() {
    return codeResourcesAsBeans(CMNavigationBase.JAVA_SCRIPT, CodeResourcesModel.MODE_BODY);
  }

  @Override
  public List<?> getHeadJavaScript() {
    return codeResourcesAsBeans(CMNavigationBase.JAVA_SCRIPT, CodeResourcesModel.MODE_HEAD);
  }

  @Override
  public List<?> getInternetExplorerJavaScript() {
    return codeResourcesAsBeans(CMNavigationBase.JAVA_SCRIPT, CodeResourcesModel.MODE_IE);
  }



  // --- hash & cache -----------------------------------------------

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    PageImpl page = (PageImpl) o;
    return Objects.equals(contentTreeRelation, page.contentTreeRelation) &&
           Objects.equals(developer, page.developer) &&
           Objects.equals(mergeCodeResources, page.mergeCodeResources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), contentTreeRelation, developer, mergeCodeResources);
  }

  @Override
  public void assumeIdentity(Object bean) {
    super.assumeIdentity(bean);
    PageImpl other = (PageImpl) bean;
    contentTreeRelation = other.contentTreeRelation;
    contentBeanFactory = other.contentBeanFactory;
    dataViewFactory = other.dataViewFactory;
    developer = other.developer;
    mergeCodeResources = other.mergeCodeResources;
  }


  // --- internal ---------------------------------------------------

  private List<?> codeResourcesAsBeans(String codePropertyName, String htmlMode) {
    CodeResourcesCacheKey cacheKey = new CodeResourcesCacheKey(getContext().getContent(), codePropertyName, isDeveloperMode(), contentTreeRelation, developer);
    cacheKey.setMergeCodeResources(getMergeCodeResources());
    CodeResourcesModel codeResourcesModel = getCache().get(cacheKey).getModel(htmlMode);
    return codeResourcesModelToBeans(codeResourcesModel);
  }

  private List<?> codeResourcesModelToBeans(CodeResourcesModel codeResourcesModel) {
    List<?> codeResources = codeResourcesModel.getLinkTargetList();
    List<Object> result = new ArrayList<>();
    for (Object item : codeResources) {
      if (item instanceof Content) {
        CMAbstractCode bean = contentBeanFactory.createBeanFor((Content) item, CMAbstractCode.class);
        result.add(dataViewFactory!=null ? dataViewFactory.loadCached(bean, null) : bean);
      } else if (item instanceof List) {
        result.add(new MergeableResourcesImpl(codeResourcesModel, contentBeanFactory, dataViewFactory));
      } else {
        LOG.warn("Cannot handle " + item + " as code resource, ignore.");
      }
    }
    return result;
  }
}
