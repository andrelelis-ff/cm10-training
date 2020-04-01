package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Resolves the relative or absolute path to identify a content.
 */
public class ContentPathExternalReferenceResolver extends ExternalReferenceResolverBase {
  private static final Logger LOG = LoggerFactory.getLogger(ContentPathExternalReferenceResolver.class);

  private static final String PREFIX = "cm-path!";

  public ContentPathExternalReferenceResolver() {
    super(PREFIX);
  }

  // --- interface --------------------------------------------------

  @Override
  protected boolean include(@NonNull FragmentParameters fragmentParameters, @NonNull String referenceInfo) {
    // Directory up path is not allowed
    if (referenceInfo.contains("..")) {
      return false;
    }

    // Disallow Sites in absolute paths
    String cmsPath = asCMSPath(referenceInfo);
    //noinspection RedundantIfStatement
    if (isAbsoluteCMSPath(cmsPath) && cmsPath.contains("/Sites")) {
      return false;
    }

    return true;
  }

  @Nullable
  @Override
  protected LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters,
                                                     @NonNull String referenceInfo,
                                                     @NonNull Site site) {
    Content linkable = resolveLinkable(referenceInfo, site);
    Content navigation = null;
    if (linkable != null) {
      // Determine context of linkable and set it as navigation
      navigation = getNavigationForLinkable(linkable);
    }
    if (navigation == null) {
      navigation = site.getSiteRootDocument();
    }

    return new LinkableAndNavigation(linkable, navigation);
  }

// --- internal ---------------------------------------------------

  private Content resolveLinkable(String referencePath, Site site) {
    Content siteRoot = site.getSiteRootFolder();

    String path = asCMSPath(referencePath);
    Content result = isAbsoluteCMSPath(path) ? contentRepository.getChild(asRelativePath(path)) : siteRoot.getChild(asRelativePath(path));

    // Exclude folders
    if (result != null && result.isFolder()) {
      return null;
    }

    return result;
  }

  /**
   * Return true if the path starts with a /
   */
  private static boolean isAbsoluteCMSPath(String path) {
    return path != null && path.startsWith("/");
  }

  /**
   * Make the given path a relative path
   */
  private static String asRelativePath(String path) {
    return path != null && path.length() > 0 && path.startsWith("/") ? path.substring(1) : path;
  }

  /**
   * Remap path to CMS path, i.e. replace '!' with '/'
   */
  private static String asCMSPath(String path) {
    return path == null ? null : path.replace("!", "/");
  }
}
