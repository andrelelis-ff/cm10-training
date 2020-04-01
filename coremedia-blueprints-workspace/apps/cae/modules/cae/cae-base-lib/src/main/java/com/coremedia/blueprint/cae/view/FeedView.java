package com.coremedia.blueprint.cae.view;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.feeds.FeedItemDataProvider;
import com.coremedia.blueprint.common.contentbeans.CMCollection;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.feeds.FeedFormat;
import com.coremedia.blueprint.common.feeds.FeedSource;
import com.coremedia.cap.multisite.ContentObjectSiteAspect;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.view.ServletView;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.google.common.collect.ImmutableMap;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEnclosureImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static java.util.stream.Collectors.toList;

/**
 * A programmatic view that generates feeds for channels.
 */
public class FeedView implements ServletView {

  private static final Logger LOG = LoggerFactory.getLogger(FeedView.class);

  public static final String RSS_LIMIT = "RSS.limit";
  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final Map<FeedFormat, String> FEED_FORMAT_MAPPING = ImmutableMap.<FeedFormat, String>builder()
          .put(FeedFormat.Rss_2_0, "rss_2.0")
          .put(FeedFormat.Atom_1_0, "atom_1.0")
          .build();

  private SitesService sitesService;
  private LinkFormatter linkFormatter;
  private SettingsService settingsService;

  private List<FeedItemDataProvider> feedItemDataProviders;

  private int feedItemLimit = 0;

  /**
   * the feed item providers configured via spring
   *
   * @param feedItemDataProviders the feed item providers configured via spring
   */
  @Required
  public void setFeedItemDataProviders(List<FeedItemDataProvider> feedItemDataProviders) {
    this.feedItemDataProviders = feedItemDataProviders;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  /**
   * Sets the maximum number of items that will be fed. Default is 0. If set to 0, no limit is set.
   *
   * @param feedItemLimit the limit
   */
  public void setFeedItemLimit(int feedItemLimit) {
    this.feedItemLimit = feedItemLimit;
  }

  /**
   * setter for spring configuration link formatter
   *
   * @param linkFormatter setter for spring configuration link formatter
   */
  @Required
  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }

  /**
   * Render the feed for the given feedable bean
   *
   * @param bean     the feedable bean to generate the rss-feed for
   * @param view     not used at the moment
   * @param request  the http-request of the user
   * @param response the http-response of the user
   */
  @Override
  public void render(Object bean, String view, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    if (bean == null) {
      throw new IllegalArgumentException("bean");
    }

    if (!(bean instanceof FeedSource)) {
      throw new IllegalArgumentException(bean + " is no " + FeedSource.class);
    }

    FeedSource feedSource = (FeedSource) bean;
    String feedFormat = FEED_FORMAT_MAPPING.get(feedSource.getFeedFormat());
    if (feedFormat == null) {
      throw new IllegalArgumentException("Unsupported output format: " + feedSource.getFeedFormat());
    }

    renderFeedable(feedSource, feedFormat, request, response);
  }

  private void renderFeedable(@NonNull FeedSource feedSource, @NonNull String feedFormat,
                              @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
    try {
      response.setContentType("application/rss+xml");
      SyndFeed feed = new SyndFeedImpl();
      feed.setFeedType(feedFormat);
      setFeedMetaData(request, response, feedSource, feed);

      List<CMLinkable> syndicatedContent = getSyndicationContent(feedSource);

      findLanguage(syndicatedContent).ifPresent(feed::setLanguage);

      List<SyndEntry> entries = getEntries(syndicatedContent, request, response);
      feed.getEntries().addAll(entries);

      if (feedFormat.equals(FEED_FORMAT_MAPPING.get(FeedFormat.Atom_1_0))) {
        convertFromRssToAtom(feed);
      }

      SyndFeedOutput output = new SyndFeedOutput();
      output.output(feed, response.getWriter());
    } catch (IOException | FeedException e) {
      LOG.error("An error occured while rendering the RSS Feed.", e);
    }
  }

  @NonNull
  private List<SyndEntry> getEntries(@NonNull List<CMLinkable> syndicatedContent, @NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response) {
    return syndicatedContent.stream()
            .map(linkable -> findEntry(linkable, request, response))
            .flatMap(Optional::stream)
            .collect(toList());
  }

