package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.common.contentbeans.BelowRootNavigation;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.feeds.FeedSource;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;

/**
 * Handles RSS documents for {@link com.coremedia.blueprint.common.contentbeans.Page}s
 */
@Link
@RequestMapping
public class PageRssHandler extends PageHandlerBase {

  private static final Logger LOG = LoggerFactory.getLogger(PageRssHandler.class);

  private static final String VIEW_RSS = "asFeed";

  private static final String SEGMENT_RSS = "rss";
  private static final String SEGMENT_TAXONOMY_ID = "taxId";
  private static final String SUFFIX_RSS = "feed.rss";

  /**
   * Pattern for RSS feeds
   * e.g. /service/rss/media/{channelId}/feed.rss
   */
  private static final String URI_PATTERN_RSS =
          "/" + PREFIX_SERVICE +
                  "/" + SEGMENT_RSS +
                  "/{" + SEGMENT_ROOT + "}" +
                  "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
                  "/" + SUFFIX_RSS;
  /**
   * Pattern for topic page RSS feeds:
   * e.g. /service/rss/media/{topicChannelId}/{taxonomyId}/feed.rss
   */
  private static final String URI_PATTERN_RSS_TOPICPAGE =
          "/" + PREFIX_SERVICE +
                  "/" + SEGMENT_RSS +
                  "/{" + SEGMENT_ROOT + "}" +
                  "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
                  "/{" + SEGMENT_TAXONOMY_ID + ":" + PATTERN_NUMBER + "}" +
                  "/" + SUFFIX_RSS;

  // --- Handlers ------------------------------------------------------------------------------------------------------

  /**
   * Handles a RSS request for a page
   * and provides a {@link com.coremedia.blueprint.common.feeds.FeedSource}
   *
   * @see com.coremedia.blueprint.cae.view.FeedView
   */
  @GetMapping(value = URI_PATTERN_RSS)
  public ModelAndView handleRss(@PathVariable(SEGMENT_ID) ContentBean contentBean,
                                @PathVariable(SEGMENT_ROOT) String rootSegment) {
    if (isSuitableFeedSource(contentBean)) {
      CMLinkable feedSource = (CMLinkable) contentBean;
      // URL validation: root segment in URL must match first element of navigation path
      // Note that CMNavigation is also a FeedSource!
      String actualRootSegment = getRootSegment(feedSource);
      if (rootSegment.equals(actualRootSegment)) {
        return HandlerHelper.createModelWithView(contentBean, VIEW_RSS);
      }
    }
    return HandlerHelper.notFound();
  }

  /**
   * Handles RSS requests for a topic page
   * The first number is the ID of the topic page, the second is the id of the taxonomy.
   */
  @GetMapping(value = URI_PATTERN_RSS_TOPICPAGE)
  public ModelAndView handleRssTopicPage(@PathVariable(SEGMENT_ID) ContentBean contentBean,
                                         @PathVariable(SEGMENT_TAXONOMY_ID) ContentBean taxonomyBean,
                                         @PathVariable(SEGMENT_ROOT) String rootSegment) {
    if (isSuitableFeedSource(contentBean) && taxonomyBean != null) {
      CMLinkable feedSource = (CMLinkable) contentBean;
      // URL validation: root segment in URL must match first element of navigation path
      // Note that CMNavigation is also a FeedSource!
      String actualRootSegment = getRootSegment(feedSource);
      RequestAttributeConstants.setPageModel(taxonomyBean);
      if (rootSegment.equals(actualRootSegment)) {
        return HandlerHelper.createModelWithView(contentBean, VIEW_RSS);
      }
    }
    return HandlerHelper.notFound();
  }

  // --- LinkSchemes ---------------------------------------------------------------------------------------------------

  /**
   * Builds a link on a {@link CMNavigation} to be rendered as RSS
   */
  @Link(type = FeedSource.class, view = VIEW_RSS, uri = URI_PATTERN_RSS)
  public Map<String, ?> buildLink(FeedSource feedSource, HttpServletRequest request) {
    if (isSuitableFeedSource(feedSource)) {
      CMLinkable source = (CMLinkable) feedSource;
      CMNavigation context = (CMNavigation) getNavigation(source);
      String rootSegment = getRootSegment(context);
      if (rootSegment != null) {
        return ImmutableMap.of(SEGMENT_ID, getId(source), SEGMENT_ROOT, rootSegment);
      }
    }
    return null;
  }

  /**
   * Builds a link on a {@link CMTaxonomy} to be rendered as RSS (Topic Page)
   */
  @Link(type = CMTaxonomy.class, view = VIEW_RSS, uri = URI_PATTERN_RSS_TOPICPAGE)
  public Map<String, ?> buildTaxonomyLink(CMTaxonomy taxonomy, HttpServletRequest request) {
    CMNavigation context = (CMNavigation) getNavigation(taxonomy);
    if (isSuitableFeedSource(context)) {
      String rootSegment = getRootSegment(context);
      if (rootSegment != null) {
        return ImmutableMap.of(SEGMENT_ID, getId(context), SEGMENT_ROOT, rootSegment, SEGMENT_TAXONOMY_ID, getId(taxonomy));
      }
    }
    LOG.error("Content has no navigation context, cannot build link for {}", taxonomy);
    return null;
  }


  // --- internal ---------------------------------------------------

  /**
   * For historical reasons, we cannot make CMLinkable extend FeedSource
   * or vice versa.  But we need both for URL handling.
   */
  private static boolean isSuitableFeedSource(Object feedSource) {
    return feedSource instanceof FeedSource && feedSource instanceof CMLinkable;
  }

  private static String getRootSegment(BelowRootNavigation context) {
    Collection<? extends Navigation> navigations = context.getRootNavigations();
    return navigations.isEmpty() ? null : navigations.iterator().next().getSegment();
  }
}
