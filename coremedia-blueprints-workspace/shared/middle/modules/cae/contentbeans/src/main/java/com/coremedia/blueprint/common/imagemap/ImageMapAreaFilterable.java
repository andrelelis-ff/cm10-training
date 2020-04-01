package com.coremedia.blueprint.common.imagemap;

import com.coremedia.blueprint.common.contentbeans.CMImageMap;

import java.util.List;
import java.util.Map;

public interface ImageMapAreaFilterable {

  List<Map<String, Object>> filter(List<Map<String, Object>> areas, CMImageMap imageMap);
}
