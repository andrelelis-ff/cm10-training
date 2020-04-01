package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * External Content resolver for 'externalRef' values "cm-seosegment:<optionalstring>--<optionalcontextid>-<contentid>"
 */
public class ContentSeoSegmentExternalReferenceResolver extends ExternalReferenceResolverBase {

  public static final String SEO_SEGMENT_PREFIX = "cm-seosegment:";
  public static final String SEO_SEGMENT_ID_DELIMITER_INFIX = "--";
  public static final String SEO_SEGMENT_ID_SPLITTER_INFIX = "-";

  public ContentSeoSegmentExternalReferenceResolver() {
    super(SEO_SEGMENT_PREFIX);
  }

  // --- interface --------------------------------------------------

  @Override
  protected boolean include(@NonNull FragmentParameters fragmentParameters, @NonNull String referenceInfo) {
    Ids ids = parseExternalReferenceInfo(referenceInfo);
    return ids != null
            && IdHelper.isDocument(ids.contentId)
            && (ids.contextId == null || IdHelper.isDocument(ids.contextId));
  }

  @Nullable
  @Override
  protected LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters,
                                                     @NonNull String referenceInfo,
                                                     @NonNull Site site) {
    Ids ids = parseExternalReferenceInfo(referenceInfo);
    if (ids == null) {
      return null;
    }
    Content linkable = resolveLinkable(ids);
    Content navigation = resolveNavigation(ids);
    return new LinkableAndNavigation(linkable, navigation);
  }

// --- internal ---------------------------------------------------

  private Content resolveLinkable(@NonNull Ids ids) {
    String capId = IdHelper.formatContentId(ids.contentId);
    return contentRepository.getContent(capId);
  }

  private Content resolveNavigation(@NonNull Ids ids) {
    if (ids.contextId == null) {
      return null;
    }

    String capId = IdHelper.formatContentId(ids.contextId);
    return contentRepository.getContent(capId);
  }


  public static Ids parseExternalReferenceInfo(@NonNull String externalRef) {
    try {
      if (!externalRef.contains(SEO_SEGMENT_ID_DELIMITER_INFIX)) {
        return null;
      }

      String idString = externalRef.substring(externalRef.indexOf(SEO_SEGMENT_ID_DELIMITER_INFIX)+SEO_SEGMENT_ID_DELIMITER_INFIX.length());
      if (idString.contains(SEO_SEGMENT_ID_SPLITTER_INFIX)) {
        String contextId = idString.substring(0, idString.indexOf(SEO_SEGMENT_ID_SPLITTER_INFIX));
        String contentId = idString.substring(idString.indexOf(SEO_SEGMENT_ID_SPLITTER_INFIX) + 1);
        return new Ids(Integer.parseInt(contextId), Integer.parseInt(contentId));
      }
      else {
        return new Ids(Integer.parseInt(idString));
      }
    } catch (NumberFormatException e) {
      // treat as error
    }
    return null;
  }

  public static class Ids {
    public Integer contextId;
    public int contentId;

    public Ids(int contextId, int contentId) {
      this.contextId = contextId;
      this.contentId = contentId;
    }

    public Ids(int contentId) {
      this.contentId = contentId;
    }
  }
}
