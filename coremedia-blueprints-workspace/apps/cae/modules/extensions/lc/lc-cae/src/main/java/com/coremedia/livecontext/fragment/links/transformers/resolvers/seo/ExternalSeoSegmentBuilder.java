package com.coremedia.livecontext.fragment.links.transformers.resolvers.seo;

import com.coremedia.blueprint.base.links.SettingsBasedVanityUrlMapper;
import com.coremedia.blueprint.base.links.VanityUrlMapperCacheKey;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import static java.text.MessageFormat.format;

/**
 * Generates external seo segment name for a given navigation and linkable for use in commerce links.
 */
public class ExternalSeoSegmentBuilder implements SeoSegmentBuilder {

  public static final Logger LOG = LoggerFactory.getLogger(ExternalSeoSegmentBuilder.class);

  private static final String PATH_DELIMITER = "--";
  private static final String PATH_DELIMITER_REGEX = PATH_DELIMITER + "+";
  private static final String ID_DELIMITER = "-";
  private static final String ID_DELIMITER_BEGIN_REGEX = "^" +ID_DELIMITER + "+";
  private static final String ID_DELIMITER_END_REGEX = ID_DELIMITER + "+$";
  private static final String DUMMY_SEGMENT = "s";

  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;
  private Cache cache;
  private SettingsService settingsService;

  @NonNull
  public String asSeoSegment(CMNavigation navigation, CMObject target) {
    if (navigation == null || target == null) {
      return "";
    }

    try {
      StringBuilder sb = new StringBuilder();

      //vanity url configured for the target?
      String vanity = getVanityUrl(navigation, target);

      if (vanity != null) {
        //we allow '/' in the vanity url which we understand as a path delimiter.
        vanity = vanity.replaceAll("/", PATH_DELIMITER);
        sb.append(vanity);
      } else {
        List<String> navigationPath = navigationSegmentsUriHelper.getPathList(navigation);
        // we omit the root segment (e.g. "aurora" because it is reproducible)
        for (int i = 1; i < navigationPath.size(); i++) {
          if (i > 1) {
            sb.append(PATH_DELIMITER);
          }
          sb.append(navigationPath.get(i));
        }

        if (!navigation.equals(target)) {
          String segment = null;
          if (target instanceof Linkable) {
            Linkable linkable = (Linkable) target;
            segment = linkable.getSegment();
          }
          if (sb.length() > 0) {
            sb.append(PATH_DELIMITER);
          }
          sb.append(asSeoTitle(StringUtils.isNotBlank(segment) ? segment : DUMMY_SEGMENT));
          sb.append(ID_DELIMITER);
          sb.append(target.getContentId());
        }
      }

      return asUrlEncoded(sb.toString());
    }
    catch (Exception e) {
      LOG.error(format("Cannot generate SEOSegment for the navigation {0} and target {1}",
              navigation.getContent().getPath(), target.getContent().getPath()), e);
      return "";
    }
  }

  @Nullable
  private String getVanityUrl(CMNavigation navigation, CMObject target) {
    Content rootChannnel = navigation.getRootNavigation().getContent();
    final SettingsBasedVanityUrlMapper vanityUrlMapper = cache.get(new VanityUrlMapperCacheKey(rootChannnel, settingsService));
    return vanityUrlMapper.patternFor(target.getContent());
  }

  private String asUrlEncoded(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      LOG.trace("Cannot encode string {}", s, e);
    }
    return s;
  }

  /**
   * To lowercase;
   * replace all non-alphabetic with a dash;
   * reduce multiple dashes to one;
   * remove dashes at the beginning.
   * remove dashes at the end.
   */
  private String asSeoTitle(String s) {
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

    //'+' means that PATH_DELIMITER will be replaced RECURSIVELY.
    result = result.replaceAll(PATH_DELIMITER_REGEX, ID_DELIMITER);

    // Remove dashes at the beginning
    result = result.replaceAll(ID_DELIMITER_BEGIN_REGEX, "");

    // Remove dashes at the end
    result = result.replaceAll(ID_DELIMITER_END_REGEX, "");

    return result;
  }

  private boolean isAscii(int ch) {
    return ((ch & 0xFFFFFF80) == 0);
  }


  @Required
  public void setNavigationSegmentsUriHelper(NavigationSegmentsUriHelper navigationSegmentsUriHelper) {
    this.navigationSegmentsUriHelper = navigationSegmentsUriHelper;
  }

  @Required
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }
}
