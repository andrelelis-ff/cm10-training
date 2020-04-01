package com.coremedia.blueprint.cae.web.taglib;

import java.util.List;

/**
 * Utility class for List-operations in JSTL.
 * For JSP Taglibs.
 * For Freemarker use {@link BlueprintFreemarkerFacade} instead.
 */
public final class ListUtil {

  // static class
  private ListUtil() {
  }

  public static boolean contains(List l, Object o){
    return l != null && l.contains(o);
  }

}
