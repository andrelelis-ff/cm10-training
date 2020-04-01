package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.multisite.Site;

/**
 * Controls the lifecycle of the SitemapRenderer.
 * Overriding methods must invoke their super methods, except for requiredProperties().
 */
public abstract class AbstractSitemapRenderer implements SitemapRenderer {
  private StringBuilder urlList;
  private String result;
  private int count;
  private Site site;


  // --- SitemapRenderer --------------------------------------------

  @Override
  public void startUrlList() {
    result = null;
    count = 0;
    urlList = new StringBuilder();
  }

  @Override
  public void appendUrl(String url) {
    if (urlList==null || result!=null) {
      throw new IllegalStateException("Must call startUrlList before.");
    }
    ++count;
  }

  @Override
  public void endUrlList() {
    if (urlList==null || result!=null) {
      throw new IllegalStateException("Must call startUrlList before.");
    }
    result = urlList.toString();
    urlList = null;
  }

  @Override
  public String getResponse() {
    if (result==null) {
      throw new IllegalStateException("Must call endUrlList before.");
    }
    return result;
  }

  @Override
  public void setSite(Site site) {
    this.site = site;
  }


  // --- features ---------------------------------------------------

  /**
   * Returns the site which has been set by {@link #setSite}.
   *
   * @return the site
   */
  protected Site getSite() {
    if (site==null) {
      throw new IllegalStateException("Site not set");
    }
    return site;
  }

  /**
   * Append something to the result.
   *
   * Helper method for the implementation of appendUrl(String url).
   * May be invoked only after startUrlList() and before endUrlList().
   *
   * @param str something
   */
  protected final void print(String str) {
    if (urlList==null || result!=null) {
      throw new IllegalStateException("May be invoked only after startUrlList() and before endUrlList().");
    }
    urlList.append(str);
  }

  /**
   * Append something to the result and add a trailing newline.
   *
   * Helper method for the implementation of appendUrl(String url).
   * May be invoked only after startUrlList() and before endUrlList().
   *
   * @param str something
   */
  protected final void println(String str) {
    print(str);
    urlList.append("\n");
  }

  /**
   * Returns the current number of list entries in the result.
   *
   * @return the current number of list entries in the result.
   */
  protected final int currentCount() {
    return count;
  }

  /**
   * Returns the interim result.
   *
   * Helper method for the implementation of appendUrl(String url).
   * May be invoked only after startUrlList() and before endUrlList().
   *
   * @return the interim result
   */
  protected final String currentResult() {
    return urlList.toString();
  }
}
