package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.links.SettingsBasedVanityUrlMapper;
import com.coremedia.blueprint.base.links.VanityUrlMapper;
import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.blueprint.common.contentbeans.CMCSS;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMCollection;
import com.coremedia.blueprint.common.contentbeans.CMHTML;
import com.coremedia.blueprint.common.contentbeans.CMJavaScript;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.layout.PageGridService;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cache.Cache;
import com.coremedia.cache.util.ObjectCacheKey;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.user.User;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Generated extension class for immutable beans of document type "CMChannel".
 */
public class CMChannelImpl extends CMChannelBase {
  private static final Logger LOG = LoggerFactory.getLogger(CMChannelImpl.class);

  private static final SettingsBasedVanityUrlMapper EMPTY_VANITY = new SettingsBasedVanityUrlMapper();

  private PageGridService pageGridService;
  private ThemeService themeService;
  private Cache cache;

  /**
   * If the header is empty, fallback to parent channel.
   */
  @Override
  public List<? extends Linkable> getHeader() {
    List<? extends Linkable> headers = filterItems(getHeaderUnfiltered());
    if (!headers.isEmpty()) {
      return headers;
    }
    CMChannel parent = getParentChannel();
    return parent == null ? Collections.<CMLinkable>emptyList() : parent.getHeader();
  }

  /**
   * public for dataview caching only //todo check alternatives
   */
  public List <? extends Linkable> getHeaderUnfiltered(){
    return super.getHeader();
  }

  /**
   * If the footer is empty, fallback to parent channel.
   */
  @Override
  public List<? extends Linkable> getFooter() {
    List<? extends Linkable> footers = filterItems(getFooterUnfiltered());
    if (!footers.isEmpty()) {
      return footers;
    }
    CMChannel parent = getParentChannel();
    return parent == null ? Collections.<CMLinkable>emptyList() : parent.getFooter();
  }

  /**
   * public for dataview caching only //todo check alternatives
   */
  public List <? extends Linkable> getFooterUnfiltered(){
    return super.getFooter();
  }

  /**
   * Return the channel's CSS.
   * <p>
   * Fallback to the parent channel if the channel has no CSS.
   */
  @Override
  public List<CMCSS> getCss() {
    List<CMCSS> css = super.getCss();
    if (!css.isEmpty()) {
      return css;
    }
    CMChannel parent = getParentChannel();
    return parent==null ? emptyList() : parent.getCss();
  }

  /**
   * Return the channel's JavaScript.
   * <p>
   * Fallback to the parent channel if the channel has no JavaScript.
   */
  @Override
  public List<CMJavaScript> getJavaScript() {
    List<CMJavaScript> js = super.getJavaScript();
    if (!js.isEmpty()) {
      return js;
    }
    CMChannel parent = getParentChannel();
    return parent==null ? emptyList() : parent.getJavaScript();
  }

  /**
   * Return the channel's Theme.
   * <p>
   * Fallback to the parent channel if the channel has no Theme.
   */
  @Override
  public CMTheme getTheme(@Nullable User developer) {
    // This would suffice for CMChannel ...
    // return createBeanFor(themeService.theme(getContent()), CMTheme.class);

    // ... but this is more convenient for developers,
    // it works also for alternative TreeRelations and thus spares overriding:
    List<Linkable> beans = Lists.reverse(treeRelation.pathToRoot(this));
    List<Content> contents = Lists.transform(beans, (Linkable l) -> l instanceof CMNavigation ? ((CMNavigation) l).getContent() : null);
    return createBeanFor(themeService.directTheme(contents, developer), CMTheme.class);
  }


  // --- internal ---------------------------------------------------

  /**
   * Fetch a parent channel to inherit missing properties from.
   * <p/>
   * If a channel has multiple parents, the chosen parent is somewhat
   * arbitrary, but deterministic: order by {@link #getContentId()}.
   *
   * @return parent channel or <code>null</code> if this is a root channel.
   */
  protected CMChannel getParentChannel() {
    Navigation parent = getParentNavigation();
    while (parent != null && !(parent instanceof CMChannel)) {
      parent = parent.getParentNavigation();
    }
    return (CMChannel) parent;
  }

