package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_SEGMENTS;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.VIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_NAVIGATION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_NAME;

@RequestMapping
@Link
public class PageHandler extends DefaultPageHandler {

  /**
   * Pattern for URLs to content, matching 1..n navigation path segments between the prefix and the content name.
   * e.g. /media/travel/europe/england/knowing-all-about-london-1234
   */
  public static final String SEO_FRIENDLY_URI_PATTERN =
          "/{" + SEGMENTS_NAVIGATION + ":" + PATTERN_SEGMENTS + "}" +
                  "/{" + SEGMENT_NAME + "}" +
                  "-{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}";

  /**
   * Pattern for URLs to navigation nodes or content with a vanity URL mapping, consisting of 1..n navigation path
   * segments or the root segment and 0..n vanity URL segments.
   * e.g. /media/sports
   */
  public static final String URI_PATTERN_VANITY =
          "/{" + SEGMENTS_NAVIGATION + ":" + PATTERN_SEGMENTS + "}";

  @GetMapping(SEO_FRIENDLY_URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ID) CMLinkable linkable,
                                    @PathVariable(SEGMENT_ID) String segmentId,
                                    @PathVariable(SEGMENTS_NAVIGATION) List<String> navigationPath,
                                    @PathVariable(SEGMENT_NAME) String vanity,
                                    @RequestParam(value = VIEW_PARAMETER, required = false) String view,
                                    HttpServletRequest servletRequest) {
    return handleRequestInternal(linkable, segmentId, navigationPath, vanity, view, servletRequest);
  }

  /**
   * Handles a request for a vanity URL containing a root segment and two additional segment, e.g. /sports/football/results/recent
   */
  @GetMapping({URI_PATTERN_VANITY, URI_PATTERN_VANITY + '/'})
  public ModelAndView handleRequest(@PathVariable(SEGMENTS_NAVIGATION) List<String> navigationPath,
                                    @RequestParam(value = VIEW_PARAMETER, required = false) String view,
                                    HttpServletRequest servletRequest) {
    return handleRequestInternal(navigationPath, view, servletRequest);
  }

  @SuppressWarnings("unused")
  @Link(type = CMTaxonomy.class)
  @Nullable
  public UriComponents buildLinkForTaxonomy(
          @NonNull CMTaxonomy taxonomy,
          @Nullable String viewName,
          @NonNull Map<String, Object> linkParameters) {
    UriComponentsBuilder ucb = buildLinkForTaxonomyInternal(taxonomy, viewName, linkParameters).orElse(null);
    return ucb==null ? null : ucb.build();
  }

  @SuppressWarnings("unused")
  @Link(type = CMLinkable.class)
  @Nullable
  public UriComponents buildLinkForLinkable(
          @NonNull CMLinkable linkable,
          @Nullable String viewName,
          @NonNull Map<String, Object> linkParameters) {
    UriComponentsBuilder ucb = buildLinkForLinkableInternal(linkable, viewName, linkParameters).orElse(null);
    return ucb==null ? null : ucb.build();
  }
}
