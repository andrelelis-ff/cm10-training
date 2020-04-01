package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.ui.data.impl.BeanImpl;

/**
 * Declares an observable with properties and their default values for the fixed index feature.
 */
public class FixedIndexViewModel extends BeanImpl {

  public static const INDEX_PROPERTY_NAME:String = "index";

  public function FixedIndexViewModel() {
    //empty constructor
  }

  public native function get index():Number;

  public native function set index(value:Number):void;
}
}
