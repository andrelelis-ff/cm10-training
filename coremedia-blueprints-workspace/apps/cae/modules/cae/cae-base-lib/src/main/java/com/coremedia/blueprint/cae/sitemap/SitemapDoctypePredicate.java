package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.common.util.Predicate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

public class SitemapDoctypePredicate implements Predicate, InitializingBean {
  private List<String> includes = null;
  private List<String> excludes = null;
  private CapConnection capConnection;


  // --- Spring configuration ---------------------------------------

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      if (includes != null) {
        for (String include : includes) {
          capConnection.getContentRepository().getContentType(include);
        }
      }
      if (excludes != null) {
        for (String exclude : excludes) {
          capConnection.getContentRepository().getContentType(exclude);
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("This predicate is not suitable for your doctypes", e);
    }
  }

  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }

  public void setExcludes(List<String> excludes) {
    this.excludes = excludes;
  }

  @Required
  public void setCapConnection(CapConnection capConnection) {
    this.capConnection = capConnection;
  }


  // --- Predicate --------------------------------------------------

  @Override
  public boolean include(Object o) {
    return o instanceof Content && checkType((Content)o);
  }


  // --- internal ---------------------------------------------------

  private boolean checkType(Content content) {
    ContentType type = content.getType();

    boolean includeIt = includes == null; //if no list exists, include all, otherwise only list members are included
    if (includes!=null) {
      for (String include : includes) {
        includeIt = includeIt || type.isSubtypeOf(include);
      }
    }

    boolean excludeIt = false;
    if (includeIt && excludes!=null) {
      for (String exclude : excludes) {
        excludeIt = excludeIt || type.isSubtypeOf(exclude);
      }
    }

    return includeIt && !excludeIt;
  }
}
