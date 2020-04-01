package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;
import com.coremedia.xml.Markup;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * CMTheme beans provide access to combined {@link com.coremedia.blueprint.common.contentbeans.CMCSS},
 * {@link com.coremedia.blueprint.common.contentbeans.CMJavaScript} documents usable to be attached to a channel.
 * </p>
 * <p>
 * Represents document type {@link #NAME CMTheme}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMTheme extends CMLocalized {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMCSS'.
   */
  String NAME = "CMTheme";

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMTheme>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMTheme>> getAspects();

  /**
   * Name of the document property 'description'.
   */
  String DESCRIPTION = "description";

  /**
   * Returns the value of the document property {@link #DESCRIPTION}.
   *
   * @return the value of the document property {@link #DESCRIPTION}
   */
  String getDescription();

  /**
   * Name of the document property 'icon'.
   */
  String ICON = "icon";

  /**
   * Returns the value of the {@link CMSymbol} document property {@link CMSymbol#ICON}.
   *
   * @return the value of the {@link CMSymbol} document property {@link CMSymbol#ICON} or null
   * @cm.template.api
   */
  Blob getIcon();

  /**
   * Name of the document property 'javaScriptLibs'.
   */
  String JAVA_SCRIPT_LIBS = "javaScriptLibs";

  /**
   * Name of the document property 'javaScripts'.
   */
  String JAVA_SCRIPTS = "javaScripts";

  /**
   * Name of the document property 'css'.
   */
  String CSS = "css";

  /**
   * Name of the document property 'viewRepositoryName'.
   */
  String VIEW_REPOSITORY_NAME = "viewRepositoryName";

  /**
   * Name of the document property 'detailText'.
   */
  String DETAIL_TEXT = "detailText";

  /**
   * Name of the document property 'resourceBundles'.
   */
  String RESOURCE_BUNDLES = "resourceBundles";

  /**
   * Name of the document property 'templateSets'.
   */
  String TEMPLATE_SETS = "templateSets";

  /**
   * Returns the value of the document property {@link #DETAIL_TEXT}.
   *
   * @return the value of the document property {@link #DETAIL_TEXT}
   * @cm.template.api
   */
  Markup getDetailText();

  /**
   * Returns the value of the document property {@link #JAVA_SCRIPT_LIBS}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMJavaScript} objects
   * @cm.template.api
   */
  @NonNull
  List<CMJavaScript> getJavaScriptLibraries();

  /**
   * Returns the value of the document property {@link #JAVA_SCRIPTS}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMJavaScript} objects
   * @cm.template.api
   */
  @NonNull
  List<CMJavaScript> getJavaScripts();

  /**
   * Returns the value of the document property {@link #CSS}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMCSS} objects
   * @cm.template.api
   */
  @NonNull
  List<CMCSS> getCss();

  /**
   * Returns the value of the document property {@link #RESOURCE_BUNDLES}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMResourceBundle} objects
   */
  @NonNull
  List<CMResourceBundle> getResourceBundles();

  String getViewRepositoryName();
}
