package com.coremedia.blueprint.assets.cae;

/**
 * @cm.template.api
 */
public class SearchOverview implements DownloadPortalContext {

  private String query;

  public SearchOverview(String query) {
    this.query = query;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SearchOverview that = (SearchOverview) o;

    return !(query != null ? !query.equals(that.query) : that.query != null);

  }

  @Override
  public String getSearchTerm() {
    return query;
  }


  @Override
  public int hashCode() {
    return query != null ? query.hashCode() : 0;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
            "query='" + query + '\'' +
            '}';
  }
}
