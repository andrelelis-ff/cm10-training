package com.coremedia.blueprint.coderesources;

import com.coremedia.cap.content.Content;
import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class CodeResourcesModelImpl implements CodeResourcesModel {
  private static final String CMABSTRACTCODE_IEEXPRESSION = "ieExpression";
  private static final String CMJAVASCRIPT_INHEAD = "inHead";

  private String codeType;
  private String htmlMode;
  private CodeResourcesImpl codeResources;
  private Predicate<Content> filter;


  // --- construct and configure ------------------------------------

  CodeResourcesModelImpl(String codeType, String htmlMode, CodeResourcesImpl codeResources) {
    this.codeType = codeType;
    this.htmlMode = htmlMode;
    this.codeResources = codeResources;
    filter = TYPE_JS.equals(codeType) ? new JavaScriptFilter() : new CssFilter();
  }


  // --- CodeResourcesModel -----------------------------------------

  @Override
  public String getCodeType() {
    return codeType;
  }

  @Override
  public String getHtmlMode() {
    return htmlMode;
  }

  // impl note: the codeResources etag is not specific for this model's
  // htmlMode, so it may change even if only unrelated resources change.
  // This could be improved.
  @Override
  public String getETag() {
    return codeResources.getETag();
  }

  @Override
  public Content getChannelWithTheme() {
    return codeResources.getChannelWithTheme();
  }

  @Override
  public Content getChannelWithCode() {
    return codeResources.getChannelWithCode();
  }

  @Override
  @NonNull
  public List<?> getLinkTargetList() {
    // Take care for the appropriate order:
    List<Object> result = new ArrayList<>();

    // 1. external links
    result.addAll(filterExternalLinks());

    // 2. ordinary code resources
    List<Content> mergeableResources = filterMergeableResources();
    if (!mergeableResources.isEmpty()) {
      if (codeResources.mergeResources()) {
        result.add(mergeableResources);  // add as mergeable resources
      } else {
        result.addAll(mergeableResources);  // add as single resources
      }
    }

    // 3. ie excludes
    result.addAll(filterIeExcludes());
    return result;
  }


  // --- more features ----------------------------------------------

  @Override
  public String toString() {
    return getClass().getName() + "[" + codeType + ", " + htmlMode + ", " + codeResources + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CodeResourcesModelImpl that = (CodeResourcesModelImpl) o;

    if (getCodeType() != null ? !getCodeType().equals(that.getCodeType()) : that.getCodeType() != null) {
      return false;
    }
    if (getHtmlMode() != null ? !getHtmlMode().equals(that.getHtmlMode()) : that.getHtmlMode() != null) {
      return false;
    }
    return codeResources != null ? codeResources.equals(that.codeResources) : that.codeResources == null;
  }

  @Override
  public int hashCode() {
    int result = getCodeType() != null ? getCodeType().hashCode() : 0;
    result = 31 * result + (getHtmlMode() != null ? getHtmlMode().hashCode() : 0);
    result = 31 * result + (codeResources != null ? codeResources.hashCode() : 0);
    return result;
  }


  // --- internal ---------------------------------------------------

  private List<Content> filterExternalLinks() {
    return codeResources.getExternalLinks().stream().filter(filter).collect(Collectors.toList());
  }

  private List<Content> filterMergeableResources() {
    return codeResources.getMergeableResources().stream().filter(filter).collect(Collectors.toList());
  }

  private List<Content> filterIeExcludes() {
    return codeResources.getIeExcludes().stream().filter(filter).collect(Collectors.toList());
  }

  private class JavaScriptFilter implements Predicate<Content> {
    @Override
    public boolean test(Content javaScript) {
      String ieExpression = javaScript.getString(CMABSTRACTCODE_IEEXPRESSION);
      if (!StringUtils.isEmpty(ieExpression)) {
        return MODE_IE.equals(htmlMode);
      } else {
        return !MODE_IE.equals(htmlMode) && javaScript.getBoolean(CMJAVASCRIPT_INHEAD) == MODE_HEAD.equals(htmlMode);
      }
    }
  }

  private class CssFilter implements Predicate<Content> {
    @Override
    public boolean test(Content css) {
      String ieExpression = css.getString(CMABSTRACTCODE_IEEXPRESSION);
      return StringUtils.isEmpty(ieExpression) == MODE_BODY.equals(htmlMode);
    }
  }

}
