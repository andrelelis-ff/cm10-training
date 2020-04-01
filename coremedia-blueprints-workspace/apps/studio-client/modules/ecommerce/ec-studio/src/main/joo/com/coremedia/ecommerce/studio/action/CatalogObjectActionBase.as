package com.coremedia.ecommerce.studio.action {
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ui.actions.DependencyTrackedAction;
import com.coremedia.ui.actions.ValueExpressionAction;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.util.ArrayUtils;
import com.coremedia.ui.util.ObjectUtils;

/**
 * <p>An abstract <code>ext.Action</code> that performs a operation on the configured catalog items.</p>
 * <p>Extend this class for an catalog item action.</p>
 * <p>The action is disabled when there is no configured catalog item.</p>
 * <p>Override the method <code>isDisabledFor</code> to provide a more specific disable behaviour.</p>
 */
public class CatalogObjectActionBase extends DependencyTrackedAction implements ValueExpressionAction {
  private var catalogObjectExpression:ValueExpression;

  /**
   * @param config the config object
   */
  public function CatalogObjectActionBase(config:CatalogObjectAction = null) {
    super(config);
    catalogObjectExpression = config.catalogObjectExpression;
    if (!catalogObjectExpression) {
      throw new Error("catalogObjectExpression is not set");
    }
  }

  /**
   * @private
   */
  public function getValueExpression():ValueExpression {
    return catalogObjectExpression;
  }

  /**
   * Return whether this action is disabled on the given array of catalog objects.
   * Override this method to implement more specific disable behaviour.
   *
   * @param catalogObjects the array of catalog objects: never empty.
   * @return whether this action is disabled
   */
  protected function isDisabledFor(catalogObjects:Array):Boolean {
    return false;
  }

  protected function isHiddenFor(catalogObjects:Array):Boolean {
    return false;
  }

  private static function catalogObjectOnly(entities:Array):Array {
    return entities.filter(ObjectUtils.isA(CatalogObject));
  }

  override protected function calculateHidden():Boolean {
    var entities:Array = getEntities();
    var catalogObjects:Array = catalogObjectOnly(entities);
    if (catalogObjects.length < entities.length) {
      // Any non-Catalog in the current value? Hide me!
      return true;
    }
    return isHiddenFor(catalogObjects);
  }

  override protected function calculateDisabled():Boolean {
    var catalogObjects:Array = getCatalogObjects();
    return !catalogObjects || catalogObjects.length === 0 || isDisabledFor(catalogObjects);
  }

  /**
   * Return the catalog objects on which this action operates.
   * If there is no catalog object it returns an empty array.
   */
  protected function getCatalogObjects():Array {
    var value:* = getEntities();
    return value is CatalogObject ? [value as CatalogObject] : (value is Array) ? (value as Array).filter(ObjectUtils.isA(CatalogObject)) : [];
  }

  private function getEntities():* {
    return ArrayUtils.asArray(catalogObjectExpression.getValue());
  }
}
}
