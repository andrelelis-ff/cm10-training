package com.coremedia.catalog.studio.repository {

import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.collectionview.SortableSwitchingContainer;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class RepositoryCatalogSearchListContainerBase extends SortableSwitchingContainer {

  public function RepositoryCatalogSearchListContainerBase(config:RepositoryCatalogSearchListContainer = null) {
    super(config);
  }

  internal function getActiveViewExpression():ValueExpression {
    var collectionViewModel :CollectionViewModel = editorContext.getCollectionViewModel();
    return ValueExpressionFactory.create(CollectionViewModel.VIEW_PROPERTY, collectionViewModel.getMainStateBean());
  }
}
}
