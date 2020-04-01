package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.cae.view.DynamicIncludePredicate;
import com.coremedia.objectserver.view.RenderNode;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.regex.Pattern;

import static com.coremedia.blueprint.cae.view.DynamicIncludeHelper.isAlreadyIncludedDynamically;

public abstract class AbstractP13nContainerPredicate implements DynamicIncludePredicate {

  private static Pattern VIEW_EXCLUDE_PATTERN = Pattern.compile("^fragmentPreview(\\[[^\\]]*\\]){0,1}$");
  private static final String VIEW_NAME_AS_PREVIEW = "asPreview";
  private static final String MULTI_VIEW_PREVIEW = "multiViewPreview";

  @Override
  public boolean apply(@Nullable RenderNode input) {
    if (input == null) {
      return false;
    }
    if (isAlreadyIncludedDynamically()) {
      return false;
    }
    if (isViewMatching(input.getView())) {
      return isBeanMatching(input.getBean());
    }
    return false;
  }

  protected boolean isViewMatching(String view) {
    return view == null || !(VIEW_EXCLUDE_PATTERN.matcher(view).matches() || view.equals(VIEW_NAME_AS_PREVIEW) || view.equals(MULTI_VIEW_PREVIEW));
  }

  protected abstract boolean isBeanMatching(Object bean);
}
