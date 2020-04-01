package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.util.RichtextHelper;
import com.coremedia.xml.Markup;

/**
 * Generated extension class for immutable beans of document type "CMArticle".
 */
public class CMArticleImpl extends CMArticleBase {
  @Override
  public Markup getTeaserText() {
    return RichtextHelper.htmlOptimizedRichtext(super.getTeaserText());
  }

  @Override
  public Markup getDetailText() {
    return RichtextHelper.htmlOptimizedRichtext(super.getDetailText());
  }
}
