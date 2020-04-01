package com.coremedia.livecontext.studio.plugins {

import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.desktop.WorkArea;
import com.coremedia.cms.editor.sdk.preview.PreviewPanel;
import com.coremedia.cms.editor.sdk.util.ContentLocalizationUtilInternal;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.CatalogObjectPropertyNames;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.mixins.ValidationState;
import com.coremedia.ui.mixins.ValidationStateMixin;

import ext.Component;
import ext.StringUtil;
import ext.plugin.AbstractPlugin;

import js.Event;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class PreviewPanelTimeZoneValidationPlugin extends AbstractPlugin {

  /**
   * The model for the date, time and timezone
   */
  [Bindable]
  public var model:Bean;

  [Bindable]
  public var previewPanel:PreviewPanel;

  private var timeZoneIdValueExpression:ValueExpression;

  private var validationStateMixin:ValidationStateMixin;

  private var warningValueExpression:ValueExpression;

  public function PreviewPanelTimeZoneValidationPlugin(config:PreviewPanelTimeZoneValidationPlugin = null) {
    super(config);
    this.model = config.model;
    model.addPropertyChangeListener("timeZone", fillWarningValueExpression);

    previewPanel.addListener("beforedestroy", function ():void {
      model.removePropertyChangeListener("timeZone", fillWarningValueExpression);
      getWarningValueExpression().removeChangeListener(applyWarning);
    });
  }

  override public function init(host:Component):void {
    super.init(host);
    validationStateMixin = host as ValidationStateMixin;
    if (validationStateMixin) {
      getWarningValueExpression().addChangeListener(applyWarning);
    }
    fillWarningValueExpression()
  }



  private function getTimeZoneIdValueExpression():ValueExpression {
    if (!timeZoneIdValueExpression) {
      timeZoneIdValueExpression = ValueExpressionFactory.createFromFunction(function ():String {
        var text:String;
        var entityExpression:ValueExpression = previewPanel.getCurrentPreviewContentValueExpression();
        var storeExpression:ValueExpression;
        if (entityExpression.getValue() is Content) {
          storeExpression = CatalogHelper.getInstance().getStoreForContentExpression(WorkArea.ACTIVE_CONTENT_VALUE_EXPRESSION);
        } else if (entityExpression.getValue() is CatalogObject) {
          storeExpression = entityExpression.extendBy(CatalogObjectPropertyNames.STORE);
        }

        if (storeExpression && storeExpression.getValue()) {
          var timeZoneId:String = Store(storeExpression.getValue()).getTimeZoneId();
          if (timeZoneId) {
            text = timeZoneId;
          }
        }

        return text;
      });
    }
    return timeZoneIdValueExpression;
  }

  private function getWarningValueExpression():ValueExpression{
    if(!warningValueExpression){
      warningValueExpression = ValueExpressionFactory.createFromValue("");
    }
    return warningValueExpression;
  }

  internal function fillWarningValueExpression():void {
      var commerceTimeZoneId:String = getTimeZoneIdValueExpression().getValue();
      if (commerceTimeZoneId) {
        var timeZoneId:String = model.get("timeZone");
        if (timeZoneId !== commerceTimeZoneId) {
          getWarningValueExpression().setValue(StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'Preview_Wcs_Timezone_Divergation_Warning_Message'),
            ContentLocalizationUtilInternal.localizeTimeZoneID(commerceTimeZoneId)));
        } else {
          getWarningValueExpression().setValue(null);
        }

      }

  }

  private function applyWarning():void {
    var warningMessage:String = warningValueExpression.getValue();
    if (warningMessage) {
      validationStateMixin.validationState = ValidationState.WARNING;
      validationStateMixin.validationMessage = warningMessage;
    } else {
      validationStateMixin.validationMessage = null;
      validationStateMixin.validationState = null;
    }

  }
}
}
