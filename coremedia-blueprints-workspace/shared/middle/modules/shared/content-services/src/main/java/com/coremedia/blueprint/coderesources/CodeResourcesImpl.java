package com.coremedia.blueprint.coderesources;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.util.DeveloperPaths;
import com.coremedia.cap.user.User;
import com.coremedia.xml.Markup;
import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Assembles the code resources (CSS or JS) of a channel.
 * <p>
 * Merges the resources of the channel's theme with the resources of the
 * channel's css or javaScript link lists.
 * <p>
 * Does *NOT* inherit code resources of the parent channel.
 */
class CodeResourcesImpl implements CodeResources {
  private static final String CMNAVIGATION_THEME = "theme";
  private static final String CMNAVIGATION_CSS = "css";
  private static final String CMNAVIGATION_JAVASCRIPT = "javaScript";
  private static final String CMTHEME_CSS = "css";
  private static final String CMTHEME_JAVASCRIPTLIBS = "javaScriptLibs";
  private static final String CMTHEME_JAVASCRIPTS = "javaScripts";
  private static final String CMABSTRACTCODE_INCLUDE = "include";
  private static final String CMABSTRACTCODE_IEEXPRESSION = "ieExpression";
  private static final String CMABSTRACTCODE_CODE = "code";
  private static final String CMABSTRACTCODE_DATAURL = "dataUrl";

  private static final String DIGEST_ALGORITHM = "MD5";

  private final boolean developerMode;
  private final DeveloperPaths development;

  private Boolean mergeResources;

  private final CodeCarriers codeCarriers;
  private final String codePropertyName;

  private final String contentHash;

  private List<Content> mergeableResources = new ArrayList<>();
  private List<Content> ieExcludes = new ArrayList<>();
  private List<Content> externalLinks = new ArrayList<>();


  // --- construct and configure ------------------------------------

  /**
   * Visible only for {@link CodeResourcesCacheKey}
   * <p>
   * This constructor is expensive.  Use a {@link CodeResourcesCacheKey}
   * instead.
   * <p>
   * The codePropertyName refers to the CMNavigation content type and
   * determines the code type (CSS or JavaScript) for this instance
   */
  CodeResourcesImpl(CodeCarriers codeCarriers, String codePropertyName, boolean developerMode, @Nullable User developer) {
    checkCodePropertyName(codePropertyName);
    this.codeCarriers = codeCarriers;
    this.codePropertyName = codePropertyName;
    this.developerMode = developerMode;
    development = new DeveloperPaths(developer);
    MessageDigest digest = createDigest();
    transitiveClosure(getCodeResourcsFromContext(), digest, new HashSet<>());
    contentHash = String.format("%01x", new BigInteger(1, digest.digest()));
  }

  public void setMergeResources(Boolean mergeResources) {
    this.mergeResources = mergeResources;
  }

  // --- features ---------------------------------------------------

  Content getChannelWithTheme() {
    return codeCarriers.getThemeCarrier();
  }

  Content getChannelWithCode() {
    return codeCarriers.getCodeCarrier();
  }

  String getETag() {
    return contentHash;
  }

  List<Content> getMergeableResources() {
    return mergeableResources;
  }

  List<Content> getIeExcludes() {
    return ieExcludes;
  }

  List<Content> getExternalLinks() {
    return externalLinks;
  }

  /**
   * Return true if the resources contained within this instance should be merged into a single resource
   */
  boolean mergeResources() {
    return mergeResources == null ? !developerMode : mergeResources;
  }

  // --- CodeResources ----------------------------------------------

  /**
   * Get a CodeResourcesModel for the given html mode (head, body or ie).
   * <p>
   * The code type (CSS or JavaScript) of the CodeResourcesModel is derived
   * by this CodeResources' codePropertyName and thus specific for the
   * CMNavigation content type.
   */
  @Override
  public CodeResourcesModel getModel(String htmlMode) {
    String codeType = CMNAVIGATION_CSS.equals(codePropertyName) ? CodeResourcesModel.TYPE_CSS : CodeResourcesModel.TYPE_JS;
    return new CodeResourcesModelImpl(codeType, htmlMode, this);
  }

  // --- Object -----------------------------------------------------

