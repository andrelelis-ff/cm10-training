package com.coremedia.livecontext.studio.desktop {
import com.coremedia.cap.struct.Struct;
import com.coremedia.ecommerce.studio.helper.AugmentationUtil;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.container.Container;


public class CommerceChildCategoriesFormBase extends Container{

  private static const PROPERTIES:String = 'properties';
  private static const LOCAL_SETTINGS_STRUCT_NAME:String = 'localSettings';
  private static const COMMERCE_STRUCT_NAME:String = 'commerce';
  private static const CHILDREN_LIST_NAME:String = 'children';
  private static const SELECT_CHILDREN_NAME:String = 'selectChildren';
  public static const CHILDREN_PROPERTY_NAME:String = LOCAL_SETTINGS_STRUCT_NAME + '.' + COMMERCE_STRUCT_NAME + '.' + CHILDREN_LIST_NAME;
  public static const SELECT_CHILDREN_PROPERTY_NAME:String = LOCAL_SETTINGS_STRUCT_NAME + '.' + COMMERCE_STRUCT_NAME + '.' + SELECT_CHILDREN_NAME;
  [Bindable]
  public var bindTo:ValueExpression;

  private var selectChildrenExpression:ValueExpression;
  private var categoryExpression:ValueExpression;

  public function CommerceChildCategoriesFormBase(config:CommerceChildCategoriesForm = null) {
    super(config);
  }

  override protected function onDestroy():void {
    selectChildrenExpression.removeChangeListener(copyChildrenFromCatalog);
    super.onDestroy();
  }

  internal function isSelectChildrenExpression(bindTo:ValueExpression):ValueExpression {
    if (!selectChildrenExpression) {
      selectChildrenExpression = bindTo.extendBy(PROPERTIES).extendBy(SELECT_CHILDREN_PROPERTY_NAME);
      selectChildrenExpression.addChangeListener(copyChildrenFromCatalog);
    }
    return selectChildrenExpression;
  }

  internal function isInheritExpression(bindTo:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function():Boolean {
      return !isSelectChildrenExpression(bindTo).getValue();
    });
  }

  internal function getCategoryExpression(bindTo:ValueExpression):ValueExpression {
    categoryExpression = AugmentationUtil.getCatalogObjectExpression(bindTo);
    return categoryExpression;
  }

  internal function createStructs():void {
    var localSettingsStructExpression:ValueExpression = bindTo.extendBy(PROPERTIES, LOCAL_SETTINGS_STRUCT_NAME);
    localSettingsStructExpression.loadValue(function():void {
      var localSettingsStruct:Struct = localSettingsStructExpression.getValue();
      RemoteBean(localSettingsStruct).load(function():void {
        if (!localSettingsStruct.get(COMMERCE_STRUCT_NAME)) {
          localSettingsStruct.getType().addStructProperty(COMMERCE_STRUCT_NAME);
        }

        var commerceStruct:Struct = localSettingsStruct.get(COMMERCE_STRUCT_NAME);
        var categoriesStruct:Struct = commerceStruct.get(CHILDREN_LIST_NAME);
        if (!categoriesStruct) {
          commerceStruct.getType().addStringListProperty(CHILDREN_LIST_NAME, 1000000);
        }
      });
    });
  }

  /**
   * copy the sub categories from the catalog to the list of selected children
   * if the list is empty in case of the switch from 'inherit' to 'select'
   */
  private function copyChildrenFromCatalog():void {
    if (selectChildrenExpression.getValue()) {
      var childrenExpression:ValueExpression = bindTo.extendBy(PROPERTIES).extendBy(CHILDREN_PROPERTY_NAME);
      if (childrenExpression.getValue() is Array) {
        //accept the previously saved children. nothing to do
      } else {
        createStructs();
        //copy from the catalog hierarchy.
        var category:Category = categoryExpression.getValue();
        var subCategories:Array = category.getSubCategories();
        if (subCategories && subCategories.length > 0) {
          childrenExpression.setValue(subCategories.map(function(subCategory:Category):String {
            return subCategory.getId();
          }));
        }

      }
    }
  }
}
}