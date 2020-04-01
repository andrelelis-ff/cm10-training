package com.coremedia.blueprint.elastic.social.cae.controller;

import java.io.Serializable;

public class BlobRefImpl implements BlobRef, Serializable {

  private static final long serialVersionUID = 1128810549795121538L;
  private final String id;


  public BlobRefImpl(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
