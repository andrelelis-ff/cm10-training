package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;
import com.coremedia.xml.Markup;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * The abstract CMMedia type starts the media hierarchy defining data, caption and an alt property.
 * </p>
 * <p>
 * All subtypes override and specialize the BlobProperty defining the MIME-type. To allow not only Blobs as data
 * CMMedia defines the data return type to be Object and leave it to the developer to concretise the type by covariant
 * overriding the return type.
 * </p>
 * <p>
 * Additionally needed properties can be attached on server-side via a DocTypeAspect and represented by aspect content beans
 * on client-side.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMMedia}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMMedia extends CMTeasable {

  String NAME = "CMMedia";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMMedia} object
   */
  @Override
  CMMedia getMaster();

  @Override
  Map<Locale, ? extends CMMedia> getVariantsByLocale();

  @Override
  Collection<? extends CMMedia> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMMedia>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMMedia>> getAspects();

  /**
   * Name of the document property 'data'.
   */
  String DATA = "data";
  /**
   * Name of the document property 'caption'.
   */
  String CAPTION = "caption";
  /**
   * Name of the document property 'alt'.
   */
  String ALT = "alt";

  /**
   * Name of the document property 'copyright'.
   */
  String COPYRIGHT = "copyright";

  /**
   * Returns the value of the document property {@link #COPYRIGHT}.
   *
   * @return the value of the document property {@link #COPYRIGHT}
   * @cm.template.api
   */
  String getCopyright();

  /**
   * Returns the value of the document property (@link #data}
   *
   * @return the value of the document property (@link #data}
   * @cm.template.api
   */
  Object getData();

  /**
   * Returns the value of the document property {@link #CAPTION}.
   *
   * @return the value of the document property {@link #CAPTION}
   * @cm.template.api
   */
  Markup getCaption();

  /**
   * Returns the value of the document property {@link #ALT}.
   *
   * @return the value of the document property {@link #ALT}
   * @cm.template.api
   */
  String getAlt();

  /**
   * Returns the transformed data blob for the given transformName
   *
   * @param transformName
   * @return the transformed data blob for the given transformName
   */
  Blob getTransformedData(String transformName);

  /**
   * provides the transformMap for this media
   *
   * @return the transformData if exists, an empty Map when no transform
   * data exists.
   */
  Map<String, String> getTransformMap();

  /**
   * In Studio an editor can disable cropping for specific media in general.
   * This is the getter to check this.
   *
   * @return true, if the media should be general uncropped.
   * @cm.template.api
   */
  boolean getDisableCropping();
}
