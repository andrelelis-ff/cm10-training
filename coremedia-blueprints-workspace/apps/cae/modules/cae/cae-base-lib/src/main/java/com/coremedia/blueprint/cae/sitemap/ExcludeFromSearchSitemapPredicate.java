package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.content.Content;
import com.coremedia.common.util.Predicate;
import org.springframework.beans.factory.annotation.Required;

public class ExcludeFromSearchSitemapPredicate implements Predicate {

  private String doctypeName;
  private String notSearchablePropertyName;

  @Override
  public boolean include(Object o) {
    return o instanceof Content && checkIsSearchable((Content) o);
  }

  /**
   * If its not the content type or the searchable flag is not set, return true, false otherwise
   *
   * @param o any content that must be checked
   * @return true if the content is not from given doctype or the searchable flag is not set.
   */
  private boolean checkIsSearchable(Content o) {
    return !o.getType().isSubtypeOf(doctypeName) || !o.getBoolean(notSearchablePropertyName);
  }

  @Required
  public void setDoctypeName(String doctypeName) {
    this.doctypeName = doctypeName;
  }

  @Required
  public void setNotSearchablePropertyName(String notSearchablePropertyName) {
    this.notSearchablePropertyName = notSearchablePropertyName;
  }
}
