package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.PropertiesWithDefaultsAdapterBase;

/**
 * Declares an observable with properties and their default values for the startTimeMillis feature.
 */
public class TimelineSettings extends PropertiesWithDefaultsAdapterBase {

  public static const TIMELINE_PROPERTY_NAME:String = "startTimeMillis";

  public function TimelineSettings(ve:ValueExpression) {
    super(ve,
          TIMELINE_PROPERTY_NAME, null
    );
  }

  public function get startTimeMillis():Number {
    return getProperty(TIMELINE_PROPERTY_NAME);
  }

  public function set startTimeMillis(value:Number):void {
    setProperty(TIMELINE_PROPERTY_NAME, value);
  }
}
}
