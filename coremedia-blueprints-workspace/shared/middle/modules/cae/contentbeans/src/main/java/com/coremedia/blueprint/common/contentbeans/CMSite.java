package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Helper document: All root navigations must be referred to by a CMSite.
 * This is an internal optimization to find the root navigations quickly.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMSite}.
 * </p>
 */
public interface CMSite extends CMLocalized {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMSite'.
   */
  String NAME = "CMSite";

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMSite>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMSite>> getAspects();

  /**
   * Name of the document property 'root'.
   */
  String ROOT = "root";

  /**
   * Returns the value of the document property {@link #ROOT}.
   *
   * @return the value of the document property {@link #ROOT}
   */
  CMNavigation getRoot();

  /**
   * Name of the document property 'id'.
   */
  String ID = "id";

  /**
   * Returns the value of the document property {@link #ID}.
   *
   * @return the value of the document property {@link #ID}
   */
  String getId();

  /**
   * Name of the document property 'master'.
   */
  String MASTER = "master";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMSite} object or null if no master site exists
   */
  @Override
  CMSite getMaster();
}
