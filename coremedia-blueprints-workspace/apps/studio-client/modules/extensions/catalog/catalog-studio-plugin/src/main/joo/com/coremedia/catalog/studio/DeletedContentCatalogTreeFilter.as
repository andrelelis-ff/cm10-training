package com.coremedia.catalog.studio {
import com.coremedia.cap.content.Content;
import com.coremedia.catalog.studio.library.RepositoryCatalogTreeModel;
import com.coremedia.cms.editor.configuration.TreeFilter;
import com.coremedia.ui.models.TreeModel;

public class DeletedContentCatalogTreeFilter implements TreeFilter {
  public function DeletedContentCatalogTreeFilter() {
  }

  public function filter(treeModel:TreeModel, child:Object):Boolean {
    if (treeModel is RepositoryCatalogTreeModel && child is Content) {
      return Content(child).isDeleted();
    }
    return false;
  }
}
}
