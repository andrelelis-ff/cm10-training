package com.coremedia.blueprint.image.transformation;

import com.coremedia.cap.common.InvalidPropertyValueException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.transform.ContentOperationsResolver;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

public class CMPictureOperationsResolver implements ContentOperationsResolver {
  private static final String TRANSFORMS = "transforms";

  @NonNull
  @Override
  public Map<String, String> getOperations(@NonNull Content content, @Nullable String property) {
    Optional<Struct> localSettings = getLocalSettings(content);
    if (!localSettings.isPresent()) {
      return new HashMap<>();
    }

    Struct settings = localSettings.get();
    if (settings.get(TRANSFORMS) == null || !(settings.get(TRANSFORMS) instanceof Struct)) {
      return new HashMap<>();
    }
    Struct transformStruct = settings.getStruct(TRANSFORMS);

    //transform from <String, Object> to <String, String> and make a copy of the map.
    return transformStruct.getProperties()
                          .entrySet()
                          .stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, entry -> valueOf(entry.getValue())));
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

  @Override
  public boolean isApplicable(@NonNull Content content, @Nullable String property) {
    return content.getType()
                  .isSubtypeOf("CMPicture");
  }
}
