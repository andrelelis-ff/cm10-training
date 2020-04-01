package com.coremedia.blueprint.sfmc.studio.lib;

import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.blob.images.ContentTransformationOperationsResolver;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CMPictureTransformationOperationsResolver implements ContentTransformationOperationsResolver {

  @Override
  @NonNull
  public Map<String, String> resolveOperations(@NonNull Content content, @NonNull String propertyName) {
    Struct localSettings = content.getStruct("localSettings");
    if (localSettings == null || localSettings.getType().getDescriptor("transforms") == null) {
      return new HashMap<>();
    }
    Map<String, Object> structMap = localSettings.getStruct("transforms").getProperties();
    return structMap.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
  }

  @Override
  public boolean accepts(@NonNull Content content, @NonNull String propertyName) {
    return content.getType().isSubtypeOf("CMPicture");
  }
}
