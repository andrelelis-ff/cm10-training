package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMPerson;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;
import com.coremedia.common.personaldata.PersonalData;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Generated base class for immutable beans of document type CMPerson.
 * Should not be changed.
 */
public abstract class CMPersonBase extends CMTeasableImpl implements CMPerson {
  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMTeasable} objects
   */
  @Override
  public CMPerson getMaster() {
    return (CMPerson) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMPerson> getVariantsByLocale() {
    return getVariantsByLocale(CMPerson.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMPerson> getLocalizations() {
    return (Collection<? extends CMPerson>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMPerson>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMPerson>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMPerson>> getAspects() {
    return (List<? extends Aspect<? extends CMPerson>>) super.getAspects();
  }

  @Override
  public @PersonalData String getFirstName() {
    return getContent().getString(FIRST_NAME);
  }

  @Override
  public @PersonalData String getLastName() {
    return getContent().getString(LAST_NAME);
  }

  @Override
  public @PersonalData String getDisplayName() {
    return getContent().getString(DISPLAY_NAME);
  }

  @Override
  public  @PersonalData String getEMail() {
    return getContent().getString(EMAIL);
  }

  @Override
  public @PersonalData String getOrganization() {
    return getContent().getString(ORGANIZATION);
  }

  @Override
  public @PersonalData String getJobTitle() {
    return getContent().getString(JOB_TITLE);
  }

  @Override
  public @PersonalData Struct getMisc() {
    return getContent().getStruct(MISC);
  }
}
