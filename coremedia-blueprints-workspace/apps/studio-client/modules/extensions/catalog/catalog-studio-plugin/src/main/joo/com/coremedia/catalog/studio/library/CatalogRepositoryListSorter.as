package com.coremedia.catalog.studio.library {

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.search.SearchParameters;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.collectionview.sort.RepositoryListSorterImpl;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.BeanState;

/**
 * Extends the default RepositoryListSorterImpl to override the resolving of children.
 * The parent/children relationship differs for the catalog documents and is implemented here.
 */
public class CatalogRepositoryListSorter extends RepositoryListSorterImpl {
  private var extension:CatalogCollectionViewExtension;

  public function CatalogRepositoryListSorter(extension:CatalogCollectionViewExtension) {
    this.extension = extension;
  }

  override public function sort(folder:Content, children:Array):Array {
    var cvManager:CollectionViewManagerInternal = (editorContext.getCollectionViewManager() as CollectionViewManagerInternal);
    var sortValues:Array = cvManager.getCollectionView().getSortStateManager().getCurrentSortCriteria();
    return triggerSolrSort(folder, children, sortValues);
  }

  override protected function computeSearchParameters(folder:Content, sortValues:Array):SearchParameters {
    var searchParameters:SearchParameters = super.computeSearchParameters(folder, sortValues);
    return extension.applySearchParameters(folder, [], searchParameters);
  }

  override public function getChildren(folder:Content):Array {
    //get categories from the parent category
    if(!folder.isLoaded()) {
      folder.load();
      return undefined;
    }

    var linkedChildren:Array = folder.getProperties().get("children");
    if (linkedChildren === undefined) {
      return undefined;
    }


    //get the children of the selected category
    var linkingChildren:Array = folder.getReferrersWithNamedDescriptor("CMHasContexts", "contexts");
    if (linkingChildren === undefined) {
      return undefined;
    }

    return linkedChildren.concat(linkingChildren);
  }


  override public function filter(folder:Content, children:Array):Array {
    var returnUndefined:Boolean = false;
    children = children.filter(function (item:Content):Boolean {
      var state:BeanState = item.getState();
      if (state.readable === false) {
        return false;
      }

      var deleted:Boolean = item.isDeleted();
      if (deleted === undefined) {
        returnUndefined = true;
      }
      return deleted === false;
    });
    return returnUndefined ? undefined : children;
  }

}
}
