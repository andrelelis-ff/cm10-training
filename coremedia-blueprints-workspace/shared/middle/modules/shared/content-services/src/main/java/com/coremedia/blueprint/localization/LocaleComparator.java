package com.coremedia.blueprint.localization;

import com.coremedia.cap.content.Content;
import com.coremedia.common.graph.PartialComparator;

/**
 * Comparator for localized contents by locale and a dedicated master.
 * <p>
 * Order: master is the least.
 */
class LocaleComparator implements PartialComparator<Content> {
  private Content master;

  LocaleComparator(Content master) {
    this.master = master;
  }

  @Override
  public int compare(Content o1, Content o2) {  // NOSONAR cyclomatic complexity
    if (o1.equals(o2)) {
      return 0;
    }
    if (o1.equals(master)) {
      return -1;
    }
    if (o2.equals(master)) {
      return 1;
    }

    String locale1 = o1.getString("locale");
    String locale2 = o2.getString("locale");
    if (locale1==null) {
      locale1 = "";
    }
    if (locale2==null) {
      locale2 = "";
    }

    if (locale1.equals(locale2)) {
      return 0;
    }
    if (locale1.startsWith(locale2)) {
      return 1;
    }
    if (locale2.startsWith(locale1)) {
      return -1;
    }
    return 2;
  }
}
