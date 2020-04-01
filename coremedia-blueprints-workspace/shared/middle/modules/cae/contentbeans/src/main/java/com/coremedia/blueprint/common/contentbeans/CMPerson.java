package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;
import com.coremedia.common.personaldata.PersonalData;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents the document type {@link #NAME CMPerson}.
 *
 * @cm.template.api
 */
public interface CMPerson extends CMTeasable {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMPerson'.
   */
  String NAME = "CMPerson";

  // Constants for property names
  String FIRST_NAME = "firstName";
  String LAST_NAME = "lastName";
  String DISPLAY_NAME = "displayName";
  String EMAIL = "eMail";
  String ORGANIZATION = "organization";
  String JOB_TITLE = "jobTitle";
  String MISC = "misc";


  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMPerson} object
   */
  @Override
  CMPerson getMaster();

  @Override
  Map<Locale, ? extends CMPerson> getVariantsByLocale();

  @Override
  Collection<? extends CMPerson> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMPerson>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMPerson>> getAspects();

  /**
   * <p>
   * Returns the value of the document property {@link #HTML_TITLE}.
   * If property {@link #HTML_TITLE} is empty,  property {@link #DISPLAY_NAME} will be used as fallback.
   * </p>
   *
   * @return the value of the document property {@link #HTML_TITLE} or property {@link #DISPLAY_NAME} as fallback
   * @cm.template.api
   */
  @Override
  String getHtmlTitle();

  /**
   * <p>
   * Returns the value of the document property {@link #TEASER_TITLE}.
   * If property {@link #TEASER_TITLE} is empty,  property {@link #DISPLAY_NAME} will be used as fallback.
   * </p>
   *
   * @return the value of the document property {@link #TEASER_TITLE} or property {@link #DISPLAY_NAME} as fallback
   * @cm.template.api
   */
  @Override
  String getTeaserTitle();

  /**
   * <p>
   * Returns the value of the document property {@link #FIRST_NAME}.
   * </p>
   *
   * @return the value of the document property {@link #FIRST_NAME}
   * @cm.template.api
   */
  @PersonalData String getFirstName();

  /**
   * <p>
   * Returns the value of the document property {@link #LAST_NAME}.
   * </p>
   *
   * @return the value of the document property {@link #LAST_NAME}
   * @cm.template.api
   */
  @PersonalData String getLastName();

  /**
   * <p>
   * Returns the value of the document property {@link #DISPLAY_NAME}.
   * </p>
   *
   * @return the value of the document property {@link #DISPLAY_NAME}
   * @cm.template.api
   */
  @PersonalData String getDisplayName();

  /**
   * <p>
   * Returns the value of the document property {@link #EMAIL}.
   * </p>
   *
   * @return the value of the document property {@link #EMAIL}
   * @cm.template.api
   */
  @PersonalData String getEMail();

  /**
   * <p>
   * Returns the value of the document property {@link #ORGANIZATION}.
   * </p>
   *
   * @return the value of the document property {@link #ORGANIZATION}
   * @cm.template.api
   */
  @PersonalData String getOrganization();

  /**
   * <p>
   * Returns the value of the document property {@link #JOB_TITLE}.
   * </p>
   *
   * @return the value of the document property {@link #JOB_TITLE}
   * @cm.template.api
   */
  @PersonalData String getJobTitle();

  /**
   * <p>
   * Returns the value of the document property {@link #MISC}.
   * </p>
   *
   * @return the value of the document property {@link #MISC}
   */
  @PersonalData Struct getMisc();

  /**
   * <p>
   * Returns the value of the document property {@link #MISC} as nested map.
   * </p>
   *
   * @return the value of the document property {@link #MISC} as map
   * @cm.template.api
   */
  @PersonalData Map<String, Object> getFurtherDetails();
}