  @Override
  public String toString() {
    return getClass().getName() + "[" + codePropertyName + ", " + codeCarriers + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CodeResourcesImpl that = (CodeResourcesImpl) o;
    return developerMode == that.developerMode &&
            Objects.equals(development, that.development) &&
            Objects.equals(codeCarriers, that.codeCarriers) &&
            Objects.equals(codePropertyName, that.codePropertyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(developerMode, development, codeCarriers, codePropertyName);
  }


  // --- internal ---------------------------------------------------

  @NonNull
  private List<Content> getCssIncludingThemes() {
    List<Content> result = new ArrayList<>();

    Content themeCarrier = codeCarriers.getThemeCarrier();
    Content theme = themeCarrier!=null ? development.substitute(themeCarrier.getLink(CMNAVIGATION_THEME)) : null;
    if (theme != null) {
      result.addAll(development.substitute(theme.getLinks(CMTHEME_CSS)));
    }

    Content cssCarrier = codeCarriers.getCodeCarrier();
    if (cssCarrier!=null) {
      result.addAll(development.substitute(cssCarrier.getLinks(CMNAVIGATION_CSS)));
    }

    return result;
  }

  @NonNull
  private List<Content> getJavaScriptIncludingThemes() {
    List<Content> result = new ArrayList<>();

    Content themeCarrier = codeCarriers.getThemeCarrier();
    Content theme = themeCarrier!=null ? development.substitute(themeCarrier.getLink(CMNAVIGATION_THEME)) : null;
    if (theme != null) {
      result.addAll(development.substitute(theme.getLinks(CMTHEME_JAVASCRIPTLIBS)));
      result.addAll(development.substitute(theme.getLinks(CMTHEME_JAVASCRIPTS)));
    }

    Content jsCarrier = codeCarriers.getCodeCarrier();
    if (jsCarrier!=null) {
      result.addAll(development.substitute(jsCarrier.getLinks(CMNAVIGATION_JAVASCRIPT)));
    }

    return result;
  }

  @NonNull
  private List<Content> getCodeResourcsFromContext() {
    return CMNAVIGATION_CSS.equals(codePropertyName)
            ? getCssIncludingThemes()
            : getJavaScriptIncludingThemes();
  }

  private static void checkCodePropertyName(String codePropertyName) {
    if (!CMNAVIGATION_CSS.equals(codePropertyName) && !CMNAVIGATION_JAVASCRIPT.equals(codePropertyName)) {
      throw new IllegalArgumentException("No such CMNavigation code property: " + codePropertyName);
    }
  }

  /**
   * Compute a filtered lists of CMAbstractCode codes for the given
   * list of codes and their CMAbstractCode#getInclude includes.
   *
   * @param devSubstitutedCodes already devSubstituted codes
   */
  private void transitiveClosure(@NonNull Collection<Content> devSubstitutedCodes, MessageDigest digest, Collection<Content> visited) {
    for (Content code : devSubstitutedCodes) {
      //only traverse code if not already traversed.
      if (!visited.contains(code)) {
        visited.add(code);
        // get all included contents as well.
        transitiveClosure(development.substitute(code.getLinks(CMABSTRACTCODE_INCLUDE)), digest, visited);
        processCode(code, digest);
      }
    }
  }

  private void processCode(Content code, MessageDigest digest) {
    boolean isIeExclude = !StringUtils.isEmpty(code.getString(CMABSTRACTCODE_IEEXPRESSION));
    boolean isExternalLink = !StringUtils.isEmpty(code.getString(CMABSTRACTCODE_DATAURL));
    Markup codeCode = code.getMarkup(CMABSTRACTCODE_CODE);

    // If an external links is also an IE exclude, treat it as an IE exclude as the conditional comments are required:
    if (isIeExclude) {
      ieExcludes.add(code);
    } else if (isExternalLink) {
      externalLinks.add(code);
    } else if (codeCode != null) {
      mergeableResources.add(code);
    }

    byte[] statusFlags = new byte[]{(byte) (isIeExclude ? 1 : 0), (byte) (isExternalLink ? 1 : 0)};
    digest.update(statusFlags);
    String s = codeCode==null ? null : Integer.toString(codeCode.hashCode());
    if (codeCode != null) {
      digest.update(s.getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Compile a single hash code for the tree of codes linked in a navigation.
   */
  private static MessageDigest createDigest() {
    try {
      return MessageDigest.getInstance(DIGEST_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Unsupported digest algorithm", e);
    }
  }
}
