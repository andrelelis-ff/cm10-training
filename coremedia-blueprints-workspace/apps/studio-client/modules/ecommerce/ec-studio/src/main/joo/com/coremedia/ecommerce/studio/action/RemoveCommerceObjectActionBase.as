package com.coremedia.ecommerce.studio.action {
import com.coremedia.cms.editor.sdk.actions.ActionConfigUtil;
import com.coremedia.cms.editor.sdk.actions.ContentUpdateAction;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import mx.resources.ResourceManager;

/**
 * This action is intended to be used from within EXML, only.
 *
 */
[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class RemoveCommerceObjectActionBase extends ContentUpdateAction {

  private var commerceObject:CatalogObject;
  private var catalogObjectIdListName:String;
  private var catalogObjectIdsExpression:ValueExpression;

  /**
   * @param config the configuration object
   */
  public function RemoveCommerceObjectActionBase(config:RemoveCommerceObjectAction = null) {
    super(RemoveCommerceObjectAction(ActionConfigUtil.extendConfiguration(ResourceManager.getInstance().getResourceBundle(null, 'com.coremedia.ecommerce.studio.ECommerceStudioPlugin').content,
            config, config.actionName,
            {
              handler: removeCommerceObject
            })));

    commerceObject = config.commerceObject;
    catalogObjectIdListName = config.catalogObjectIdListName;
    catalogObjectIdsExpression = config.catalogObjectIdsExpression;
  }

  private function removeCommerceObject():void {
    var id:String;
    //error handling
    if (commerceObject is CatalogObject) {
      id = commerceObject.getId();
    } else {
      //error handling: when the id is invalid then the catalog object is just a bean with the id containing the invalid id
      id = commerceObject.get('id');
    }
    CatalogHelper.removeCatalogObject(ValueExpressionFactory.createFromValue(getContent()),
            catalogObjectIdListName, id, catalogObjectIdsExpression);

  }
}
}
