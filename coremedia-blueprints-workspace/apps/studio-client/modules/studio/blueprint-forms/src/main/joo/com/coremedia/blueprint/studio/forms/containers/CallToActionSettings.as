package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.PropertiesWithDefaultsAdapterBase;

/**
 * Declares an observable with properties and their default values for the call-to-action feature.
 *
 * The settings will be stored in the bean the given {@link ValueExpression} provides using the following properties:
 *
 * {@link #CallToActionSettings#CALL_TO_ACTION_ENABLED_PROPERTY_NAME} specifies if the cta feature is enabled.
 * {@link #CallToActionSettings#CALL_TO_ACTION_CUSTOM_TEXT_PROPERTY_NAME} specifies a custom cta text (optional).
 *
 * Also supports a legacy settings format via the constructor parameter "legacy". If set to true the internal settings
 * format is changed ("callToActionEnabled" is replaced with "callToActionDisabled").
 * The difference is that the legacy format assumes that the cta feature is always enabled while the new format only
 * enables the cta feature when enabled is set to true. The difference is only made internally (when storing and
 * retrieving the settings), always use the new settings format to access the properties, the old format will be
 * transformed accordingly when storing the settings in the provided bean.
 */
public class CallToActionSettings extends PropertiesWithDefaultsAdapterBase {

  // legacy property name
  private static const CALL_TO_ACTION_DISABLED_PROPERTY_NAME:String = "callToActionDisabled";

  public static const CALL_TO_ACTION_ENABLED_PROPERTY_NAME:String = "callToActionEnabled";
  public static const CALL_TO_ACTION_CUSTOM_TEXT_PROPERTY_NAME:String = "callToActionCustomText";

  private var legacy:Boolean;

  public function CallToActionSettings(ve:ValueExpression, legacy:Boolean = false) {
    super(ve,
        CALL_TO_ACTION_ENABLED_PROPERTY_NAME, false,
        CALL_TO_ACTION_CUSTOM_TEXT_PROPERTY_NAME, "",
        // legacy
        CALL_TO_ACTION_DISABLED_PROPERTY_NAME, false
    );
    this.legacy = legacy;

    if (legacy) {
      addListener(CALL_TO_ACTION_DISABLED_PROPERTY_NAME, ctaDisabledUpdated);
    }
  }

  private function ctaDisabledUpdated():void {
    fireEvent(CALL_TO_ACTION_ENABLED_PROPERTY_NAME, {});
  }

  public function get callToActionEnabled():Boolean {
    if (legacy) {
      return !getProperty(CALL_TO_ACTION_DISABLED_PROPERTY_NAME);
    }
    return getProperty(CALL_TO_ACTION_ENABLED_PROPERTY_NAME);
  }

  public function set callToActionEnabled(value:Boolean):void {
    if (legacy) {
      setProperty(CALL_TO_ACTION_DISABLED_PROPERTY_NAME, !value);
    } else {
      setProperty(CALL_TO_ACTION_ENABLED_PROPERTY_NAME, value);
    }
  }

  public function get callToActionCustomText():String {
    return getProperty(CALL_TO_ACTION_CUSTOM_TEXT_PROPERTY_NAME);
  }

  public function set callToActionCustomText(value:String):void {
    setProperty(CALL_TO_ACTION_CUSTOM_TEXT_PROPERTY_NAME, value);
  }

  override public function destroy(...params):void {
    if (legacy) {
      removeListener(CALL_TO_ACTION_DISABLED_PROPERTY_NAME, ctaDisabledUpdated);
    }
    super.destroy(params);
  }
}
}
