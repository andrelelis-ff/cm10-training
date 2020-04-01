package com.coremedia.blueprint.common.util;

import com.coremedia.cap.common.XmlGrammar;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RichtextHelperTest {
  private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

  private static final Markup REALLY_EMPTY_RICHTEXT = MarkupFactory.fromString(XML_HEADER + "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\"/>").withGrammar(XmlGrammar.RICH_TEXT_1_0_NAME);
  private static final Markup DIRTY_EMPTY_RICHTEXT = MarkupFactory.fromString(XML_HEADER + "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\"><p/></div>").withGrammar(XmlGrammar.RICH_TEXT_1_0_NAME);
  private static final Markup NON_EMPTY_RICHTEXT = MarkupFactory.fromString(XML_HEADER + "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\"><p>foo</p></div>").withGrammar(XmlGrammar.RICH_TEXT_1_0_NAME);
  private static final Markup POOR_RICHTEXT = MarkupFactory.fromString("<div><p>foo</p></div>");


  // --- tests ------------------------------------------------------

  @Test
  void testResultConstantIsReallyEmpty() {
    assertMarkupEquals(REALLY_EMPTY_RICHTEXT, RichtextHelper.EMPTY_RICHTEXT);
  }

  @Test
  void testReallyEmptyRichtext() {
    assertMarkupEquals(RichtextHelper.EMPTY_RICHTEXT, RichtextHelper.htmlOptimizedRichtext(REALLY_EMPTY_RICHTEXT));
  }

  @Test
  void testDirtyEmptyRichtext() {
    assertMarkupEquals(RichtextHelper.EMPTY_RICHTEXT, RichtextHelper.htmlOptimizedRichtext(DIRTY_EMPTY_RICHTEXT));
  }

  @Test
  void testNonEmptyRichtext() {
    assertMarkupEquals(NON_EMPTY_RICHTEXT, RichtextHelper.htmlOptimizedRichtext(NON_EMPTY_RICHTEXT));
  }

  @Test
  void testPoorRichtext() {
    assertMarkupEquals(POOR_RICHTEXT, RichtextHelper.htmlOptimizedRichtext(POOR_RICHTEXT));
  }



  // --- internal ---------------------------------------------------

  private void assertMarkupEquals(Markup expected, Markup actual) {
    // MarkupImpl#equals is only String based and thus too strict wrt.
    // XML semantics (e.g. order of attributes).  If this check ever fails,
    // change it to a more appropriate comparison.
    assertEquals(expected, actual);
  }
}
