package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.components.IAnnotatedLinkListForm;

/**
 * Fires after the configuration has changed.
 */
[Event(name="CTAConfigurationChanged")] // NOSONAR - no type

public class CallToActionConfigurationFormBase extends PropertyFieldGroup implements IAnnotatedLinkListForm {

  public static const CTA_CONFIGURATION_CHANGED_EVENT:String = "CTAConfigurationChanged";

  /**
   * A value expression that leads to a bean storing the {@link CallToActionSettings}.
   */
  private var _settingsVE:ValueExpression;

  /**
   * If TRUE legacy CTA settings (described in {@link CallToActionSettings}) are used.
   */
  [Bindable]
  public var useLegacyCTASettings:Boolean;

  private var _ctaSettings:CallToActionSettings;

  private var _ctaViewModel:CallToActionViewModel;

  private var textDisabledVE:ValueExpression;

  public function CallToActionConfigurationFormBase(config:CallToActionConfigurationForm = null) {
    super(config);
    ctaSettings.addListener(CallToActionSettings.CALL_TO_ACTION_ENABLED_PROPERTY_NAME, settingsEnabledListener);
    ctaSettings.addListener(CallToActionSettings.CALL_TO_ACTION_CUSTOM_TEXT_PROPERTY_NAME, settingsTextListener);
    if (settingsVE.isLoaded()) {
      settingsEnabledListener();
      settingsTextListener();
    }

    forceReadOnlyValueExpression && forceReadOnlyValueExpression.addChangeListener(updateTextDisabledVE);
  }

  [Bindable]
  internal function get ctaSettings():CallToActionSettings {
    if (!_ctaSettings) {
      _ctaSettings = new CallToActionSettings(settingsVE, useLegacyCTASettings);
    }
    return _ctaSettings;
  }

  [Bindable]
  internal function get ctaViewModel():CallToActionViewModel {
    if (!_ctaViewModel) {
      _ctaViewModel = new CallToActionViewModel();
    }
    return _ctaViewModel;
  }

  private function settingsEnabledListener():void {
    fireEvent(CTA_CONFIGURATION_CHANGED_EVENT);
    updateTextDisabledVE();
  }

  private function settingsTextListener():void {
    fireEvent(CTA_CONFIGURATION_CHANGED_EVENT);
  }

  override protected function onDestroy():void {
    ctaViewModel.destroy();
    ctaSettings.destroy();
    super.onDestroy();
  }

  private function updateTextDisabledVE():void {
    var ctaTextDisabled:Boolean = calcTextDisabled(_ctaSettings && ctaSettings.callToActionEnabled);
    getTextDisabledVE().setValue(ctaTextDisabled);
  }

  private function calcTextDisabled(value:Boolean):Boolean {
    return !value && forceReadOnlyValueExpression && !forceReadOnlyValueExpression.getValue();
  }

  protected function getTextDisabledVE():ValueExpression {
    if (!textDisabledVE) {
      textDisabledVE = ValueExpressionFactory.createFromValue(false);
    }
    return textDisabledVE;
  }

  [Bindable]
  public function set settingsVE(settingsVE:ValueExpression):void {
    _settingsVE = settingsVE;
  }

  [Bindable]
  public function get settingsVE():ValueExpression {
    return _settingsVE;
  }
}
}
