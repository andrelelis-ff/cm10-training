package com.coremedia.ecommerce.studio.components.link {
import com.coremedia.ui.components.SwitchingContainer;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class ReadOnlyCatalogLinkPropertyFieldBase extends SwitchingContainer {

  protected static const READ_ONLY_CATALOG_LINK_ITEM_ID:String = 'readOnlyCatalogLink';
  protected static const READ_ONLY_CATALOG_LINK_EMPTY_DISPLAYFIELD_ITEM_ID:String = 'readOnlyCatalogLinkEmptyDisplayField';

  public function ReadOnlyCatalogLinkPropertyFieldBase(config:ReadOnlyCatalogLinkPropertyField = null) {
    super(config);
  }

  internal function getActiveCatalogLinkPropertyValueExpression(config:ReadOnlyCatalogLinkPropertyField):ValueExpression {
    return ValueExpressionFactory.createFromFunction(getActiveCatalogLinkProperty, config);
  }

  private function getActiveCatalogLinkProperty(config:ReadOnlyCatalogLinkPropertyField):String {
    var valueExpression:ValueExpression = config.bindTo.extendBy(config.propertyName);
    var values:Array = valueExpression.getValue();
    if (values && values.length != 0) {
      return READ_ONLY_CATALOG_LINK_ITEM_ID;
    }
    return READ_ONLY_CATALOG_LINK_EMPTY_DISPLAYFIELD_ITEM_ID;
  }

}
}
