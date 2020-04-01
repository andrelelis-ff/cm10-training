package com.coremedia.catalog.studio.library {

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.catalog.studio.CatalogStudioPluginBase;
import com.coremedia.cms.editor.sdk.collectionview.tree.ContentTreeModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.catalogHelper;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.beanFactory;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.icons.CoreIcons')]
public class RepositoryCatalogTreeModel extends ContentTreeModel {
  public static const REPOSITORY_CATALOG_TREE_ID:String = "repositoryCatalogTreeId";

  override public function getNodeId(model:Object):String {
    var content:Content = model as Content;
    if (!content || content.isFolder()) {
      return null;
    }

    if(!CatalogHelper.getInstance().isActiveCoreMediaStore()) {
      return null;
    }

    var contentType:ContentType = content.getType();
    if (contentType) {
      var typeBean:RemoteBean = contentType as RemoteBean;
      if (typeBean && typeBean.isLoaded() && !contentType.isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_CATEGORY)) {
        return null;
      }
    }

    // otherwise, we really don't know if its a CMCategory, but we have to return something here synchronously...
    return super.getNodeId(model);
  }

  override public function getIdPathFromModel(model:Object):Array {
    var content:Content = model as Content;
    if (!content) {
      // No path exists.
      return null;
    }
    if(!content.isLoaded()) {
      return undefined;
    }

    //the current active site has another store contentType
    if(!CatalogHelper.getInstance().isActiveCoreMediaStore()) {
      return null;
    }

    var contentType:ContentType = content.getType();
    if (!(contentType.isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_PRODUCT) || contentType.isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_CATEGORY))) {
      return null;
    }


    // check if the content is part of the active site,
    // otherwise return null so that the content from the global tree is selected
    var siteId:String = editorContext.getSitesService().getSiteIdFor(content);
    if(siteId !== editorContext.getSitesService().getPreferredSiteId()) {
      return null;
    }

    return super.getIdPathFromModel(model);
  }

  override public function getNodeModel(nodeId:String):Object {
    if(!CatalogHelper.getInstance().isActiveCoreMediaStore()) {
      return null;
    }

    var content:Content = beanFactory.getRemoteBean(nodeId) as Content;
    if(content === null) {
      return null;
    }
    
    if(!content.isLoaded()) {
      content.load();
      return undefined;
    }

    if(!content.getType().isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_CATEGORY)) {
      return null;
    }

    return content && !content.isFolder() ? content : null;
  }

  override public function getRootId():String {
    return getNodeId(getCatalogRoot());
  }

  override public function getText(nodeId:String):String {
    if (nodeId === getNodeId(getCatalogRoot())) {
      return "Corporate-Catalog";
    }
    return super.getText(nodeId);
  }

  override public function getIconCls(nodeId:String):String {
    var nodeModel:Content = getNodeModel(nodeId) as Content;
    if (nodeModel === getCatalogRoot()) {
      return ResourceManager.getInstance().getString('com.coremedia.icons.CoreIcons', 'commerce_catalog');
    }
    return super.getIconCls(nodeId);
  }

  override protected function getVisibleRootModels():Array {
    return [getCatalogRoot()];
  }

  // To avoid a static cycle while doing MAKE: the modifier 'static' has ben removed
  //noinspection JSMethodCanBeStatic
  private function getCatalogRoot():Content {
    var storeExpression:ValueExpression = catalogHelper.getActiveStoreExpression();
    return CatalogStudioPluginBase.getCatalogRootForStore(storeExpression);
  }

  public function toString():String {
    return "RepositoryCatalogTreeModel";
  }

  override public function getTreeId():String {
    return REPOSITORY_CATALOG_TREE_ID;
  }
}
}
