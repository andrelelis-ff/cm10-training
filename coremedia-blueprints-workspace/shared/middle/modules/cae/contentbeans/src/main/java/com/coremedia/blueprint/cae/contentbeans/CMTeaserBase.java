package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMTeaser;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.cap.common.CapStructHelper.getBoolean_;
import static com.coremedia.cap.common.CapStructHelper.getString;
import static com.coremedia.cap.common.CapStructHelper.isEmpty;

/**
 * Generated base class for immutable beans of document type CMTeaser.
 * Should not be changed.
 */
@SuppressWarnings("FieldCanBeLocal")
public abstract class CMTeaserBase extends CMTeasableImpl implements CMTeaser {

  static final String ANNOTATED_LINK_STRUCT_CTA_ENABLED_PROPERTY_NAME = "callToActionEnabled";
  static final String ANNOTATED_LINK_STRUCT_CTA_CUSTOM_TEXT_PROPERTY_NAME = "callToActionCustomText";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMTeaser} objects
   */
  @Override
  public CMTeaser getMaster() {
    return (CMTeaser) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMTeaser> getVariantsByLocale() {
    return getVariantsByLocale(CMTeaser.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMTeaser> getLocalizations() {
    return (Collection<? extends CMTeaser>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMTeaser>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMTeaser>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMTeaser>> getAspects() {
    return (List<? extends Aspect<? extends CMTeaser>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #TARGET},
   * but only if {@link #getValidationService()#validate} succeeds.
   *
   * @return the valid {@link CMLinkable} object
   */
  @Override
  public CMLinkable getTarget() {
    return getLegacyAnnotatedLink(getTargetsUnfiltered(), getTargetUnfiltered());
  }

  @Override
  public Map<String, List<Map<String, Object>>> getTargets() {
    return getAnnotatedLinkList(getTargetsUnfiltered(), getTargetUnfiltered(), TARGET);
  }

  @Override
  @NonNull
  protected List<Map<String, Object>> convertLinkListToAnnotatedLinkList(@NonNull List<CMLinkable> linkList, @Nullable String linkListPropertyName) {
    List<Map<String, Object>> annotatedLinkList = super.convertLinkListToAnnotatedLinkList(linkList, linkListPropertyName);
    if (CMTeaser.TARGET.equals(linkListPropertyName) && !annotatedLinkList.isEmpty()) {
      Map<String, Object> targetStructMap = annotatedLinkList.get(0);
      boolean ctaEnabled = false;
      String ctaCustomText = null;
      Struct localSettings = getLocalAndLinkedSettings();
      if (!isEmpty(localSettings)) {
        Boolean ctaDisabled = getBoolean_(localSettings, LEGACY_STRUCT_CTA_DISABLED_PROPERTY_NAME);
        ctaEnabled = ctaDisabled != null && !ctaDisabled;
        ctaCustomText = getString(localSettings, LEGACY_STRUCT_CTA_CUSTOM_TEXT_PROPERTY_NAME);
      }
      if (ctaEnabled) {
        targetStructMap.put(ANNOTATED_LINK_STRUCT_CTA_ENABLED_PROPERTY_NAME, true);
      }
      if (ctaCustomText != null && !ctaCustomText.isEmpty()) {
        targetStructMap.put(ANNOTATED_LINK_STRUCT_CTA_CUSTOM_TEXT_PROPERTY_NAME, ctaCustomText);
      }
    }
    return annotatedLinkList;
  }

  @NonNull
  public Map<String, List<Map<String, Object>>> getTargetsUnfiltered() {
    return super.getAnnotatedLinkListUnfiltered(TARGETS);
  }

  @NonNull
  public List<CMLinkable> getTargetUnfiltered() {
    return super.getLegacyLinkListUnfiltered(TARGET);
  }
}
