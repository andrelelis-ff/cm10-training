package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.elastic.core.api.blobs.Blob;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An adapter to access {@link com.coremedia.elastic.core.api.blobs.Blob}s with the {@link com.coremedia.cap.common.Blob}
 * interface.
 */
public class BlobAdapter implements com.coremedia.cap.common.Blob {

  private final Blob delegate;

  public BlobAdapter(Blob delegate) {
    this.delegate = delegate;
  }

  @Override
  public MimeType getContentType() {
    try {
      return new MimeType(delegate.getContentType());
    } catch (MimeTypeParseException e) {
      throw new IllegalArgumentException("delegate blob does not provide valid content type", e);
    }
  }

  @Override
  public int getSize() {
    return (int) delegate.getLength();
  }

  @Override
  public String getETag() {
    return null;
  }

  @Override
  public void writeOn(OutputStream outputStream) throws IOException {
    delegate.writeOn(outputStream);
  }

  /**
   * Since @link{com.coremedia.elastic.core.api.blobs.Blob} does not support streaming, this
   * method returns a ByteArrayInputStream backed by #asBytes.
   *
   * @return a ByteArrayInputStream
   * @see #asBytes()
   */
  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(asBytes());
  }

  @Override
  public byte[] asBytes() {
    try {
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(getSize());
      writeOn(outputStream);
      // no need to close stream
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new IllegalArgumentException("cannot read blob", e);
    }
  }

  @Override
  public void dispose() {
    // Elastic blobs are not disposed.
  }
}
