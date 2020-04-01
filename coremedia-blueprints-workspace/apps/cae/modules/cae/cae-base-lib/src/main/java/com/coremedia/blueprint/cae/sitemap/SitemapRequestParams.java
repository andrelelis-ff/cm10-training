package com.coremedia.blueprint.cae.sitemap;

public interface SitemapRequestParams {
  /**
   * Folders to be excluded (recursively) from sitemap generation.
   *
   * A comma separated list of paths.
   */
  String PARAM_EXCLUDE_FOLDERS = "excludeFolders";

  /**
   * Determines whether the generated sitemap is to be gzipped.
   */
  String PARAM_GZIP_COMPRESSION = "gzip";
}
