package com.coremedia.livecontext.validation;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.contentbeans.CMLinkableImpl;
import com.coremedia.blueprint.common.contentbeans.CMTeaser;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.services.validation.AbstractValidator;
import com.coremedia.cap.struct.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * validates {@link CMTeaser} objects by validating the {@link com.coremedia.blueprint.common.contentbeans.CMTeaser#getTarget()}
 * candidate if the {@link #INHERIT_VALIDITY_SETTING_NAME} setting of the {@link CMTeaser} is <code>true</code>
 * by using the injected {@link com.coremedia.blueprint.common.services.validation.ValidationService}.
 * If the passed in object is a {@link Page}, its {@link com.coremedia.blueprint.common.contentbeans.Page#getContent()}
 * is tested instead.
 */
public class InvalidTeaserTargetValidator extends AbstractValidator<Object> {

  private static final Logger LOG = LoggerFactory.getLogger(InvalidTeaserTargetPredicate.class);

  private static final String INHERIT_VALIDITY_SETTING_NAME = "useTeaserTargetValidity";

  private SettingsService settingsService;

  @Override
  protected Predicate<Object> createPredicate() {
    return new InvalidTeaserTargetPredicate();
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return CMTeaser.class.isAssignableFrom(clazz) || Page.class.isAssignableFrom(clazz);
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  private class InvalidTeaserTargetPredicate implements Predicate<Object> {
    @Override
    public boolean test(@Nullable Object candidate) {
      if (candidate instanceof Page) {
        candidate = ((Page) candidate).getContent();
      }

      if (!(candidate instanceof CMTeaser)) {
        return true;
      }

      CMTeaser teaser = (CMTeaser) candidate;
      Boolean inheritValidity = settingsService.getSetting(INHERIT_VALIDITY_SETTING_NAME, Boolean.class, teaser)
              .orElse(false);
      if (!inheritValidity) {
        return true;
      }

      LOG.debug("{} settings is enabled, checking validity of teaser target...", INHERIT_VALIDITY_SETTING_NAME);

      // Get the raw targets from the content property
      Struct targetsStruct = teaser.getContent().getStruct(CMTeaser.TARGETS);
      List<Struct> linkStructs = targetsStruct.getStructs(CMLinkableImpl.ANNOTATED_LINKS_STRUCT_ROOT_PROPERTY_NAME);

      if (linkStructs.isEmpty()) {
        return true;
      }
      // Get the validated targets
      Map<String, List<Map<String, Object>>> targetsObj = teaser.getTargets();
      List<Map<String, Object>> linkObjs = targetsObj.get(CMLinkableImpl.ANNOTATED_LINKS_STRUCT_ROOT_PROPERTY_NAME);

      if (!linkObjs.isEmpty()) {
        return true;
      }
      // The getTargets() method returned no links while there are links according to the content's raw 'targets' property.
      // This happens when the validation service filters out all targets.
      // Validity inheritance shall be checked for this teaser, therefore return false!
      LOG.debug("all teaser targets are invalid, therefore Teaser {} is considered invalid", teaser);
      return false;
    }
  }
}
