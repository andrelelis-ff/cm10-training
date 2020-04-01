package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.ui.data.impl.BeanImpl;

/**
 * Declares an observable with properties and their default values for the call-to-action feature.
 */
public class CallToActionViewModel extends BeanImpl {

  public static const CTA_ENABLED_PROPERTY_NAME:String = "CTAEnabled";
  public static const CTA_TEXT_PROPERTY_NAME:String = "CTAText";

  public function CallToActionViewModel() {
  }

  public native function get CTAEnabled():Boolean;

  public native function set CTAEnabled(value:Boolean):void;

  public native function get CTAText():String;

  public native function set CTAText(value:String):void;
}
}
