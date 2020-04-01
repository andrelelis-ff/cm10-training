package com.coremedia.livecontext.elastic.social.studio {

import com.coremedia.elastic.social.studio.model.Contribution;
import com.coremedia.elastic.social.studio.model.ContributionAdministrationPropertyNames;
import com.coremedia.elastic.social.studio.model.impl.AbstractContributionAdministration;
import com.coremedia.elastic.social.studio.moderation.shared.details.base.ContentInformationContainer;
import com.coremedia.ui.components.IconDisplayField;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.container.Container;
import ext.form.Label;
import ext.form.field.DisplayField;
import ext.panel.Panel;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.icons.CoreIcons')]
public class ProductInformationContainerBase extends Container {
  protected static const TARGET_LABEL_ID:String = "cm-elastic-social-target-label";
  protected static const TARGET_BUTTON_ICON_ITEM_ID:String = "cm-elastic-social-target-icon";
  private static const ICON_CLS:String = ResourceManager.getInstance().getString('com.coremedia.icons.CoreIcons', 'type_product');

  private var moderationContributionAdministrationImpl:AbstractContributionAdministration;
  private var targetIconDisplayField:IconDisplayField;
  private var targetLabel:DisplayField;
  private var displayedContributionValueExpression:ValueExpression;

  public function ProductInformationContainerBase(config:ContentInformationContainer = null) {
    moderationContributionAdministrationImpl = config.contributionAdministration as AbstractContributionAdministration;

    displayedContributionValueExpression = ValueExpressionFactory.create(
            ContributionAdministrationPropertyNames.DISPLAYED, moderationContributionAdministrationImpl);

    super(config);
  }

  override protected function afterRender():void {
    super.afterRender();
    displayedContributionValueExpression.addChangeListener(toggleTarget);
    displayedContributionValueExpression.addChangeListener(setContentTypeIconCssClass);
    setContentTypeIconCssClass();
    toggleTarget();
  }

  private function toggleTarget():void {
    var contribution:Contribution = moderationContributionAdministrationImpl.getDisplayed();
    if (contribution && contribution.getTarget()) {
      getTargetIcon().show();
      getTargetLabel().show();
    } else {
      getTargetIcon().hide();
      getTargetLabel().hide();
    }
    this.updateLayout();
  }

  private function setContentTypeIconCssClass():void {
    if (moderationContributionAdministrationImpl) {
      var displayed:Contribution = moderationContributionAdministrationImpl.getDisplayed();
      if (displayed && displayed.getTarget) {
          displayed.getTarget(function (target:*):void {
            //
          });
      }
      getTargetIcon().iconCls = ICON_CLS;
    }
  }

  private function getTargetIcon():IconDisplayField {
    if (!targetIconDisplayField) {
      targetIconDisplayField = queryById(TARGET_BUTTON_ICON_ITEM_ID) as IconDisplayField;
    }

    return targetIconDisplayField;
  }

  private function getTargetLabel():DisplayField {
    if (!targetLabel) {
      targetLabel = queryById(TARGET_LABEL_ID) as DisplayField;
    }

    return targetLabel;
  }

  override protected function beforeDestroy():void {
    displayedContributionValueExpression && displayedContributionValueExpression.removeChangeListener(toggleTarget);
    displayedContributionValueExpression && displayedContributionValueExpression.removeChangeListener(setContentTypeIconCssClass);
    super.beforeDestroy();
  }
}
}
