package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMDownload;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMDownload.
 * Should not be changed.
 */
public abstract class CMDownloadBase extends CMTeasableImpl implements CMDownload {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMDownload} objects
   */
  @Override
  public CMDownload getMaster() {
    return (CMDownload) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMDownload> getVariantsByLocale() {
    return getVariantsByLocale(CMDownload.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMDownload> getLocalizations() {
    return (Collection<? extends CMDownload>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMDownload>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMDownload>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMDownload>> getAspects() {
    return (List<? extends Aspect<? extends CMDownload>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #DATA}.
   *
   * @return the value of the document property {@link #DATA}
   */
  @Override
  public Blob getData() {
    return getContent().getBlobRef(CMDownload.DATA);
  }

  public String getFilename() {
    return getContent().getString(CMDownload.FILENAME);
  }

}
