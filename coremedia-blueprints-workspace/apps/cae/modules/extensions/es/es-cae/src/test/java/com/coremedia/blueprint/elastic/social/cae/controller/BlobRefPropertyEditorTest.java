package com.coremedia.blueprint.elastic.social.cae.controller;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class BlobRefPropertyEditorTest {

  @Test
  public void test() {
    BlobRefImpl blobRef = new BlobRefImpl("123");
    BlobRefPropertyEditor editor = new BlobRefPropertyEditor();
    editor.setAsText("123");

    Object value = editor.getValue();

    assertTrue(value instanceof BlobRef);
    assertEquals(blobRef.getId(), ((BlobRef)value).getId());
  }
}
