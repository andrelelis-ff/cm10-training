package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.transform.Transformation;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * A picture with the actual image blob and some metadata.
 * </p>
 * <p>
 * Note that a Picture is a first class content object. If you are sure
 * that your picture is used only in an article or in a photoshow, just
 * ignore the teaser properties. We could have introduced an <i>Asset</i> type
 * for such purposes, but we wanted to keep the doctypes simple.
 * </p>
 * <p>
 * If you need distinct blobs of a picture, e.g. optimized for online
 * or print usage, you should derive a new doctype from CMPicture and
 * add more BlobProperties or use an DocTypeAspect to attach further properties.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMPicture}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMPicture extends CMVisual {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMPicture'.
   */
  String NAME = "CMPicture";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMPicture} object
   */
  @Override
  CMPicture getMaster();

  @Override
  Map<Locale, ? extends CMPicture> getVariantsByLocale();

  @Override
  Collection<? extends CMPicture> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMPicture>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMPicture>> getAspects();

  /**
   * Returns the value of the document property (@link #data}
   *
   * @return the value of the document property (@link #data}
   */
  @Override
  Blob getData();

  /**
   * Returns the available transformation definitions for this image.
   * Image variants may differ if the site specific image variants are enabled.
   *
   * @cm.template.api
   */
  List<Transformation> getTransformations();

  /**
   * Returns the transformation for the give name.
   */
  Transformation getTransformation(String name);

  /**
   * Returns the focus point.
   * The position is a value between 0 and 1, relative to the actual dimensions of the picture.
   *
   * @return the focus point
   */
  Point2D getFocusPoint();
}
