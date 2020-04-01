package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.coremedia.cap.common.IdHelper.formatContentId;

/**
 * External Content resolver for 'externalRef' values "cm-seosegment:segment/path[/seotitle-<contentid>]"
 */
public class SeoSegmentExternalReferenceResolver extends ExternalReferenceResolverBase {

  private static final Logger LOG = LoggerFactory.getLogger(SeoSegmentExternalReferenceResolver.class);

  private static final String SEO_SEGMENT_PREFIX = "cm-seosegment:";
  private static final String ID_DELIMITER = "-";
  private static final String PATH_DELIMITER = "--";

  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;
  private UrlPathFormattingHelper urlPathFormattingHelper;

  public SeoSegmentExternalReferenceResolver() {
    super(SEO_SEGMENT_PREFIX);
  }

  @Nullable
  @Override
  protected LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters,
                                                     @NonNull String referenceInfo,
                                                     @NonNull Site site) {

    Content siteRootDocument = site.getSiteRootDocument();
    if (siteRootDocument == null) {
      LOG.warn("No site root document found for site: {}", site);
      return null;
    }

    String decodedSegmentPath = decode(referenceInfo);

    // try to resolve rest of path as vanity URL: this will be null, if there is no vanity mapping
    String rootSegment = urlPathFormattingHelper.getVanityName(siteRootDocument);
    CMChannel rootChannel = (CMChannel) navigationSegmentsUriHelper.parsePath(Collections.singletonList(rootSegment));
    //handle the case when the vanity consists of multiple paths. We have to translate the internal delimiter back to '/'
    String vanity = decodedSegmentPath.replaceAll(PATH_DELIMITER, "/");
    CMLinkable target = (CMLinkable) rootChannel.getVanityUrlMapper().forPattern(vanity);
    if (target != null) {
      // vanity URL found: determine the context for the target in the current site
      CMContext context = contextHelper.findAndSelectContextFor(rootChannel, target);
      Content content = context == null ? siteRootDocument : context.getContent();
      Content linkable = target.getContent();
      LOG.debug("vanity URL found for {} pointing to {}", vanity, linkable);
      return new LinkableAndNavigation(linkable, content);
    }

    List<String> segmentList = toSegmentList(rootSegment, decodedSegmentPath);

    int indexOfDelimiter = decodedSegmentPath.lastIndexOf(ID_DELIMITER);
    LinkableAndNavigation linkableAndNavigation = resolveNavigation(segmentList, indexOfDelimiter > 0);
    // if we didn't get a navigation, we cannot
    if (linkableAndNavigation.getNavigation() == null) {
      return linkableAndNavigation;
    }

    if (linkableAndNavigation.getLinkable() != null) {
      return linkableAndNavigation;
    }

    // we got a navigation but no linkable, let's try to resolve the linkable by ID
    Content navigation = linkableAndNavigation.getNavigation();

    Content content = getContentById(decodedSegmentPath, indexOfDelimiter);
    return new LinkableAndNavigation(content, navigation);
  }

  /**
   * check if #decodedSegmentPath is a valid content id
   * @return a valid content
   */
  @Nullable
  private Content getContentById(@NonNull String decodedSegmentPath, int indexOfDelimiter) {
    String potentialContentIdStr = decodedSegmentPath.substring(indexOfDelimiter + 1);
    if (!StringUtils.isNumeric(potentialContentIdStr)) {
      return null;
    }

    LOG.debug("trying to lookup content by numeric id {}", potentialContentIdStr);
    Content content = contentRepository.getContent(formatContentId(potentialContentIdStr));
    LOG.debug("numeric id {} resolved to {}", potentialContentIdStr, content);

    // validate content
    if (content != null && isInvalidLinkable(content)) {
      return null;
    }
    return content;
  }

  /**
   *
   * @param content
   * @return
   */
  private boolean isInvalidLinkable(@NonNull Content content) {
    ContentType type = content.getType();
    return !type.isSubtypeOf(CMLinkable.NAME) || type.isSubtypeOf(CMContext.NAME);
  }

  @NonNull
  private List<String> toSegmentList(@NonNull String rootSegment, String decodedSegmentPath) {
    List<String> segmentList = new ArrayList<>();
    segmentList.add(rootSegment);
    if (StringUtils.isNotBlank(decodedSegmentPath)) {
      segmentList.addAll(Arrays.asList(decodedSegmentPath.split(PATH_DELIMITER)));
    }
    return segmentList;
  }

  /**
   * Resolve linkable and navigation ignoring potential content ID in last segment
   * @return linkable and navigation where linkable is null if #hasIdDelimiter is true and last segment didn't resolve to a navigation content
   */
  @NonNull
  private LinkableAndNavigation resolveNavigation(@NonNull List<String> segmentList, boolean hasIdDelimiter) {
    Content navigation = resolveNavigation(segmentList);
    if (navigation == null && hasIdDelimiter) {
      // last segment path might be a Linkable with concrete content ID
      // first try to get parent navigation (without potential linkable path segment)
      final List<String> parentSegmentList = segmentList.subList(0, segmentList.size() - 1);
      navigation = resolveNavigation(parentSegmentList);
      return new LinkableAndNavigation(null, navigation);
    } else {
      // URL denotes a Navigation item without numeric ID, also use it as linkable
      return new LinkableAndNavigation(navigation, navigation);
    }
  }

  private Content resolveNavigation(List<String> segmentList) {
    Navigation navigation = navigationSegmentsUriHelper.parsePath(segmentList);
    if (navigation != null) {
      LOG.debug("found navigation content {} for segment list {}", navigation, segmentList);
      return navigation.getContext().getContent();
    }
    LOG.debug("no navigation content found for segment list {}", segmentList);
    return null;
  }

  @NonNull
  private static String decode(@NonNull String str) {
    try {
      return URLDecoder.decode(str, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      LOG.trace("Cannot decode string {}", str, e);
    }
    return str;
  }

  @Required
  public void setNavigationSegmentsUriHelper(NavigationSegmentsUriHelper navigationSegmentsUriHelper) {
    this.navigationSegmentsUriHelper = navigationSegmentsUriHelper;
  }

  @Required
  public void setUrlPathFormattingHelper(UrlPathFormattingHelper urlPathFormattingHelper) {
    this.urlPathFormattingHelper = urlPathFormattingHelper;
  }
}
