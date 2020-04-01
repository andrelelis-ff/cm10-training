package com.coremedia.ecommerce.studio.components.repository {
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.container.Container;

public class CatalogRepositoryThumbnailsBase extends Container{
  /**
   * value expression for the selected folder in the library tree
   */
  [Bindable]
  public var selectedFolderValueExpression:ValueExpression;

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  public function CatalogRepositoryThumbnailsBase(config:CatalogRepositoryThumbnails = null) {
    super(config);
  }

  internal function getCatalogItemsValueExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      return CatalogHelper.getInstance().getChildren(selectedFolderValueExpression.getValue());
    });
  }

  public function disableBrowserContextMenu():void {
/* TODO Ext6, see CMS-7893
    var thumbViewPanel:* = this.el.down('div.catalog-thumb-data-view-panel');
    thumbViewPanel.on("contextmenu", Ext.emptyFn, null, {
      preventDefault: true
    });
*/
  }

}
}