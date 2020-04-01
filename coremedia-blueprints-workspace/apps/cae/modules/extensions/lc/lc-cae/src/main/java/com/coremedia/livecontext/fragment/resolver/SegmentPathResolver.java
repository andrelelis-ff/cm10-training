package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.cae.search.SegmentResolver;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Objects;

public class SegmentPathResolver extends ExternalReferenceResolverBase {

  public static final String SEGMENTPATH_PARAM_PREFIX = "cm-segmentpath:";

  private static final String SEGMENT_PATH_SEPARATOR = "!";
  private static final Splitter SEGMENT_PATH_SPLITTER = Splitter.on(SEGMENT_PATH_SEPARATOR).omitEmptyStrings();

  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;
  private SegmentResolver segmentResolver;

  public SegmentPathResolver() {
    super(SEGMENTPATH_PARAM_PREFIX);
  }

// --- configuration ----------------------------------------------

  @Required
  public void setNavigationSegmentsUriHelper(NavigationSegmentsUriHelper navigationSegmentsUriHelper) {
    this.navigationSegmentsUriHelper = navigationSegmentsUriHelper;
  }

  @Required
  public void setSegmentResolver(@NonNull SegmentResolver segmentResolver) {
    Objects.requireNonNull(segmentResolver);
    this.segmentResolver = segmentResolver;
  }

  // --- interface --------------------------------------------------

  @Nullable
  @Override
  protected LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters,
                                                     @NonNull String referenceInfo,
                                                     @NonNull Site site) {
    if (!StringUtils.startsWith(referenceInfo, "!")) {
      return null;
    }
    List<String> segmentPathList = splitSegmentPath(referenceInfo);
    return findContentBySegment(segmentPathList);
  }

  // --- internal ---------------------------------------------------

  private static List<String> splitSegmentPath(CharSequence segmentPath) {
    Iterable<String> splittedSegment = SEGMENT_PATH_SPLITTER.split(segmentPath);
    return ImmutableList.copyOf(splittedSegment);
  }

  private LinkableAndNavigation findContentBySegment(@NonNull List<String> segments) {
    if (segments.isEmpty()) {
      // No match for an empty path.
      return null;
    }

    // if there is exactly one segment, it is a (root-)navigation,
    // otherwise lookup the parent navigation for now.
    List<String> navigationSegments = segments.size()==1 ? segments : segments.subList(0, segments.size()-1);
    Navigation navigation = navigationSegmentsUriHelper.parsePath(navigationSegments);
    if (!(navigation instanceof CMNavigation)) {
      // No navigation at all, or not a CMS navigation, no such linkable.
      return null;
    }
    CMNavigation cmNavigation = (CMNavigation) navigation;
    Content navigationContent = cmNavigation.getContent();
    if (segments.size()==1) {
      // A single segment matches a root navigation, we are ready.
      return new LinkableAndNavigation(navigationContent, navigationContent);
    }

    // State now: Found the parent navigation, one segment left to lookup.
    String segment = segments.get(segments.size()-1);

    // check for a matching child navigation
    Content childNavigation = resolveChild(segment, navigation);
    if (childNavigation!=null) {
      return new LinkableAndNavigation(childNavigation, childNavigation);
    }

    // check for a linkable in the context of the parent navigation
    Content linkable = resolveContent(cmNavigation, segment);
    return linkable!=null ? new LinkableAndNavigation(linkable, navigationContent) : null;
  }

  /**
   * Find a child navigation matching the segment
   */
  private static Content resolveChild(String segment, Navigation navigation) {
    for (Linkable linkable : navigation.getChildren()) {
      if (linkable instanceof CMLinkable && segment.equals(linkable.getSegment())) {
        return ((CMLinkable)linkable).getContent();
      }
    }
    return null;
  }

  private Content resolveContent(CMNavigation navigation, String segment) {
    CMLinkable linkable = segmentResolver.resolveSegment(navigation.getContentId(), segment, CMLinkable.class);
    return linkable != null ? linkable.getContent() : null;
  }
}
