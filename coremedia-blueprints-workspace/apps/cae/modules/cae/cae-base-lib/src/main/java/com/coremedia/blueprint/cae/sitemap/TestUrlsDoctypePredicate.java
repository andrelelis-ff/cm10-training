package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.common.util.Predicate;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;

/**
 * Returns true iff the object in question is a Content instance of a resource
 * type which is relevant for sitemaps.
 */
public class TestUrlsDoctypePredicate implements Predicate, InitializingBean {
  private CapConnection capConnection;
  private List<String> includedDoctypes = Collections.emptyList();


  // --- Spring -----------------------------------------------------

  @Required
  public void setCapConnection(CapConnection capConnection) {
    this.capConnection = capConnection;
  }

  @Required
  public void setIncludedDoctypes(List<String> includedDoctypes) {
    this.includedDoctypes = ImmutableList.copyOf(includedDoctypes);
  }

  @Override
  public void afterPropertiesSet() {
    // Check if this predicate makes sense for the repository's doctypes
    try {
      for (String includedDoctype : includedDoctypes) {
        capConnection.getContentRepository().getContentType(includedDoctype);
      }
    } catch (Exception e) {
      throw new IllegalStateException("This predicate is not suitable for your doctypes", e);
    }
  }


  // --- Predicate --------------------------------------------------

  @Override
  public boolean include(Object o) {
    if (!(o instanceof Content)) {
      return false;
    }
    Content content = (Content) o;
    return checkType(content);
  }


  // --- internal ---------------------------------------------------

  private boolean checkType(Content content) {
    // Include only teasable content, exclude non-textual content (media) and dynamic content.
    ContentType type = content.getType();
    return isTypeIncluded(type);
  }

  /**
   * Doctypes which are included by this predicate.
   * @param type The type to check.
   * @return True if type is included, else false.
   */
  private boolean isTypeIncluded(ContentType type) {
    for (String includedDoctype : includedDoctypes) {
      if (type.getName().equals(includedDoctype)) {
        return true;
      }
    }

    return false;
  }
}
