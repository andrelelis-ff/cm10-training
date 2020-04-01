package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.elastic.social.rest.api.CategoryKeyAndDisplay;
import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.inject.Named;

@Named
public class CMChannelCategoryResolver implements CategoryResolver {

  // copied from content beans definitions
  public static final String CMCHANNEL_SEGMENT = "segment";
  public static final String CMCHANNEL_TITLE = "title";
  public static final String CMCHANNEL_DOCTYPE = "CMChannel";
  private static final String KEY_PREFIX = "key_";

  @Override
  public CategoryKeyAndDisplay resolve(@NonNull Content content) {
    if(handlesType(content.getType()) ) {
      String key = content.getString(CMCHANNEL_SEGMENT);
      //segment may be empty for category channels
      if(StringUtils.isEmpty(key)) {
        key = KEY_PREFIX + String.valueOf(IdHelper.parseContentId(content.getId()));
      }
      return new CategoryKeyAndDisplay(key, content.getString(CMCHANNEL_TITLE));
    }
    return null;
  }

  private boolean handlesType(ContentType contentType) {
    return contentType.isSubtypeOf(CMCHANNEL_DOCTYPE);
  }
}
