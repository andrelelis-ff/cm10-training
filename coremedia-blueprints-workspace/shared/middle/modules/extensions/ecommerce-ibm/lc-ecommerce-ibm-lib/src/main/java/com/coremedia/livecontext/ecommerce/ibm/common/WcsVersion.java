package com.coremedia.livecontext.ecommerce.ibm.common;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Enumeration type for WCS versions.
 */
public enum WcsVersion {

  WCS_VERSION_UNKNOWN(0.0F),
  WCS_VERSION_7_6(7.6F),
  WCS_VERSION_7_7(7.7F),
  WCS_VERSION_7_8(7.8F),
  WCS_VERSION_8_0(8.0F),
  WCS_VERSION_9_0(9.0F);

  private final float version;

  WcsVersion(float version) {
    this.version = version;
  }

  /**
   * Lookup the enum instance for the given version String.
   *
   * @param wcsVersion a version String, e.g., 7.6
   * @return the optional wcversion enum instance
   */
  @NonNull
  public static Optional<WcsVersion> fromVersionString(@NonNull String wcsVersion) {
    return tryParseFloat(wcsVersion).flatMap(WcsVersion::findWcsVersion);
  }

  @NonNull
  private static Optional<Float> tryParseFloat(@NonNull String wcsVersion) {
    try {
      float parsed = Float.parseFloat(wcsVersion);
      return of(parsed);
    } catch (NumberFormatException ignored) {
      return empty();
    }
  }

  @NonNull
  private static Optional<WcsVersion> findWcsVersion(@NonNull Float value) {
    return Stream.of(values()).filter(version1 -> value.compareTo(version1.version) == 0).findFirst();
  }

  @NonNull
  public String toVersionString() {
    return Float.toString(version);
  }

  /**
   * Test whether or not this WCS version is less than the given WCS version
   *
   * @param wcsVersion another WCS version
   * @return true if this WCS version is less than the given WCS version
   */
  public boolean lessThan(@NonNull WcsVersion wcsVersion) {
    return ordinal() < wcsVersion.ordinal();
  }
}
