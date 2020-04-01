package com.coremedia.blueprint.editor.init;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link SegmentInitializer}.
 */
public class SegmentInitializerTest {
  private SegmentInitializer converter = new SegmentInitializer();

  @Test
  public void testConvertUmlautCharacters() {
    Assert.assertEquals("taetaeraetae", converter.convertSegment("t\u00E4t\u00E4r\u00E4t\u00E4"));
    Assert.assertEquals("tAetAerAetAe", converter.convertSegment("t\u00C4t\u00C4r\u00C4t\u00C4"));
    Assert.assertEquals("oeffentlich", converter.convertSegment("\u00F6ffentlich"));
    Assert.assertEquals("Oeffentlich", converter.convertSegment("\u00D6ffentlich"));
    Assert.assertEquals("uebel", converter.convertSegment("\u00FCbel"));
    Assert.assertEquals("Uebel", converter.convertSegment("\u00DCbel"));
    Assert.assertEquals("fussball", converter.convertSegment("fu\u00dfball"));
  }

  @Test
  public void testConvertWhitespaceCharacters() {
    Assert.assertEquals("public_area", converter.convertSegment("public area"));
    Assert.assertEquals("_space_", converter.convertSegment(" space "));
    Assert.assertEquals("_", converter.convertSegment("_"));
  }

  @Test
  public void testPrependUnderscoreToDigitsOnly() {
    Assert.assertEquals("-123", converter.convertSegment("123"));
    Assert.assertEquals("-123", converter.convertSegment("-123"));
    Assert.assertEquals("-123", converter.convertSegment("-123"));
    Assert.assertEquals("_123", converter.convertSegment("_123"));
  }
}
