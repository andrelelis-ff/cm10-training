package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * This model contains a subset of assets, e.g. of a subject tag or an asset category. The subset is defined by the
 * currentPage and the total number of pages with assets.
 * </p>
 *
 * @cm.template.api
 */
public class PaginatedAssets {

  private final List<AMAsset> assets;
  private int currentPage = 1;
  private int pageCount = 1;
  private long totalCount = 0;

  private Notification notification;

  private final Map<String, String> baseRequestParams = new HashMap<>();

  /**
   * Creates the default page that does not contain any assets and that is not linked to any
   * taxonomy node.
   */
  public PaginatedAssets() {
    this.assets = Collections.emptyList();
  }

  /**
   * Creates an asset page defined by the given parameters.
   *
   * @param assets      the asset subset
   * @param currentPage the current page number
   * @param pageCount   the total number of available pages
   * @param totalCount  the total number of assets
   */
  public PaginatedAssets(@NonNull List<AMAsset> assets, int currentPage, int pageCount, long totalCount) {
    this.assets = Collections.unmodifiableList(assets);
    if (pageCount > 0) {
      this.pageCount = pageCount;
    }
    if (currentPage > 0 && currentPage <= pageCount) {
      this.currentPage = currentPage;
    }
    this.totalCount = totalCount;
  }

  /**
   * Returns the assets within the current page
   *
   * @return the assets within the current page
   * @cm.template.api
   */
  @NonNull
  public List<AMAsset> getAssets() {
    return assets;
  }

  /**
   * Returns the current page number
   *
   * @return the current page (always greater than 0)
   * @cm.template.api
   */
  public int getCurrentPage() {
    return currentPage;
  }

  /**
   * Returns the total number of available pages
   *
   * @return the total number of available pages (always greater than 0)
   * @cm.template.api
   */
  public int getPageCount() {
    return pageCount;
  }

  public long getTotalCount() {
    return totalCount;
  }

  /**
   * @cm.template.api
   */
  public Map<String, String> getBaseRequestParams() {
    return Collections.unmodifiableMap(baseRequestParams);
  }

  public void setBaseRequestParams(Map<String, String> baseRequestParams) {
    this.baseRequestParams.clear();
    this.baseRequestParams.putAll(baseRequestParams);
  }

  /**
   * @cm.template.api
   */
  @Nullable
  public Notification getNotification() {
    return notification;
  }

  public void setNotification(@Nullable Notification notification) {
    this.notification = notification;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaginatedAssets that = (PaginatedAssets) o;
    boolean sameCounters = currentPage == that.currentPage &&
            pageCount == that.pageCount &&
            totalCount == that.totalCount;
    return sameCounters &&
            Objects.equals(baseRequestParams, that.baseRequestParams) &&
            Objects.equals(assets, that.assets) &&
            Objects.equals(notification, that.notification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assets, currentPage, pageCount, baseRequestParams, notification);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
            ", currentPage=" + currentPage +
            ", pageCount=" + pageCount +
            ", baseRequestParams=" + baseRequestParams +
            ", localizedMessage=" + notification +
            '}';
  }
}
