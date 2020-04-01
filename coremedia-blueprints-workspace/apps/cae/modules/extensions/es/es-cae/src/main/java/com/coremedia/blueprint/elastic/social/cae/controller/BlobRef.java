package com.coremedia.blueprint.elastic.social.cae.controller;

/**
 * Serializable reference to a {@link com.coremedia.elastic.core.api.blobs.Blob} which can be used for Spring Webflows.
 * This class is used to generate links for a {@link com.coremedia.elastic.core.api.blobs.Blob}.
 *
 * @cm.template.api
 */
public interface BlobRef {

  String getId();
}
