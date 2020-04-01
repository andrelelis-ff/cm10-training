package com.coremedia.livecontext.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface CMExternalPage extends CMChannel {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMExternalPage'.
   */
  String NAME = "CMExternalPage";

  /**
   * Name of the document property 'externalId'.
   *
   * <p>Useful for queries and content level code.
   */
  String EXTERNAL_ID = "externalId";

  /**
   * Name of the localSettings struct property 'externalUriPath'.
   *
   * <p>Useful to build link to the external system
   */
  String EXTERNAL_URI_PATH = "externalUriPath";

  @Override
  CMExternalPage getMaster();

  @Override
  Map<Locale, ? extends CMExternalPage> getVariantsByLocale();

  @Override
  Collection<? extends CMExternalPage> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMExternalPage>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMExternalPage>> getAspects();

  String getExternalId();

  String getExternalUriPath();
}
