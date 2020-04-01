package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.PropertiesWithDefaultsAdapterBase;

/**
 * Declares an observable with properties and their default values for the fixed index feature.
 */
public class FixedIndexSettings extends PropertiesWithDefaultsAdapterBase {

  public static const INDEX_PROPERTY_NAME:String = "index";

  public function FixedIndexSettings(ve:ValueExpression) {
    super(ve,
          INDEX_PROPERTY_NAME, null
    );
  }

  public function get index():Number {
    return getProperty(INDEX_PROPERTY_NAME);
  }

  public function set index(value:Number):void {
    setProperty(INDEX_PROPERTY_NAME, value);
  }
}
}
