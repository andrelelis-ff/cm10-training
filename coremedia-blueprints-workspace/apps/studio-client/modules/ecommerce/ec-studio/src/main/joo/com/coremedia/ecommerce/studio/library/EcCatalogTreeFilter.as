package com.coremedia.ecommerce.studio.library {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.configuration.TreeFilter;
import com.coremedia.cms.editor.sdk.collectionview.tree.CompoundChildTreeModel;
import com.coremedia.cms.editor.sdk.collectionview.tree.CompoundTreeModel;
import com.coremedia.cms.editor.sdk.collectionview.tree.RepositoryTreeModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ecommerce.studio.components.preferences.CatalogPreferencesBase;
import com.coremedia.ui.models.TreeModel;

/**
 * Filters products from the content library tree.
 * Hides deleted content from the RepositoryCatalogTree.
 */
public class EcCatalogTreeFilter implements TreeFilter {
  private var folderName:String;

  public function EcCatalogTreeFilter(folderName:String) {
    this.folderName = folderName;
  }

  public function filter(treeModel:TreeModel, child:Object):Boolean {
    //Check if the tree model is enabled. If not, the whole tree isn't visible anyway.
    if (treeModel && child is Content) {

      if (treeModel is CompoundChildTreeModel && !(treeModel as CompoundChildTreeModel).isEnabled()) {
        return true;
      }

      var content:Content = child as Content;
      if (content) {
        var repositoryTreeModel:RepositoryTreeModel = treeModel as RepositoryTreeModel;
        if (repositoryTreeModel) {
          return false;
        }
      }

      if (false && !content.getPath()) {
        return undefined;
      }

      return filterContent(treeModel, content);
    }
    return false;
  }


  /**
   * Checks the settings state for the catalog tree, the location of the content and the origin site
   * if the given content should be hidden. Since the filter is also used for the library list/thumbnail view it
   * is not sufficient only to check if the TreeModel is enabled. If the catalog should be hidden, we have to show
   * the products instead.
   * @param treeModel the tree model the filter is executed for
   * @param content the content that should be displayed/filtered
   * @return true if the content should be hidden, undefined if the content or other properties not loaded yet.
   */
  private function filterContent(treeModel:TreeModel, content:Content):Boolean {
    var rootId:String = treeModel.getRootId();
    if (rootId === undefined) {
      return undefined;
    }

    var prefs:Struct = editorContext.getPreferences();
    if (!prefs) {
      return undefined;
    }

    //if the product and categories should be displayed in the content three, then the filter can be disabled
    var showCatalogAsContent:Boolean = prefs.get(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY);
    if (showCatalogAsContent !== undefined && showCatalogAsContent) {
      return false;
    }

    if (rootId == RepositoryTreeModel.REPOSITORY_ROOT_ID) {
      //check if there is currently a CoreMedia store selected

      //if the content belongs to the active site, check if it is a content and should be hidden
      if (content.getPath().indexOf("/" + folderName) !== -1) {
        var sites:Array = editorContext.getSitesService().getSites();
        if (sites === undefined) {
          return undefined;
        }
        for each(var site:Site in sites) {
          var path:String = site.getSiteRootFolder().getPath() + "/" + folderName;
          if (path === content.getPath()) {
            return true;
          }
        }
      }
    }

    return false;
  }
}
}
