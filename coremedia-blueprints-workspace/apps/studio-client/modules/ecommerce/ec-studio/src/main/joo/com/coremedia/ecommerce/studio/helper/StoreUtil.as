package com.coremedia.ecommerce.studio.helper {

import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ecommerce.studio.catalogHelper;
import com.coremedia.ecommerce.studio.model.Catalog;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.RemoteBeanUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

/**
 * Utilities for dealing with store instances.
 */
public class StoreUtil {

  public static function getStoreForSiteExpression(site:Site):ValueExpression {
    return ValueExpressionFactory.createFromFunction(getStoreForSite, site);
  }

  public static function getStoreForSite(site:Site):Store {
    var siteId:String = site.getId();
    return getValidatedStore(siteId);
  }

  public static function getRootCategoryForStoreExpression(store:Store):ValueExpression {
    return ValueExpressionFactory.createFromFunction(getRootCategoryForStore, store);
  }

  public static function getRootCategoryForStore(store:Store):Category {
    if(store === undefined) {
      return undefined;
    }
    if(store is Store) {
      var defaultCatalog:Catalog = store.getDefaultCatalog();
      if (defaultCatalog === undefined) {
        return undefined;
      }
      if (defaultCatalog === null) {
        return store.getRootCategory();
      }
      return defaultCatalog.getRootCategory();
    }
    return null;
  }

  /**
   * Return the store for the given siteId if it exists, null otherwise.
   */
  internal static function getValidatedStore(siteId:String):Store {
    if (siteId === undefined) {
      return undefined;
    }
    if (siteId === null) {
      return null;
    }
    var workspaceId:String = catalogHelper.getExtractedWorkspaceId();
    var store:Store = beanFactory.getRemoteBean("livecontext/store/" + siteId + "/" + workspaceId) as Store;
    // only the server knows if the store exists, so load and check if it's ID has some value
    var accessible:Boolean = RemoteBeanUtil.isAccessible(store);
    if (accessible === undefined) {
      return undefined;
    }

    return accessible ? store : null;
  }

  public static function getActiveStore():Store {
    var siteId:String = editorContext.getSitesService().getPreferredSiteId();
    return getValidatedStore(siteId);
  }
}
}
