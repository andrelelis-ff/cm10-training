package com.coremedia.blueprint.segments;

import com.coremedia.cap.content.Content;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * ContentSegmentStrategy for CMPerson
 */
public class CMPersonSegmentStrategy extends CMLinkableSegmentStrategy {
  private static final String DISPLAY_NAME = "displayName";
  private static final String FIRST_NAME = "firstName";
  private static final String LAST_NAME = "lastName";

  /**
   * Use segment, displayName, firstName+lastName, title or id as segment.
   */
  @NonNull
  @Override
  public String segment(@NonNull Content content) {
    String value = getSomeString(content, false, SEGMENT, DISPLAY_NAME);
    if (StringUtils.isBlank(value)) {
      value = (nullToEmpty(content.getString(FIRST_NAME)) + " " + nullToEmpty(content.getString(LAST_NAME))).trim();
    }
    if (StringUtils.isBlank(value)) {
      value = getSomeString(content, true, TITLE);
    }
    return value;
  }

  private static String nullToEmpty(String str) {
    return str==null ? "" : str;
  }
}
