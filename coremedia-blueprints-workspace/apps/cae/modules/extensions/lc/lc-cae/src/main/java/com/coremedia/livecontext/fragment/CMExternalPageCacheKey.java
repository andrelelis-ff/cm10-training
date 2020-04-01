package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cotopaxi.common.CacheUtil;
import com.coremedia.livecontext.contentbeans.CMExternalPage;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Map;

/**
 * Cache Key that caches CMExternalChannel for a certain pageId (stored id externalId property).
 * At the same time it verifies whether the result is part of the navigation.
 */
class CMExternalPageCacheKey extends CacheKey<Content> {

  private final Site site;
  private final String pageId;
  private final TreeRelation<Content> treeRelation;

  CMExternalPageCacheKey(@NonNull String pageId, @NonNull Site site, TreeRelation<Content> treeRelation) {
    this.pageId = pageId;
    this.site = site;
    this.treeRelation = treeRelation;
  }

  @Override
  public Content evaluate(final Cache cache) throws Exception {
    return CacheUtil.getAny(new CacheUtil.Oracle<Content>() {
      @Override
      public Content guess() {
        Map<String, Content> externalPageMap = cache.get(new CMExternalPageBySiteCacheKey(site));
        return externalPageMap.get(pageId);
      }

      @Override
      public boolean verify(Content content) {
        // check if the "guessed" content really and still has the externalId and is in the navigation tree
        // of the given rootChannel
        if (pageId.equals(content.getString(CMExternalPage.EXTERNAL_ID))) {
          // check whether the content is in navigation
          List<Content> pathToRoot = treeRelation.pathToRoot(content);
          if (pathToRoot != null && pathToRoot.get(0).equals(site.getSiteRootDocument())) {
            return true;
          }
        }
        return false;
      }
    });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CMExternalPageCacheKey that = (CMExternalPageCacheKey) o;

    if (!pageId.equals(that.pageId)) return false;

    //noinspection RedundantIfStatement
    if (!site.equals(that.site)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = site.hashCode();
    result = 31 * result + (pageId.hashCode());
    return result;
  }
}
