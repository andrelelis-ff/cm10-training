package com.coremedia.livecontext.asset.studio.components {
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class CommerceReferencesFormBase extends PropertyFieldGroup{
  private static const PROPERTIES:String = 'properties';
  private static const LOCAL_SETTINGS_STRUCT_NAME:String = 'localSettings';
  private static const COMMERCE_STRUCT_NAME:String = 'commerce';
  private static const PRODUCTS_LIST_NAME:String = CatalogHelper.REFERENCES_LIST_NAME;
  public static const PROPERTY_NAME:String = LOCAL_SETTINGS_STRUCT_NAME + '.' + COMMERCE_STRUCT_NAME + '.' + PRODUCTS_LIST_NAME;
  private static const INHERIT_PROPERTY_NAME:String = 'inherit';

  private var inheritedExpression:ValueExpression;
  public function CommerceReferencesFormBase(config:CommerceReferencesForm = null) {
    super(config);
  }

  protected function getShopExpression(config:CommerceReferencesForm):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():String {
      var store:Store = Store(CatalogHelper.getInstance().getStoreForContentExpression(config.bindTo).getValue());
      return store && store.getName() && !CatalogHelper.getInstance().isCoreMediaStore(store);
    });
  }

  protected function getReadOnlyExpression(config:CommerceReferencesForm):ValueExpression {
    return ValueExpressionFactory.createFromFunction(getReadOnlyFunction(config));
  }

  protected function getReadOnlyFunction(config:CommerceReferencesForm):Function {
    return function():Boolean {
      //are we forced to set read-only?
      if (config.forceReadOnlyValueExpression.getValue()) {
        return true;
      }
      if (config.bindTo) {
        return !!getInheritedExpression(config).getValue();
      }
      return false;
    }
  }

  protected function getInheritedExpression(config:CommerceReferencesForm):ValueExpression {
    if (!inheritedExpression) {
      inheritedExpression = config.bindTo.extendBy(PROPERTIES, LOCAL_SETTINGS_STRUCT_NAME, COMMERCE_STRUCT_NAME, INHERIT_PROPERTY_NAME);
    }
    return inheritedExpression;
  }


  protected function createStructs():void {
    var localSettingsStructExpression:ValueExpression = bindTo.extendBy(PROPERTIES, LOCAL_SETTINGS_STRUCT_NAME);
    localSettingsStructExpression.loadValue(function():void {
      var localSettingsStruct:Struct = localSettingsStructExpression.getValue();
      RemoteBean(localSettingsStruct).load(function():void {
        if (!localSettingsStruct.get(COMMERCE_STRUCT_NAME)) {
          localSettingsStruct.getType().addStructProperty(COMMERCE_STRUCT_NAME);
        }

        var commerceStruct:Struct = localSettingsStruct.get(COMMERCE_STRUCT_NAME);
        var productsStruct:Struct = commerceStruct.get(PRODUCTS_LIST_NAME);
        if (!productsStruct) {
          commerceStruct.getType().addStringListProperty(PRODUCTS_LIST_NAME, 1000000);
        }
      });
    });
  }

}
}