package com.coremedia.blueprint.elastic.social.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BbCodeToCoreMediaRichtextTransformerTest {
  private BbCodeToCoreMediaRichtextTransformer bbCodeToCoreMediaRichtextTransformer = BbCodeToCoreMediaRichtextTransformer.newInstance();

  private static final String RICHTEXT_START_TAG = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
  private static final String RICHTEXT_END_TAG = "</div>";

  // --- Helper methods -------------------------------------------------------------------

  // wraps the "plain content" to convert in minimal XML to
  // make it "CoreMedia Richtext 1.0"-compliant
  private static String richtextFrom(final String text) {
    return RICHTEXT_START_TAG
            + "<p>"
            + text
            + "</p>"
            + RICHTEXT_END_TAG;
  }

  private String transformAsString(String bbCode) {
    return bbCodeToCoreMediaRichtextTransformer.transform(bbCode).toString();
  }


  // --- Tests-----------------------------------------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void checkNullArgument() {
    bbCodeToCoreMediaRichtextTransformer.transform(null);
  }

  @Test
  public void convertEmptyString() {
    assertEquals("<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><p/></div>", transformAsString(""));
  }

  @Test
  public void convertSingleSpace() {
    final String bbCode = " ";
    assertEquals(richtextFrom(" "), transformAsString(bbCode));
  }

  @Test
  public void convertHtmlEntities() {
    assertEquals(richtextFrom("&amp;"), transformAsString("&"));
    assertEquals(richtextFrom("&lt;"), transformAsString("<"));
    assertEquals(richtextFrom("&gt;"), transformAsString(">"));
  }

  @Test
  public void convertLineBreaks() {
    assertEquals(richtextFrom("foo<br/>bar"), transformAsString("foo\r\nbar"));
    assertEquals(richtextFrom("foo<br/>bar"), transformAsString("foo\rbar"));
    assertEquals(richtextFrom("foo<br/>bar"), transformAsString("foo\nbar"));
  }

  @Test
  public void convertHtmlTags() {
    assertEquals(richtextFrom("<a xlink:href=\"www.coremedia.com\" xlink:show=\"replace\">www.coremedia.com</a>"),
            transformAsString("[url]www.coremedia.com[/url]"));
    assertEquals(richtextFrom("<a xlink:href=\"www.coremedia.com\" xlink:show=\"replace\">CoreMedia</a>"),
            transformAsString("[url=www.coremedia.com]CoreMedia[/url]"));
    // Bold font
    assertEquals(richtextFrom("<strong>foo</strong>"),
            transformAsString("[b]foo[/b]"));

    // Italic font
    assertEquals(richtextFrom("<em>foo</em>"),
            transformAsString("[i]foo[/i]"));

    // Quotes
    assertEquals(richtextFrom("<em>fooBar</em>"),
            transformAsString("[quote]fooBar[/quote]"));
    assertEquals(richtextFrom("fooBar"),
            transformAsString("[quote author='Horst' date='moep']fooBar[/quote]"));
  }
}