  @Override
  public VanityUrlMapper getVanityUrlMapper() {
    if (isRoot()) {
      return cache==null ? createVanityUrlMapper() : cache.get(new VanityUrlMapperCacheKey());
    } else {
      // optimization, assume vanity URLs are only managed on the root channel
      return EMPTY_VANITY;
    }
  }

  private VanityUrlMapper createVanityUrlMapper() {
    return new SettingsBasedVanityUrlMapper(this, getSettingsService());
  }

  private class VanityUrlMapperCacheKey extends ObjectCacheKey<CMChannelImpl, VanityUrlMapper> {
    VanityUrlMapperCacheKey() {
      super(CMChannelImpl.this, CMChannelImpl.this::createVanityUrlMapper);
    }
  }

  @Override
  public String getFeedDescription() {
    return getFeedTitle();
  }

  @Override
  @NonNull
  public PageGrid getPageGrid() {
    return pageGridService.getContentBackedPageGrid(this);
  }

  /**
   * Ensures that only those items are flattened that
   * should be displayed in the navigation.
   * @param item The item to check the type for.
   * @return True, if the item should be displayed in the navigation.
   */
  private boolean isValidNavigationType(CMLinkable item) {
    ContentType contentType = item.getContent().getType();
    return !contentType.isSubtypeOf(CMMedia.NAME) && !contentType.isSubtypeOf(CMHTML.NAME);
  }

  @Required
  public void setPageGridService(PageGridService pageGridService) {
    this.pageGridService = pageGridService;
  }

  @Required
  public void setThemeService(ThemeService themeService) {
    this.themeService = themeService;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }

  @Override
  public List<? extends CMLinkable> getFeedItems() {
    return getItems();
  }

  @Override
  public List<? extends CMLinkable> getItemsFlattened() {
    List<CMLinkable> result = new ArrayList<>();
    flatten(result, getItems());
    return result;
  }

  @SuppressWarnings("unchecked")
  private List<? extends CMLinkable> getItems() {
    //TODO broaden to implements FeedSource<Object>
    return (List<? extends CMLinkable>) getPageGrid().getMainItems();
  }

  /**
   * Recursive search for items that can be displayed as navigation items.
   * @param result The filtered result list that contains the items to display in the navigation.
   * @param items The items of the current content bean.
   */
  private void flatten(List<CMLinkable> result, List<?> items) {
    for (Object item : items) {
      if (item instanceof CMCollection<?>) {
        //enter child
        final CMCollection<?> cmCollection = (CMCollection<?>) item;
        flatten(result, cmCollection.getItems());
      } else if (item instanceof CMLinkable) {
        final CMLinkable linkable = (CMLinkable) item;
        //filter item for valid types and exclude duplicates
        if (!result.contains(item) && isValidNavigationType(linkable)) {
          result.add(linkable);
        }
      }
    }
  }

  @Override
  @NonNull
  public List<CMMedia> getMedia() {
    return fetchMediaWithRecursionDetection(new HashSet<>());
  }

  @Override
  @NonNull
  public List<CMMedia> fetchMediaWithRecursionDetection(Collection<CMTeasable> visited) {
    // Recursion detection
    if (visited.contains(this)) {
      LOG.debug("Recursive lookup of media for {}", this);
      return Collections.emptyList();
    }
    visited.add(this);

    // Prefer own media
    List<CMMedia> media = super.getMedia();
    if (!media.isEmpty()) {
      return media;
    }

    // Fallback: media of some main item
    for (Object mainItem : getPageGrid().getMainItems()) {
      CMTeasable teasable = asTeasable(mainItem);
      if (teasable != null) {
        media = teasable.fetchMediaWithRecursionDetection(visited);
        if (!media.isEmpty()) {
          return media;
        }
      }
    }

    // Surrender
    return Collections.emptyList();
  }

  private CMTeasable asTeasable(Object obj) {
    if (obj instanceof Content) {
      Content content = (Content) obj;
      if (content.isInstanceOf(CMTeasable.NAME)) {
        return getContentBeanFactory().createBeanFor(content, CMTeasable.class);
      }
    }
    return null;
  }
}
