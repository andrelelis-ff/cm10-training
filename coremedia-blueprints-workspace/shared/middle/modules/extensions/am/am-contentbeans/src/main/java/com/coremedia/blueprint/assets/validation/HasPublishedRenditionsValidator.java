package com.coremedia.blueprint.assets.validation;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.common.services.validation.AbstractValidator;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Predicate;

/**
 * Checks if an {@link AMAsset} is valid.
 * For a preview, it must have any renditions, for the live site, it must have only published renditions.
 */
public class HasPublishedRenditionsValidator extends AbstractValidator<AMAsset> {

  private boolean preview;

  @Value("${cae.is.preview:false}")
  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  @Override
  protected Predicate<AMAsset> createPredicate() {
    return asset -> asset != null
            && (preview ? !asset.getRenditions().isEmpty() : !asset.getPublishedRenditions().isEmpty());
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return AMAsset.class.isAssignableFrom(clazz);
  }
}
