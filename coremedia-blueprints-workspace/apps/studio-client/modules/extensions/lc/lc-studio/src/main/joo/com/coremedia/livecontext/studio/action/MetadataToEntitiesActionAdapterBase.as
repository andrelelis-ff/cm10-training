package com.coremedia.livecontext.studio.action {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.actions.metadata.MetadataBeanAction;
import com.coremedia.cms.editor.sdk.preview.MetadataHelper;
import com.coremedia.cms.editor.sdk.preview.PreviewContextMenu;
import com.coremedia.cms.editor.sdk.preview.metadata.MetadataTreeNode;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.actions.DependencyTrackedAction;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Action;
import ext.Component;
import ext.Ext;

/**
 * Adapter that implements a MetadataAction based on a backing action.
 *
 * All critical methods are delegated to the backing action after extracting
 * a bean from the underlying metadata. If no bean can be obtained from
 * the MetadataTreeNode (or one of it's parents if useParentNode is enabled), the
 * backing action is configured with metadata properties (if available).
 *
 * @see com.coremedia.cms.editor.sdk.actions.ContentAction
 */
public class MetadataToEntitiesActionAdapterBase extends MetadataBeanAction {

  private var backingAction:Action;
  private var store:Store;
  private var properties:Object;

  internal const resolvedBeanValueExpression:ValueExpression = ValueExpressionFactory.createFromValue();
  private var myContextMenu:PreviewContextMenu;
  private var component:Component;

  public function MetadataToEntitiesActionAdapterBase(config:MetadataToEntitiesActionAdapter = null) {
    backingAction = config.backingAction as Action;
    var setEntities:String = config.setEntities || "setContents";
    if(!(backingAction[setEntities] is Function)) {
      throw new Error("config param setEntities cannot be resolved to a function");
    }
    var newConfig:MetadataToEntitiesActionAdapter = MetadataToEntitiesActionAdapter(Ext.apply({
      iconCls: backingAction.getIconCls(),
      text: backingAction.getText(),
      handler: delegateToBackingAction
    }, config));
    super(newConfig);

    resolvedBeanValueExpression.addChangeListener(function(ve:ValueExpression):void {
      var resolvedBean:* = ve.getValue();
      var values:Array = [];
      if(resolvedBean is Content) {
        // activate content actions
        values.push(resolvedBean);
      } else if(resolvedBean === null) {
        // activate 'augment this' actions
        values.push(properties);
      }
      backingAction[setEntities](values);
    });
  }

  override protected function isDisabledFor(metadata:MetadataTreeNode):Boolean {
    extractBeanAndProperties(metadata);
    registerResetResolvedBeanHandler();
    if(backingAction is DependencyTrackedAction) {
      return backingAction['calculateDisabled']();
    }
    return false;
  }

  internal function registerResetResolvedBeanHandler():void {
    if (!myContextMenu) {
      myContextMenu = component.findParentByType(PreviewContextMenu.xtype) as PreviewContextMenu;
      myContextMenu.on('show', function ():void {
        // trigger reload
        resolvedBeanValueExpression.setValue(undefined);
      });
    }
  }
  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    component = comp;
  }

  private function delegateToBackingAction():void {
    extractBeanAndProperties(getMetadata());

    // copy items to delegate (if possible)
    if (items && items.length > 0) {
      backingAction['items'] = items;
    }
    backingAction.execute();
  }

  internal function resolveBeanForAction():void{
    var resolvedBean:* = resolvedBeanValueExpression.getValue();
    if(store !== null && resolvedBean === undefined) {
      var rb:RemoteBean = store.resolveShopUrlForPbe(properties.shopUrl);
      resolvedBeanValueExpression.setValue(rb);
    }
  }

  internal function extractBeanAndProperties(metadata:MetadataTreeNode):void {
    var children:Array = metadata.getChildren();
    if(!Ext.isEmpty(children)) {
      this.properties = MetadataHelper.getAllProperties(children[0]);
    }
    store = MetadataHelper.getBeanMetadataValue(metadata) as Store;
    resolveBeanForAction();
  }
}
}
