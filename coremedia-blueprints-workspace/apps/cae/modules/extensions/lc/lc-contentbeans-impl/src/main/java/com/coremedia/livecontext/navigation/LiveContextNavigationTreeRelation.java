package com.coremedia.livecontext.navigation;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.ContentObjectSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.tree.ExternalChannelContentTreeRelation;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Collections.emptyList;

/**
 * Tree relation implementation of the live context.
 * The live context tree relation resolves content beans of type CMExternalChannel
 * and applies the matching category subtree as navigation tree to the given navigation.
 * <p/>
 * The live context categories are wrapped into LiveContextNavigation instances that includes
 * this tree relation for resolving additional sub children that are live context categories.
 */
public class LiveContextNavigationTreeRelation implements TreeRelation<Linkable> {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiveContextNavigationTreeRelation.class);

  private ExternalChannelContentTreeRelation delegate;
  private SitesService sitesService;
  private ContentBeanFactory contentBeanFactory;
  private LiveContextNavigationFactory navigationFactory;

  @Override
  @NonNull
  public Collection<Linkable> getChildrenOf(Linkable parent) {
    Collection<Linkable> navigationList = new ArrayList<>();
    Site site = getSite(parent);
    if (parent instanceof LiveContextNavigation && site != null) {
      Category parentCategory = ((LiveContextNavigation) parent).getCategory();
      if (parentCategory != null) {
        //we deal with a category already, so we simply resolve the categories children
        for (Category child : parentCategory.getChildren()) {
          Linkable navigation = navigationFactory.createNavigation(child, site);
          navigationList.add(navigation);
        }
      }
    }
    return navigationList;
  }

  @Override
  public Linkable getParentOf(Linkable child) {
    if (child instanceof LiveContextNavigation) {
      LiveContextNavigation navigation = (LiveContextNavigation) child;
      Category category = navigation.getCategory();
      if(null != category) {
        Category parent = category.getParent();
        if (null != parent) {
          return navigationFactory.createNavigation(parent, ((LiveContextNavigation) child).getSite());
        }
      }
      final Site site = navigation.getSite();
      return contentBeanFactory.createBeanFor(site.getSiteRootDocument(), Linkable.class);
    }
    return null;
  }

  CMExternalChannel getNearestExternalChannelForCategory(@Nullable Category category, @Nullable Site site) {
    if (category != null && site != null) {
      Content nearestExternalChannelForCategory = delegate.getNearestContentForCategory(category, site);
      return nearestExternalChannelForCategory != null ?
        contentBeanFactory.createBeanFor(nearestExternalChannelForCategory, CMExternalChannel.class) : null;
    }
    return null;
  }

  TreeRelation<Content> getContentTreeRelation() {
    return delegate;
  }

  @Override
  public Linkable getParentUnchecked(Linkable child) {
    return getParentOf(child);
  }

  @Override
  @NonNull
  public List<Linkable> pathToRoot(final Linkable child) {
    if (child instanceof LiveContextNavigation) {
      LinkedList<Linkable> result = newLinkedList();

      LiveContextNavigation navigation = (LiveContextNavigation) child;
      final Site site = navigation.getSite();
      result.add(contentBeanFactory.createBeanFor(site.getSiteRootDocument(), Linkable.class));

      Category category = navigation.getCategory();
      if (category == null) {
        LOGGER.debug("Category for navigation {} is null", navigation);
        return emptyList();
      }

      List<Category> breadcrumb = category.getBreadcrumb();
      result.addAll(Lists.transform(breadcrumb, new CategoryToLiveContextNavigationTransformer(site)));
      LOGGER.trace("path to root for {}: {}", child, result);
      return result;
    }
    return emptyList();
  }

  @Override
  public boolean isRoot(Linkable item) {
    return getParentOf(item) == null;
  }

  @Override
  public boolean isApplicable(Linkable item) {
    return item instanceof CMExternalChannel;
  }

  private Site getSite(Linkable linkable) {
    if (linkable instanceof LiveContextNavigation) {
      return ((LiveContextNavigation) linkable).getSite();
    } else if (linkable instanceof ContentBean) {
      ContentObjectSiteAspect siteAspect = sitesService.getSiteAspect(((ContentBean)linkable).getContent());
      return siteAspect.getSite();
    }
    return null;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setNavigationFactory(LiveContextNavigationFactory navigationFactory) {
    this.navigationFactory = navigationFactory;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setDelegate(ExternalChannelContentTreeRelation delegate) {
    this.delegate = delegate;
  }

  private class CategoryToLiveContextNavigationTransformer implements Function<Category, Linkable> {
    private final Site site;

    CategoryToLiveContextNavigationTransformer(Site site) {
      this.site = site;
    }

    @Nullable
    @Override
    public Linkable apply(@Nullable Category input) {
      if(null != input) {
        return navigationFactory.createNavigation(input, site);
      }
      return null;
    }
  }
}
