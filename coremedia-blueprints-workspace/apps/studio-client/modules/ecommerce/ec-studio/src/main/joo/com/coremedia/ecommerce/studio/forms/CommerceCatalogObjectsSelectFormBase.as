package com.coremedia.ecommerce.studio.forms {
import com.coremedia.ecommerce.studio.components.CommerceObjectSelector;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.store.BeanRecord;

import ext.Component;
import ext.container.Container;
import ext.form.FieldContainer;

public class CommerceCatalogObjectsSelectFormBase extends FieldContainer{

  [Bindable]
  public var bindTo:ValueExpression;
  [Bindable]
  public var forceReadOnlyValueExpression:ValueExpression;

  private var storeForContentExpression:ValueExpression;
  private var catalogObjectsExpression:ValueExpression;

  public function CommerceCatalogObjectsSelectFormBase(config:CommerceCatalogObjectsSelectForm = null) {
    super(config);
    getStoreForContentExpression().addChangeListener(adjustLabel);
  }

  override protected function onDestroy():void {
    getStoreForContentExpression().removeChangeListener(adjustLabel);
    super.onDestroy();
  }

  override protected function afterRender():void {
    super.afterRender();
    on("add", adjustLabel);
    on("remove", adjustLabel);
    adjustLabel();
  }

  internal function getCatalogObjectsExpression(config:CommerceCatalogObjectsSelectForm):ValueExpression {
    if (!catalogObjectsExpression) {
      catalogObjectsExpression = CatalogHelper.getCatalogObjectsExpression(config.bindTo,
              config.catalogObjectIdListName,
              config.invalidMessage,
              config.catalogObjectIdsExpression);
    }
    return catalogObjectsExpression;
  }

  internal function getHandleSelectFunction(config:CommerceCatalogObjectsSelectForm):Function {
    return function (selector:CommerceObjectSelector, record:BeanRecord):void {
      CatalogHelper.addCatalogObject(config.bindTo, config.catalogObjectIdListName, record.getBean().get("id"), config.catalogObjectIdsExpression);
      selector.clearValue();
    };
  }

  internal static function getCatalogObjectKey(item:Bean):String {
    if (item is CatalogObject) {
      return CatalogObject(item).getUriPath();
    } else {
      //error handling: when the id is invalid then catalog object is just a bean with the id containing the invalid id
      return item.get('id');
    }
  }

  private function getStoreForContentExpression():ValueExpression {
    if (!storeForContentExpression) {
      storeForContentExpression =   CatalogHelper.getInstance().
              getStoreForContentExpression(bindTo);
    }
    return storeForContentExpression;
  }

  //////////custom functions to remove/add catalog object fields without touching the catalog object selector

  internal function removeCommerceObjectFields(container:Container):void {
    if (!container.itemCollection || container.itemCollection.length === 0) return;

    container.itemCollection.each(function(item:Component):void{
      //don't remove the selector and the error label
      if (item.getItemId() === CommerceCatalogObjectsSelectForm.SELECTOR_ITEM_ID ||
              item.getItemId() === CommerceCatalogObjectsSelectForm.NO_STORE_LABEL) {
        return;
      }
      container.remove(item, true);
    });
  }

  internal function addCommerceObjectFields(container:Container, components:Array):void {
    if (!components || components.length === 0) return;
    components.forEach(function(item:Component):void{
      // the index of the selector must be computed every time
      var selectorIndex:Number = container.itemCollection.indexOf(container.getComponent(CommerceCatalogObjectsSelectForm.SELECTOR_ITEM_ID));
      // add the item just before the selector
      container.insert(selectorIndex, item);
    });
  }

  private function adjustLabel():void {
    if (getStoreForContentExpression().isLoaded()) {
      doAdjustLabel(getStoreForContentExpression().getValue());
    } else {
      getStoreForContentExpression().loadValue(doAdjustLabel);
    }
  }

  private function doAdjustLabel(store:Store):void {
    //show 'no store' label if no store available
    itemCollection.each(function (item:Component, index:Number):void {
      var container:Container = item as Container;
      if (container) {
        container.setVisible(store);
      }
    });
    getComponent(CommerceCatalogObjectsSelectForm.NO_STORE_LABEL).setVisible(!store);
  }

}
}