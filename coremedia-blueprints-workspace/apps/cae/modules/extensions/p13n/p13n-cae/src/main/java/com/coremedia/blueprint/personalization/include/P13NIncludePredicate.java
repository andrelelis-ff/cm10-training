package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.cae.view.DynamicIncludePredicate;
import com.coremedia.blueprint.personalization.contentbeans.CMP13NSearch;
import com.coremedia.blueprint.personalization.contentbeans.CMSelectionRules;
import com.coremedia.objectserver.view.RenderNode;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.regex.Pattern;

public class P13NIncludePredicate implements DynamicIncludePredicate {

  private static Pattern VIEW_EXCLUDE_PATTERN = Pattern.compile("^fragmentPreview(\\[[^\\]]*\\]){0,1}$");
  private static final String VIEW_NAME_AS_PREVIEW = "asPreview";
  private static final String MULTI_VIEW_PREVIEW = "multiViewPreview";

  @Override
  public boolean apply(@Nullable RenderNode input) {
    if (input == null) {
      return false;
    }
    if (isBeanMatching(input.getBean())){
      return isViewMatching(input.getView());
    }
    return false;
  }

  private boolean isViewMatching(String view) {
    if (view == null) {
      return true;
    }
    return !(VIEW_EXCLUDE_PATTERN.matcher(view).matches() || view.equals(VIEW_NAME_AS_PREVIEW) || view.equals(MULTI_VIEW_PREVIEW));
  }

  private boolean isBeanMatching(Object bean) {
    return bean instanceof CMSelectionRules || bean instanceof CMP13NSearch;
  }
}
