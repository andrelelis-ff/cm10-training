package com.coremedia.blueprint.caefeeder;

import com.coremedia.cap.feeder.bean.PropertyConverter;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ValidFromPropertyConverter implements PropertyConverter {
  /**
   * Convert the given value.
   *
   * @param value value
   * @return converted value
   */
  @Override
  public Object convertValue(Object value) {
    if (value != null && !(value instanceof Calendar)) {
      throw new IllegalArgumentException("value needs to be a Calendar not a " + value.getClass());
    }
    Calendar cal = null;
    if (value == null) {
      cal = GregorianCalendar.getInstance();
      cal.set(Calendar.YEAR, 1970);  //NOSONAR
      cal.set(Calendar.MONTH, Calendar.JANUARY);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.set(Calendar.HOUR_OF_DAY, 1);
      cal.set(Calendar.MINUTE, 1);
      cal.set(Calendar.SECOND, 1);
      cal.set(Calendar.MILLISECOND, 1);
    } else {
      cal = (Calendar) value;
    }
    return cal;
  }

  @Override
  public Class convertType(Class type) {
    return Calendar.class;
  }
}
