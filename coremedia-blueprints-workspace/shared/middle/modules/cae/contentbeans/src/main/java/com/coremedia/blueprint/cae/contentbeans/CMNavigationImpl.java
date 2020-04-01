package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.feeds.FeedFormat;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Generated extension class for immutable beans of document type "CMNavigation".
 */
public abstract class CMNavigationImpl extends CMNavigationBase {

  protected TreeRelation<Linkable> treeRelation;
  private TreeRelation<Content> codeResourcesTreeRelation;

  // --- construction -----------------------------------------------

  @Required
  public void setTreeRelation(TreeRelation<Linkable> treeRelation) {
    this.treeRelation = treeRelation;
  }

  @Required
  public void setCodeResourcesTreeRelation(TreeRelation<Content> codeResourcesTreeRelation) {
    this.codeResourcesTreeRelation = codeResourcesTreeRelation;
  }

  // --- CMNavigation -----------------------------------------------

  @Override
  public List<? extends Linkable> getChildren() {
    List<? extends Linkable> childrenUnfiltered = getChildrenUnfiltered();
    return filterItems(childrenUnfiltered);
  }

  @SuppressWarnings("unchecked")
  public List<? extends Linkable> getChildrenUnfiltered() {
    return (List<? extends Linkable>) treeRelation.getChildrenOf(this);
  }

  /**
   * Filter items using the {@link #getValidationService()}
   *
   * @param itemsUnfiltered the list of unfiltered items (not necessarily instances of CMLinkable)
   * @return a list of items that have passed validation
   */
  @SuppressWarnings("unchecked")
  protected List<? extends Linkable> filterItems(List<? extends Linkable> itemsUnfiltered) {
    return getValidationService().filterList(itemsUnfiltered);
  }

  @Override
  public List<? extends Linkable> getVisibleChildren() {
    List<? extends Linkable> allChildren = getChildren();
    List<Linkable> visible = new ArrayList<>();
    for (Linkable child : allChildren) {
      if (!(child instanceof Navigation) || !((Navigation)child).isHidden()) {
        visible.add(child);
      }
    }
    return visible;
  }

  /**
   * Returns the CMNavigation items of {@link #getVisibleChildren()}
   * which are not {@link #isHiddenInSitemap()}.
   *
   * @return the children for a sitemap
   */
  @Override
  public List<? extends Linkable> getSitemapChildren() {
    List<? extends Linkable> allChildren = getVisibleChildren();
    List<Linkable> visible = new ArrayList<>();
    for (Linkable child : allChildren) {
      if (!(child instanceof Navigation) || !((Navigation)child).isHiddenInSitemap()) {
        visible.add(child);
      }
    }
    return visible;
  }

  @Override
  public List<? extends Linkable> getNavigationPathList() {
    return treeRelation.pathToRoot(this);
  }

  @Override
  public boolean isRoot() {
    return treeRelation.isRoot(this);
  }

  @Override
  public CMNavigation getRootNavigation() {
    List<? extends Linkable> linkables = treeRelation.pathToRoot(this);
    return (CMNavigation) linkables.get(0);
  }

  @Override
  public Collection<? extends Navigation> getRootNavigations() {
    return isRoot() ? Collections.singletonList(this) : super.getRootNavigations();
  }

  @Override
  public Navigation getParentNavigation() {
    return (Navigation) treeRelation.getParentOf(this);
  }

  @Override
  public TreeRelation<Content> getCodeResourcesTreeRelation() {
    return codeResourcesTreeRelation;
  }


  // --- FeedSource -------------------------------------------------

  @Override
  public String getFeedTitle() {
    return StringUtils.isNotBlank(getTitle()) ? getTitle() : StringUtils.EMPTY;
  }

  @Override
  public FeedFormat getFeedFormat() {
    FeedFormat configuredFeedFormat = FeedFormat.Rss_2_0;
    // determine the target feed format
    // RSS is the default format
    String formatSetting = getSettingsService().settingWithDefault("site.rss.format", String.class, FeedFormat.Rss_2_0.toString(), this);
    for (FeedFormat format : FeedFormat.values()) {
      if (format.toString().equals(formatSetting)) {
        configuredFeedFormat = format;
        break;
      }
    }
    return configuredFeedFormat;
  }
}
