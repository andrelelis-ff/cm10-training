package com.coremedia.catalog.studio.library {
import com.coremedia.cms.editor.sdk.collectionview.tree.RepositoryTreeDragDropModel;
import com.coremedia.ui.models.TreeModel;

/**
 * Drag and Drop model for the content based catalog tree.
 * The catalog tree needs a separate DragDropModel because of the TreeModel
 * instance that is passed in the constructor. When a drag is executed the tree model of the
 * source and the target are checked if they are equal. Otherwise, the drag and drop action is not allowed.
 */
public class RepositoryCatalogTreeDragDropModel extends RepositoryTreeDragDropModel {

  public function RepositoryCatalogTreeDragDropModel(treeModel:TreeModel) {
    super(treeModel);
  }

  override public function toString():String {
    return "DnD Model for the Repository Catalog";
  }
}
}