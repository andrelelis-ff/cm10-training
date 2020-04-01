package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * External Content resolver for 'externalRef' values that contain a content id.
 */
public class ContentCapIdExternalReferenceResolver extends ExternalReferenceResolverBase {

  public ContentCapIdExternalReferenceResolver() {
    super(CONTENT_ID_FRAGMENT_PREFIX);
  }

  // --- interface --------------------------------------------------

  @Override
  protected boolean include(@NonNull FragmentParameters fragmentParameters, @NonNull String referenceInfo) {
    return IdHelper.isContentObjectId(referenceInfo);
  }

  @Nullable
  @Override
  protected LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters,
                                                     @NonNull String referenceInfo,
                                                     @NonNull Site site) {
    Content linkable = contentRepository.getContent(referenceInfo);
    Content navigation = null;
    if (linkable != null) {
      // Determine context of linkable and set it as navigation
      navigation = getNavigationForLinkable(linkable);
    }
    if (navigation == null){
      navigation = site.getSiteRootDocument();
    }

    return new LinkableAndNavigation(linkable, navigation);
  }

}
