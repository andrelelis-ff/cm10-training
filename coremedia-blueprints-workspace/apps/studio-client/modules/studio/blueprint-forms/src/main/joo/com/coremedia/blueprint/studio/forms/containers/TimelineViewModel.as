package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.ui.data.impl.BeanImpl;

/**
 * Declares an observable with properties and their default values for the startTimeMillis feature.
 */
public class TimelineViewModel extends BeanImpl {

  public static const TIMELINE_PROPERTY_NAME:String = "startTimeMillis";

  public function TimelineViewModel() {
    //empty constructor
  }

  public native function get startTimeMillis():Number;

  public native function set startTimeMillis(value:Number):void;
}
}
