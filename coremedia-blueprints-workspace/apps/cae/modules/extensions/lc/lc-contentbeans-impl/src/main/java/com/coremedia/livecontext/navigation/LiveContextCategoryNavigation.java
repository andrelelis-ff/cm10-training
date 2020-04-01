package com.coremedia.livecontext.navigation;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cae.aspect.provider.AspectsProvider;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.user.User;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.util.Assert.isInstanceOf;
import static org.springframework.util.Assert.notNull;

/**
 * A LiveContextNavigation which is backed by a category at runtime.
 * Not persisted in the CMS repository.
 */
public class LiveContextCategoryNavigation implements LiveContextNavigation {
  private LiveContextNavigationTreeRelation treeRelation;
  private Category category;
  private Site site;


  // --- Construction -----------------------------------------------

  public LiveContextCategoryNavigation(@NonNull Category category,
                                       @NonNull Site site,
                                       @NonNull LiveContextNavigationTreeRelation treeRelation) {
    notNull(category);
    notNull(site);
    notNull(treeRelation);

    this.category = category;
    this.site = site;
    this.treeRelation = treeRelation;
  }


  // --- LiveContextNavigation --------------------------------------

  @NonNull
  @Override
  public Category getCategory() {
    return category;
  }

  @NonNull
  @Override
  public Site getSite() {
    return site;
  }


  // --- Navigation -------------------------------------------------

  @Override
  public List<? extends Linkable> getChildren() {
    Collection<Linkable> children = treeRelation.getChildrenOf(this);
    return (List<? extends Linkable>) children;
  }

  @Override
  public Navigation getParentNavigation() {
    Linkable parentOf = treeRelation.getParentOf(this);
    return parentOf instanceof Navigation ? (Navigation) parentOf : null;
  }

  @Override
  public CMTheme getTheme(@Nullable User developer) {
    return getContext().getTheme(developer);
  }

  @Override
  public CMNavigation getRootNavigation() {
    List<Linkable> navigationPath = treeRelation.pathToRoot(this);
    if (navigationPath.isEmpty()) {
      return null;
    }

    Linkable rootNavigation = navigationPath.get(0);
    isInstanceOf(CMNavigation.class, rootNavigation);
    return (CMNavigation) rootNavigation;
  }

  @Override
  public CMContext getContext() {
    CMExternalChannel externalChannel = treeRelation.getNearestExternalChannelForCategory(getCategory(), getSite());
    if (externalChannel != null) {
      return externalChannel;
    }
    // after no adequate external page could be found (not even recursively along the category path)...
    // return the catalog root page, whose position is the second in the navigation path list. Or...
    List<? extends Linkable> pathToRoot = getNavigationPathList();
    if (pathToRoot.size() > 1 && pathToRoot.get(1) instanceof CMContext) {
      return (CMContext) pathToRoot.get(1);
    }
    // after no catalog root page is found take the site root node
    if (!pathToRoot.isEmpty() && pathToRoot.get(0) instanceof CMContext) {
      return (CMContext) pathToRoot.get(0);
    }
    return null;
  }

  @Override
  @NonNull
  public List<? extends Linkable> getNavigationPathList() {
    return treeRelation.pathToRoot(this);
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public List<? extends Linkable> getVisibleChildren() {
    return getChildren();
  }

  @Override
  public boolean isHiddenInSitemap() {
    return false;
  }

  @Override
  public List<? extends Linkable> getSitemapChildren() {
    return getChildren();
  }

  @Override
  public String getTitle() {
    return category.getName();
  }

  @Override
  public String getSegment() {
    String seoSegment = category.getSeoSegment();
    return isEmpty(seoSegment) ? category.getExternalId() : seoSegment;
  }

  @Override
  public String getKeywords() {
    return category.getName() + " " + category.getSeoSegment() + " " + category.getShortDescription();
  }

  @Override
  public boolean isRoot() {
    return getParentNavigation() == null;
  }

  @Override
  public Locale getLocale() {
    return category.getLocale();
  }

  @Override
  public String getViewTypeName() {
    return null;
  }

  @Override
  public AspectsProvider getAspectsProvider() {
    //todo introduced to make Elastic Social Plugin work again - revise or remove aspects
    if (getParentNavigation() != null) {
      return getParentNavigation().getAspectsProvider();
    }
    return null;
  }

  @Override
  public TreeRelation<Content> getCodeResourcesTreeRelation() {
    return treeRelation.getContentTreeRelation();
  }

  @Override
  public String toString() {
    return getClass().getName() + "{" +
            "context=" + getContext().getContent().getPath() +
            ", category=" + getCategory().getName() +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LiveContextCategoryNavigation that = (LiveContextCategoryNavigation) o;
    return category.equals(that.category) && site.equals(that.site);
  }

  @Override
  public int hashCode() {
    int result = category.hashCode();
    result = 31 * result + site.hashCode();
    return result;
  }
}
