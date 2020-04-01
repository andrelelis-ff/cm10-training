package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.cap.struct.Struct;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.common.personaldata.PolyPersonalData;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

public class CMPersonImpl extends CMPersonBase {
  @Override
  public @PersonalData String getDisplayName() {
    @PersonalData String displayName = super.getDisplayName();
    if (!StringUtils.isBlank(displayName)) {
      return displayName.trim();
    }
    return (nullToEmpty(getFirstName()) + " " + nullToEmpty(getLastName())).trim();
  }

  @Override
  @SuppressWarnings("PersonalData") // Only the fallback to displayName includes PersonalData, which can be ignored here.
  public String getHtmlTitle() {
    String title = super.getHtmlTitle();
    if (StringUtils.isBlank(title)) {
      title = getDisplayName();
    }
    return title;
  }

  @Override
  @SuppressWarnings("PersonalData") // Only the fallback to displayName includes PersonalData, which can be ignored here.
  public String getTeaserTitle() {
    String title = super.getTeaserTitle();
    if (StringUtils.isBlank(title)) {
      title = getDisplayName();
    }
    return title;
  }

  public @PersonalData Map<String, Object> getFurtherDetails() {
    @PersonalData Struct misc = getMisc();
    return misc != null ? misc.toNestedMaps() : Collections.emptyMap();
  }

  private static @PolyPersonalData String nullToEmpty(@PolyPersonalData String str) {
    return str == null ? "" : str;
  }
}
