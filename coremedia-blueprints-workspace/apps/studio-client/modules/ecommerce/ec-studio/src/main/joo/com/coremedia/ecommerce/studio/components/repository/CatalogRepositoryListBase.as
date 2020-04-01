package com.coremedia.ecommerce.studio.components.repository {
import com.coremedia.ecommerce.studio.components.AbstractCatalogList;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.Marketing;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.RemoteBeanUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.util.EventUtil;

import ext.grid.column.Column;
import ext.grid.header.HeaderContainer;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class CatalogRepositoryListBase extends AbstractCatalogList {

  /**
   * value expression for the selected node in the library tree
   */
  [Bindable]
  public var selectedNodeValueExpression:ValueExpression;

  private var sortInfo:Object = {};

  /**
   * When a user opens a context menu on a TablePanel, an object with the properties rowIndex, columnIndex,
   * record and columnDateIndex about the clicked table cell is written to the value expression.
   */
  private var lastClickedCellVE:ValueExpression;

  public function CatalogRepositoryListBase(config:CatalogRepositoryList = null) {
    super(config);
    on('afterrender', bindStoreAndView);
    selectedNodeValueExpression.addChangeListener(selectionChanged);
  }


  override protected function beforeDestroy():void {
    selectedNodeValueExpression.removeChangeListener(selectionChanged);

    super.beforeDestroy();
  }

  private function selectionChanged():void {
    var value:RemoteBean = selectedNodeValueExpression.getValue();
    if(value is Marketing) {
      getView()['emptyText'] = resourceManager.getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'CatalogView_spots_selection_empty_text');
    }
    else {
      getView()['emptyText'] = resourceManager.getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'CatalogView_empty_text');
    }
    getView().refresh();
  }

  private function bindStoreAndView():void {
    on('sortchange', sortChanged);
    getCatalogItemsValueExpression().addChangeListener(catalogItemsChanged);
    // TODO Ext 6 list sorters, see CMS-7895
    // getStore().setDefaultSort('id', 'ASC');
  }

  //noinspection JSUnusedLocalSymbols
  private function sortChanged(headerContainer:HeaderContainer, column:Column, direction:String, eOpts:Object):void {
    sortInfo.field = column.getSortParam();
    sortInfo.direction = direction;
    if (sortInfo.field === 'name'|| sortInfo.direction === "DESC") {
      loadCurrentBeans();
    }
  }

  private function catalogItemsChanged():void {
    if (sortInfo.field === "name" || sortInfo.direction === "DESC") {
      loadCurrentBeans();
    }
  }

  private function loadCurrentBeans():void {
    // start loading all RemoteBeans in this view...
    // and afterwards sort the Store...
    var beans:Array = getCatalogItemsValueExpression().getValue();
    if (beans && beans.length > 0) {
      RemoteBeanUtil.loadAll(function():void {
        EventUtil.invokeLater(function():void {
          getStore().sort(sortInfo.field, sortInfo.direction);
        });
      }, beans);
    }
  }

  internal function getCatalogItemsValueExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      return CatalogHelper.getInstance().getChildren(selectedNodeValueExpression.getValue());
    });
  }

  internal function getLastClickedCellVE():ValueExpression {
    if(!lastClickedCellVE){
      lastClickedCellVE = ValueExpressionFactory.createFromValue();
    }
    return lastClickedCellVE;
  }
}
}