  @NonNull
  private Optional<SyndEntry> findEntry(@NonNull CMLinkable linkable, @NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response) {
    return selectProvider(linkable)
            .map(feedItemDataProvider -> feedItemDataProvider.getSyndEntry(request, response, linkable));
  }

  @NonNull
  private Optional<FeedItemDataProvider> selectProvider(@NonNull CMLinkable linkable) {
    return feedItemDataProviders.stream()
            .filter(provider -> provider.isSupported(linkable))
            .findFirst();
  }

  /**
   * Get the feed language
   *
   * @param items the items to retrieve the language from
   * @return the first language found by searching the items, if any
   */
  @NonNull
  private Optional<String> findLanguage(@NonNull List<CMLinkable> items) {
    return items.stream()
            .map(ContentBean::getContent)
            .map(sitesService::getContentSiteAspect)
            .map(ContentObjectSiteAspect::getLocale)
            .filter(locale -> !locale.getLanguage().isEmpty())
            .map(locale -> {
              String language = locale.getLanguage();
              String country = locale.getCountry();
              return !country.isEmpty() ? language + '-' + country : language;
            })
            .findFirst();
  }

  private void convertFromRssToAtom(@NonNull SyndFeed feed) {
    for (SyndEntry entry : feed.getEntries()) {
      if (entry.getModules().size() > 1) {
        MediaEntryModule mediaModule = (MediaEntryModule) entry.getModules().get(1);
        for (MediaContent mediaContent : mediaModule.getMediaContents()) {
          entry.getEnclosures().add(getEnclosureFromMediaContent(mediaContent));
        }
        entry.setModules(null);
      }
    }
  }

  @NonNull
  private SyndEnclosure getEnclosureFromMediaContent(@NonNull MediaContent mediaContent) {
    SyndEnclosure syndEnclosure = new SyndEnclosureImpl();
    syndEnclosure.setUrl(mediaContent.getReference().toString());
    syndEnclosure.setLength(mediaContent.getFileSize());
    syndEnclosure.setType(mediaContent.getType());
    return syndEnclosure;
  }

  private void setFeedMetaData(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                               @NonNull FeedSource feedSource, @NonNull SyndFeed feed) {
    Object taxonomy = request.getAttribute(RequestAttributeConstants.ATTR_NAME_PAGE_MODEL);
    String feedTitle = taxonomy instanceof CMTaxonomy ? ((CMTaxonomy) taxonomy).getValue() : feedSource.getFeedTitle();
    feed.setTitle(StringUtils.isNotBlank(feedTitle) ? feedTitle : StringUtils.EMPTY);

    String feedDescription = feedSource.getFeedDescription();
    feed.setDescription(StringUtils.isNotBlank(feedDescription) ? feedDescription : StringUtils.EMPTY);

    // enforce rendering of absolute URLs
    request.setAttribute(ABSOLUTE_URI_KEY, true);
    String link = linkFormatter.formatLink(feedSource, null, request, response, false);

    feed.setLink(link);
    feed.setUri(link);
    feed.setEncoding(DEFAULT_ENCODING);
  }

  @NonNull
  private List<CMLinkable> getSyndicationContent(@NonNull FeedSource feedSource) {
    List<CMLinkable> contents = new ArrayList<>();
    for (Object item : feedSource.getFeedItems()) {
      if (item instanceof CMLinkable) {
        if (item instanceof CMCollection) {
          //noinspection unchecked
          contents.addAll(((CMCollection) item).getItems());
        } else {
          contents.add((CMLinkable) item);
        }
      } else {
        LOG.warn("ignoring syndication content item {} of non-linkable type {}", item, item != null ? item.getClass() : null);
      }
    }
    int limit = feedItemLimit;
    /*
     The linked object might have a setting attached that specifies a maximum number
     of articles that shall be displayed. If so, extract and apply!
     */
    Object limitObj = settingsService.setting(RSS_LIMIT, Object.class, feedSource);
    if (limitObj != null && limitObj instanceof String) {
      limit = Integer.parseInt((String) limitObj);
    }

    if (limit > 0 && limit < contents.size()) {
      contents = contents.subList(0, limit);
    }
    return contents;
  }
}
