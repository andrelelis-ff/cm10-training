package com.coremedia.blueprint.viewtype;

import com.coremedia.cap.content.Content;
import org.apache.commons.lang3.StringUtils;

/**
 * Viewtype related features
 */
public class ViewtypeService {
  private static final String CM_VIEWTYPE = "CMViewtype";
  private static final String LAYOUT = "layout";


  // --- Features ---------------------------------------------------

  public String getLayout(Content content) {
    checkIsViewtype(content);

    // State of the art
    String layout = content.getString(LAYOUT);
    if (!StringUtils.isEmpty(layout)) {
      return layout;
    }

    // Backward compatibility
    return content.getName();
  }


  // --- internal ---------------------------------------------------

  private static void checkIsViewtype(Content bundle) {
    if (!bundle.getType().isSubtypeOf(CM_VIEWTYPE)) {
      throw new IllegalArgumentException(bundle + " is no CMViewtype");
    }
  }

}
