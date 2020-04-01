package com.coremedia.catalog.studio.library {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.impl.BulkUndeleteMethod;
import com.coremedia.cap.content.results.BulkOperationResult;
import com.coremedia.cap.content.search.SearchParameters;
import com.coremedia.cap.content.search.SearchResult;
import com.coremedia.cap.content.search.SearchService;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.StringUtil;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.catalog.studio.CatalogStudioPlugin')]
public class CatalogUndelete {

  private var treeRelation:CatalogTreeRelation;
  private var contents:Array;
  private var callback:Function;

  public function CatalogUndelete(catalogTreeRelation:CatalogTreeRelation, contents:Array, callback:Function = undefined) {
    this.treeRelation = catalogTreeRelation;
    this.contents = contents;
    this.callback = callback;
  }

  public function execute():void {
    new BulkUndeleteMethod(contents[0].getRepository(), contents, updateRestoredItems).execute();
  }

  /**
   * Restores the linking for every restored content.
   * @param result the result of the undelete operation
   */
  private function updateRestoredItems(result:BulkOperationResult):void {
    if (result.successful) {
      for each(var content:Content in contents) {
        findParentAndRestoreLink(content);
      }
    }

    if (callback) {
      callback.call(null, result);
    }
  }

  /**
   * Looks up if there is a category in the same folder.
   * If not, the product is opened so that the user can link it manually.
   * Since there is a validator, the user will see what the problem is.
   */
  //TODO restoring check-in/out states
  private function findParentAndRestoreLink(catalogItem:Content):void {
    ValueExpressionFactory.createFromFunction(function ():Boolean {
      return findUndeletedParent(catalogItem);
    }).loadValue(function (category:Content):void {
      //no category found?
      if (category === null) {
        var path:String = getParentFolder(catalogItem).getPath();

        //then try to find a deleted parent that matches
        findDeletedParent(catalogItem, path, function (deletedParent:Content):void {
          if (deletedParent == null) {
            executeErrorHandling(catalogItem);
          }
          else {
            new BulkUndeleteMethod(catalogItem.getRepository(), [deletedParent], function (result:BulkOperationResult):void {
              if (result.successful) {
                //restore the linking to the catalog, maybe the link still exists for the restored
                //content but the method takes care of it and filters duplicates
                CatalogTreeRelationHelper.addCategoryChild(deletedParent, catalogItem);

                //we only restored the deleted parent until now, but we finally have to restore the linking for it too.
                //we do this by invoking all recursively
                findParentAndRestoreLink(deletedParent);
              }
              else {
                executeErrorHandling(catalogItem);
              }
            }).execute();
          }
        });
      }
      else {
        //restore the linking to the catalog, maybe the link still exists for the restored
        //content but the method takes care of it and filters duplicates
        CatalogTreeRelationHelper.addCategoryChild(category, catalogItem);
      }
    });
  }

  /**
   * The default error handling for restoring errors is to open the document.
   * This way, the user can restore the linking but also gets an hint about the problem.
   * @param content the content that has been restored, but could not been linked to a parent.
   */
  private function executeErrorHandling(content:Content):void {
    editorContext.getContentTabManager().openDocument(content);
    var title:String = ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'catalog_undelete_err_title');
    var message:String = StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'catalog_undelete_err_message'), content.getName());
    MessageBoxUtil.showInfo(title, message);
  }

  /**
   * Executes a search query to find a deleted category that last path matches the expected parent category path
   * @param catalogItem the catalog item to find the deleted parent category for
   * @param matchingPath the path where the category is expected
   * @param callback the callback called with the deleted category or null if no such value was found
   */
  private function findDeletedParent(catalogItem:Content, matchingPath:String, callback:Function):void {
    var site:Site = editorContext.getSitesService().getSiteFor(catalogItem);
    //http://localhost:40080/api/content/search?query=&contentType=Document_&folder=content%2F1&orderBy=type%20desc&orderBy=name%20desc&includeSubfolders=true&filterQuery=(status%3A3)&limit=-1&includeSubtypes=true&_dc=1442318843685
    var params:SearchParameters = SearchParameters({});
    params.folder = site.getSiteRootFolder().getUriPath();
    params.includeSubfolders = true;
    params.contentType = [CatalogTreeRelation.CONTENT_TYPE_CATEGORY];
    params.filterQuery = ["isdeleted:true"];
    params.query = "";

    var searchService:SearchService = editorContext.getSession().getConnection().getContentRepository().getSearchService();
    var result:SearchResult = searchService.search(params);
    result.load(function ():void {
      var hits:Array = result.getHits();
      for each(var hit:Content in hits) {
        if (hit.isDeleted() && !hit.isDestroyed() && hit.getLastParent() != null) {
          var pathInfo:String = getPathInfo(hit);
          if(pathInfo !== null && pathInfo === matchingPath) {
            callback.call(null, hit);
            return;
          }
        }
      }
      callback.call(null, null);
    });
  }

  /**
   * Returns the full path of the deleted content
   * @param deleted the deleted content
   * @return the formatted path
   */
  private function getPathInfo(deleted:Content):String {
    var pathArray:Array = [];
    var currentContent:Content = deleted.getLastParent();
    while (currentContent && currentContent.isRoot() === false) {
      if(currentContent.isDestroyed()) {
        return null;
      }
      var name:String = currentContent.getName();
      pathArray.push(name);
      if (currentContent.isDeleted()) {
        currentContent = currentContent.getLastParent();
      } else {
        currentContent = currentContent.getParent();
      }
    }
    pathArray.reverse();
    return '/' + pathArray.join('/');
  }

  /**
   * Returns the first document for the given type of the folder of the given catalog item.
   * @param catalogItem the un-deleted catalog item
   * @return the parent category or null if no such category could be resolved.
   */
  private function findUndeletedParent(catalogItem:Content):Content {
    var type:String = catalogItem.getType().getName();

    if (type == CatalogTreeRelation.CONTENT_TYPE_PRODUCT) {
      //the restored product still has the category link, so we only have to check if
      //one of the parent categories is not deleted too
      var categories:Array = treeRelation.getParents(catalogItem);
      for each(var parent:Content in categories) {
        if(!parent.isDeleted()) {
          return parent;
        }
      }
    }

    var children:Array = getParentFolder(catalogItem).getChildDocuments();
    for each(var child:Content in children) {
      if (!child.isLoaded()) {
        child.load();
        return undefined;
      }

      if (!child.getType()) {
        return undefined;
      }

      if (!child.getPath()) {
        return undefined;
      }

      if (child.getType().getName() == CatalogTreeRelation.CONTENT_TYPE_CATEGORY) {
        return child;
      }
    }
    return null;
  }

  /**
   * Returns the parent folder depending on the content type: for a product or category
   * @param content the content to retrieve the parent folder for
   */
  private function getParentFolder(content:Content):Content {
    var type:String = content.getType().getName();
    if (type == CatalogTreeRelation.CONTENT_TYPE_CATEGORY) {
      return content.getParent().getParent();
    }
    else if (type == CatalogTreeRelation.CONTENT_TYPE_PRODUCT) {
      return content.getParent();
    }
    return null;
  }
}
}