package com.coremedia.livecontext.ecommerce.ibm.cae.sitemap;

import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.common.util.Predicate;

/**
 * Returns true iff the object in question is a Content instance of a resource
 * to be indexed into the WCS search.
 */
public class WcsAasCrawlSitemapPredicate implements Predicate {

  // --- Predicate --------------------------------------------------

  @Override
  public boolean include(Object o) {
    if (!(o instanceof Content)) {
      return false;
    }
    Content content = (Content) o;
    return isWcsSearchRelevant(content);
  }


  // --- internal ---------------------------------------------------

  private boolean isWcsSearchRelevant(Content content) {
    ContentType type = content.getType();
    return type.isSubtypeOf(CMArticle.NAME);
  }
}
