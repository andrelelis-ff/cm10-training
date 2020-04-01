package com.coremedia.blueprint.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Validates a longitude-latitude value.
 */

public final class LatitudeLongitudeUtil {

  /**
   * Hide Utility Class Constructor
   */
  private LatitudeLongitudeUtil() {
  }

  private static final double MIN_LAT = -90;
  private static final double MAX_LAT = +90;
  private static final double MIN_LNG = -180;
  private static final double MAX_LNG = +180;
  private static final int LAT = 0;
  private static final int LNG = 1;

  private static final String SEPERATOR = ",";
  private static final String PATTERN = "(\\+|-)?\\d+(\\.\\d+)?" + SEPERATOR + "(\\+|-)?\\d+(\\.\\d+)?";

  private static final Logger LOG = LoggerFactory.getLogger(LatitudeLongitudeUtil.class);

  public enum Result {
    VALID, INVALID_LATITUDE, INVALID_LONGITUDE, INVALID_NUMBER_FORMAT, INVALID_FORMAT
  }

  public static Result validate(String geoCode) {
    if (!StringUtils.isEmpty(geoCode)) {
      if (Pattern.matches(PATTERN, geoCode)) {

        String[] latlng = geoCode.split(SEPERATOR);

        String lat = latlng[LAT];
        String lng = latlng[LNG];
        try {
          double doubleLat = Double.parseDouble(lat);
          double doubleLng = Double.parseDouble(lng);

          if (doubleLat < MIN_LAT || doubleLat > MAX_LAT) {
            return Result.INVALID_LATITUDE;
          }

          if (doubleLng < MIN_LNG || doubleLng > MAX_LNG) {
            return Result.INVALID_LONGITUDE;
          }
        } catch (NumberFormatException e) {
          LOG.warn("Error parsing the latitude and longitude strings to doubles. There may be a problem with the regular expression");
          return Result.INVALID_NUMBER_FORMAT;
        }

      } else {
        return Result.INVALID_FORMAT;
      }
    }
    return Result.VALID;
  }
}
