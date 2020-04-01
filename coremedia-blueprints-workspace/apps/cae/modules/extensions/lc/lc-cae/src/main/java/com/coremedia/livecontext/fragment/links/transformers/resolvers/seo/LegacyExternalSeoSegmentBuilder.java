package com.coremedia.livecontext.fragment.links.transformers.resolvers.seo;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Generates external seo segment name for a given navigation and linkable for use in commerce links.
 * @deprecated use {@link ExternalSeoSegmentBuilder} instead
 */
@Deprecated
public class LegacyExternalSeoSegmentBuilder implements SeoSegmentBuilder {

  public static final Logger LOG = LoggerFactory.getLogger(LegacyExternalSeoSegmentBuilder.class);

  public static final String SEO_ID_PREFIX = "--";
  public static final String SEO_ID_SEPARATOR = "-";

  @NonNull
  public String asSeoSegment(CMNavigation navigation, CMObject contentBean) {
    if (navigation == null || contentBean == null) {
      return "";
    }

    if (contentBean instanceof CMLinkable) {
      return asSeoSegment(navigation, ((CMLinkable) contentBean));
    }

    try {
      StringBuilder sb = new StringBuilder();
      sb.append(SEO_ID_PREFIX).append(navigation.getContentId()).append(SEO_ID_SEPARATOR).append(contentBean.getContentId());
      return sb.toString();
    }
    catch (Exception e) {
      LOG.error("Could not generate SEOSegment", e);
      return "";
    }
  }

  public String asSeoSegment(CMNavigation navigation, CMLinkable linkable) {
    if (navigation == null || linkable == null) {
      return null;
    }

    try {
      StringBuilder sb = new StringBuilder();
      sb.append(asSeoTitle(linkable.getTitle()));
      sb.append(SEO_ID_PREFIX);

      if (!navigation.equals(linkable)) {
        sb.append(navigation.getContentId()).append(SEO_ID_SEPARATOR).append(linkable.getContentId());
      }
      else {
        sb.append(navigation.getContentId());
      }
      return sb.toString();
    }
    catch (Exception e) {
      LOG.error("Could not generate SEOSegment", e);
      return null;
    }
  }

  /**
   * To lowercase; replace all non-alphabetic with a dash; reduce multiple dashes to one, remove dashes at end.
   */
  @VisibleForTesting
  protected String asSeoTitle(String s) {
    char[] ca = s.toCharArray();
    for (int index = 0; index < ca.length; index++) {
      if (Character.isLetterOrDigit(ca[index]) && isAscii(ca[index])) {
        ca[index] = Character.toLowerCase(ca[index]);
      }
      else {
        ca[index] = '-';
      }
    }
    String result = String.valueOf(ca);

    // Reduce consecutive dashes into one
    while (result.contains("--")) {
      result = result.replace("--", "-");
    }

    // Remove dashes at end
    while (result.endsWith("-")) {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }

  private boolean isAscii(int ch) {
    return ((ch & 0xFFFFFF80) == 0);
  }
}
