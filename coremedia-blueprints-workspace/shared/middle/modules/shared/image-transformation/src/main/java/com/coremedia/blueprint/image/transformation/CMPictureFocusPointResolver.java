package com.coremedia.blueprint.image.transformation;

import com.coremedia.cap.common.InvalidPropertyValueException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.transform.FocusPoint;
import com.coremedia.cap.transform.ContentFocusPointResolver;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class CMPictureFocusPointResolver implements ContentFocusPointResolver {
  private static final String FOCUS_POINT_X = "x";
  private static final String FOCUS_POINT_Y = "y";
  private static final String FOCUS_POINT_STRUCT_NAME = "focusPoint";

  @NonNull
  @Override
  public Optional<FocusPoint> getFocusPoint(@NonNull Content content, @Nullable String property) {
    Optional<Struct> localSettings = getLocalSettings(content);
    return localSettings.flatMap(CMPictureFocusPointResolver::fetchFocusPoint);

  }

  @NonNull
  public Optional<Struct> getLocalSettings(@NonNull Content content) {
    Map<String, Object> properties = content.getProperties();
    if (!properties.containsKey("localSettings")) {
      return Optional.empty();
    }

    try {
      return Optional.ofNullable(content.getStruct("localSettings"));
    } catch (InvalidPropertyValueException e) {
      return Optional.empty();
    }
  }

  @NonNull
  private static Optional<FocusPoint> fetchFocusPoint(Struct localSettings) {
    if (!localSettings.getProperties().containsKey(FOCUS_POINT_STRUCT_NAME)) {
      return Optional.empty();
    }


    try {
      Struct focusPointStruct = localSettings.getStruct(FOCUS_POINT_STRUCT_NAME);
      Map<String, Object> pointStructProperties = focusPointStruct.getProperties();
      if (!pointStructProperties.containsKey(FOCUS_POINT_X) || !pointStructProperties.containsKey(FOCUS_POINT_Y)) {
        return Optional.empty();
      }
      String x = focusPointStruct.getString(FOCUS_POINT_X);
      String y = focusPointStruct.getString(FOCUS_POINT_Y);

      return Optional.of(new FocusPoint(Float.parseFloat(x), Float.parseFloat(y)));
    } catch (InvalidPropertyValueException e) {
      // That's ok, focusPoint is an optional setting.
      return Optional.empty();
    }
  }

  @Override
  public boolean isApplicable(@NonNull Content content, @Nullable String property) {
    return content.getType()
                  .isSubtypeOf("CMPicture");
  }
}
