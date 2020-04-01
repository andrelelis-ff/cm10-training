package com.coremedia.blueprint.elastic.social.cae.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlobRefImplTest {

  @Test
  public void test() {
    String id = "id";
    BlobRefImpl ref = new BlobRefImpl(id);

    assertEquals(id, ref.getId());
  }
}
