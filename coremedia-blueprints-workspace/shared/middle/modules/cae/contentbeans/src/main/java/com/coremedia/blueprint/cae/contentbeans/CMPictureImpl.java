package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.cta.CallToActionButtonSettings;
import com.coremedia.blueprint.common.teaser.TeaserSettings;
import com.coremedia.cap.transform.Transformation;
import com.coremedia.cap.transform.TransformImageService;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Generated extension class for immutable beans of document type "CMPicture".
 */
public class CMPictureImpl extends CMPictureBase {
  private TransformImageService transformImageService;

  /*
   * Add additional methods here.
   * Add them to the interface {@link com.coremedia.blueprint.common.contentbeans.CMPicture} to make them public.
   */
  @Required
  public void setTransformImageService(TransformImageService transformImageService) {
    this.transformImageService = transformImageService;
  }

  /**
   * Override the method to handle images, which does not have a transformation already.
   * @return a map of transformations, merged from image settings and {@link TransformImageService} service
   */
  @Override
  public Map<String,String> getTransformMap() {
    return transformImageService.getTransformationOperations(this.getContent(), DATA);
  }

  @Override
  public List<Transformation> getTransformations() {
    return transformImageService.getTransformations(getContent());
  }

  @Override
  public Transformation getTransformation(String name) {
    return transformImageService.getTransformation(this.getContent(), name);
  }

  @Override
  public List<CallToActionButtonSettings> getCallToActionSettings() {
    return Collections.emptyList();
  }


  @Override
  public TeaserSettings getTeaserSettings() {
    Map<String, Object> mapping = getTeaserSettingsMap();
    //noinspection Convert2Lambda
    return new TeaserSettings() {
      @Override
      public boolean isRenderLinkToDetailPage() {
        return (boolean) mapping.getOrDefault("renderLinkToDetailPage", false);
      }
    };
  }
}
