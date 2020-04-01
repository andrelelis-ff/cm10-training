package com.coremedia.blueprint.common.imagemap;

import com.coremedia.blueprint.base.cae.web.taglib.ImageFunctions;
import com.coremedia.blueprint.common.contentbeans.CMImageMap;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Map;

/**
 * The ValidityImageMapAreaFilter class checks, if linked contents are valid.
 */
public class ValidityImageMapAreaFilter implements ImageMapAreaFilterable {

  private ValidationService validationService;

  @Override
  public List<Map<String, Object>> filter(List<Map<String, Object>> areas, CMImageMap imageMap) {

    Iterable<Map<String, Object>> filteredAreas = Iterables.filter(areas, new Predicate<Map<String, Object>>() {
      @Override
      public boolean apply(@Nullable Map<String, Object> map) {
        if (map == null) {
          return false;
        }
        Object linkedContent = map.get(ImageFunctions.LINKED_CONTENT);
        if (linkedContent == null || !validationService.validate(linkedContent)) {
          return false;
        } else if (linkedContent instanceof ContentBean) {
          ContentBean cb = (ContentBean) linkedContent;
          Content c = cb.getContent();
          return c != null && c.isInProduction();
        }
        // should not happen
        return true;
      }
    });
    return Lists.newArrayList(filteredAreas);
  }

  @Required
  public void setValidationService(ValidationService validationService) {
    this.validationService = validationService;
  }
}
