package com.coremedia.blueprint.themeimporter;

import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.mimetype.MimeTypeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.regex.Matcher;

import static com.coremedia.blueprint.themeimporter.ThemeImporterImpl.CG_HOSTPORT;
import static com.coremedia.blueprint.themeimporter.ThemeImporterImpl.CG_PATH;
import static com.coremedia.blueprint.themeimporter.ThemeImporterImpl.CG_PROTOCOL;
import static com.coremedia.blueprint.themeimporter.ThemeImporterImpl.CG_SUFFIX;
import static com.coremedia.blueprint.themeimporter.ThemeImporterImpl.CG_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThemeImporterImplUnitTest {

  @Mock
  CapConnection capConnection;
  @Mock
  MimeTypeService mimeTypeService;
  @Mock
  LocalizationService localizationService;
  @Mock
  private SettingsJsonToMapAdapter settingsJsonToMapAdapter;
  @Mock
  private MapToStructAdapter mapToStructAdapter;
  @Mock
  ThemeImporterContentHelper contentHelper;


  // --- Tests ------------------------------------------------------

  @Test
  public void testNameOnly() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("background: url(bigplay.svg) no-repeat;");
    checkNameOnly(matcher);
    assertFalse(matcher.find());
  }

  @Test
  public void testQuotes() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("background: url('bigplay.svg') no-repeat;");
    checkNameOnly(matcher);
    assertFalse(matcher.find());
  }

  @Test
  public void testDoubleQuotes() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("background: url(\"bigplay.svg\") no-repeat;");
    checkNameOnly(matcher);
    assertFalse(matcher.find());
  }

  @Test
  public void testNewlines() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("foo\nbackground: url(bigplay.svg) no-repeat;\nbar");
    checkNameOnly(matcher);
    assertFalse(matcher.find());
  }

  @Test
  public void testMultipleMatches() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("background: url(bigplay.svg) no-repeat;\nforeground: url('bigplay.svg') no-repeat;\nunderground: url(\"bigplay.svg\") no-repeat;");
    checkNameOnly(matcher);
    checkNameOnly(matcher);
    checkNameOnly(matcher);
    assertFalse(matcher.find());
  }

  @Test
  public void testRelativeUrl() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("background-image: url(../img/button_video_play.png);");
    assertTrue(matcher.find());
    assertEquals("../img/button_video_play.png", matcher.group(CG_URL));
    assertNull(matcher.group(CG_PROTOCOL));
    assertNull(matcher.group(CG_HOSTPORT));
    assertEquals("../img/button_video_play.png", matcher.group(CG_PATH));
    assertTrue(matcher.group(CG_SUFFIX).isEmpty());
    assertFalse(matcher.find());
  }

  @Test
  public void testQuerySuffix() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("src: url(\"../fonts/bootstrap/glyphicons-halflings-regular.eot?#iefix\") format(\"embedded-opentype\"),");
    assertTrue(matcher.find());
    assertEquals("../fonts/bootstrap/glyphicons-halflings-regular.eot?#iefix", matcher.group(CG_URL));
    assertNull(matcher.group(CG_PROTOCOL));
    assertNull(matcher.group(CG_HOSTPORT));
    assertEquals("../fonts/bootstrap/glyphicons-halflings-regular.eot", matcher.group(CG_PATH));
    assertEquals("?#iefix", matcher.group(CG_SUFFIX));
    assertFalse(matcher.find());
  }

  @Test
  public void testFragmentSuffix() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("src: url(\"../fonts/bootstrap/glyphicons-halflings-regular.eot#bla\") format(\"embedded-opentype\"),");
    assertTrue(matcher.find());
    assertEquals("../fonts/bootstrap/glyphicons-halflings-regular.eot#bla", matcher.group(CG_URL));
    assertNull(matcher.group(CG_PROTOCOL));
    assertNull(matcher.group(CG_HOSTPORT));
    assertEquals("../fonts/bootstrap/glyphicons-halflings-regular.eot", matcher.group(CG_PATH));
    assertEquals("#bla", matcher.group(CG_SUFFIX));
    assertFalse(matcher.find());
  }

  @Test
  public void testProtocolLessUri() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("src: url(\"//media.yoox.biz/ytos/resources/MONCLER/icons/icons.eot\"),");
    assertTrue(matcher.find());
    assertEquals("//media.yoox.biz/ytos/resources/MONCLER/icons/icons.eot", matcher.group(CG_URL));
    assertNull(matcher.group(CG_PROTOCOL));
    assertEquals("media.yoox.biz", matcher.group(CG_HOSTPORT));
    assertEquals("/ytos/resources/MONCLER/icons/icons.eot", matcher.group(CG_PATH));
    assertTrue(matcher.group(CG_SUFFIX).isEmpty());
  }

  @Test
  public void testBase64Encoding() {
    Matcher matcher = ThemeImporterImpl.URL_PATTERN.matcher("background: url(data:image/svg+xml;base64,PD94bWwgBlaBlaBla8L3N2Zz4=);");
    assertTrue(matcher.find());
    assertEquals("data:image/svg+xml;base64,PD94bWwgBlaBlaBla8L3N2Zz4=", matcher.group(CG_URL));
    assertEquals("data", matcher.group(CG_PROTOCOL));
    assertNull(matcher.group(CG_HOSTPORT));
    assertEquals("image/svg+xml;base64,PD94bWwgBlaBlaBla8L3N2Zz4=", matcher.group(CG_PATH));
    assertTrue(matcher.group(CG_SUFFIX).isEmpty());
    assertFalse(matcher.find());
  }

  /**
   * Check that urlsToXlinks correctly concats the transformed matches with the
   * text in between.  The actual url transformation is not relevant here.
   */
  @Test
  public void testUrlsToXlinks() {
    String css = "@media screen and (min-width: 1025px) {\n" +
            "  .cm-nav-collapse__gradiant {\n" +
            "    bottom: -20px;\n" +
            "    background: url(http://example.org/path/to/pic.jpg);\n" +
            "    width: 100%;\n" +
            "    background: url(data:image/svg+xml;base64,PD94bWwgFooBarL3N2Zz4=);\n" +
            "    position: absolute;\n" +
            "  }\n" +
            "}\n";
    String expected = "@media screen and (min-width: 1025px) {\n" +
            "  .cm-nav-collapse__gradiant {\n" +
            "    bottom: -20px;\n" +
            "    background: url(<a xlink:href=\"http://example.org/path/to/pic.jpg\">http://example.org/path/to/pic.jpg</a>);\n" +
            "    width: 100%;\n" +
            "    background: url(data:image/svg+xml;base64,PD94bWwgFooBarL3N2Zz4=);\n" +
            "    position: absolute;\n" +
            "  }\n" +
            "}\n";
    ThemeImporterImpl testling = new ThemeImporterImpl(capConnection, mimeTypeService, localizationService, mapToStructAdapter, settingsJsonToMapAdapter);
    String actual = testling.urlsToXlinks(css, null, null);
    assertEquals(expected, actual);
  }

  @Test
  public void testDollarInUrl() {
    when(contentHelper.fetchContent(anyString())).thenReturn(null);
    String js = "var value = \"url($2$foo$)\";";
    String expected = "var value = &quot;url($2$foo$)&quot;;";
    ThemeImporterImpl testling = new ThemeImporterImpl(capConnection, mimeTypeService, localizationService, mapToStructAdapter, settingsJsonToMapAdapter);
    String actual = testling.urlsToXlinks(js, "irrelevant, but not null", contentHelper);
    assertEquals(expected, actual);
  }

  // Customers and/or Thirdparties do such nasty things.
  // This url() expression is from the real world, we must be able to
  // preserve such text.
  @Test
  public void testMixedQuotes() {
    when(contentHelper.fetchContent(anyString())).thenReturn(null);
    String charSalad = "$image.attr(\"style\", \"background-image: url(\"+i(181)+');\");";
    String expected = "$image.attr(&quot;style&quot;, &quot;background-image: url(&quot;+i(181)+&apos;);&quot;);";
    ThemeImporterImpl testling = new ThemeImporterImpl(capConnection, mimeTypeService, localizationService, mapToStructAdapter, settingsJsonToMapAdapter);
    String actual = testling.urlsToXlinks(charSalad, "irrelevant, but not null", contentHelper);
    assertEquals(expected, actual);
  }

  @Test
  public void testFormatDescription() {
    assertEquals("single line", ThemeImporterImpl.formatDescription("single line", 512));
    assertEquals("One line Another line", ThemeImporterImpl.formatDescription("One line\n  Another line", 512));
    assertEquals("First paragraph", ThemeImporterImpl.formatDescription("First paragraph\n\nAnother paragraph", 512));
    assertEquals("That's it", ThemeImporterImpl.formatDescription("That's \nit\n\nbla bla\nblub\n\nfoo\nbar", 512));
    assertEquals("Shortened at blank ...", ThemeImporterImpl.formatDescription("Shortened\n   at blank exactly", 22));
    assertEquals("Shortened at blank ...", ThemeImporterImpl.formatDescription("Shortened\n   at blank inexactly", 23));
    assertEquals("desparatelyshortened...", ThemeImporterImpl.formatDescription("desparatelyshortenedhere", 23));
  }

  @Test
  public void testStringToDocumentName() {
    // Ensure reproducibility of hash, and ignore the hash in the remaining tests.
    assertEquals(ThemeImporterImpl.stringToDocumentName("foo"), ThemeImporterImpl.stringToDocumentName("foo"));
    // If this turns out to fail with different JVMs, just delete it.
    // The actual hash value does not really matter, would just be nice to be
    // reliable, though, so give it a try.
    checkStringToDocumentName("ACBD18DB4CC2F85CEDEF654FCCC4A4D8foo", null, -1, "foo");

    // Simple string
    checkStringToDocumentName(null, "foo", -1, "foo");
    // A value from the real world
    checkStringToDocumentName(null, "https:||maps|googleapis|com|maps|api|js?key=ABCDEFG&amp;libraries=places", -1,
            "https://maps.googleapis.com/maps/api/js?key=ABCDEFG&amp;libraries=places");
    // Bastard value from hell
    checkStringToDocumentName(null, "|||||| \"<'#!§$%&)(=?`>´\\|\\\\", -1, " ../.// \"<'#!§$%&)(=?`>´|\\ ");
    // No use, but robust
    checkStringToDocumentName(null, "", -1, " ");

    // Size corner cases
    String justInSize = "abc---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------xyz";
    checkStringToDocumentName(null, justInSize, 233, justInSize);
    String oneTooLong = "abc----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------xyz";
    checkStringToDocumentName(null, oneTooLong.substring(1), 233, oneTooLong);
    String explodingTooLong = "abc|||g||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||xyz";
    String expectedExclHash = "|g\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|\\|xyz";
    checkStringToDocumentName(null, expectedExclHash, 233, explodingTooLong);

    // Beyond FFFF
    String smile = "\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00z";
    checkStringToDocumentName(null, smile, 233, smile);
    String halfACharTooLong = "\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00";
    checkStringToDocumentName(null, halfACharTooLong.substring(2), 232, halfACharTooLong);
  }


  // --- internal ---------------------------------------------------

  private static void checkStringToDocumentName(String expected, String expectedExclHash, int expectedLength, String value) {
    String actual = ThemeImporterImpl.stringToDocumentName(value);

    // Developer's intention
    if (expected != null) {
      assertEquals(expected, actual);
    }
    if (expectedExclHash != null) {
      assertEquals(expectedExclHash, actual.substring(32));
    }
    if (expectedLength >= 0) {
      assertEquals(expectedLength, actual.length());
    }

    // Database constraint
    assertTrue(actual.length() <= 233);
  }

  private static void checkNameOnly(Matcher matcher) {
    assertTrue(matcher.find());
    assertEquals("bigplay.svg", matcher.group(CG_URL));
    assertNull(matcher.group(CG_PROTOCOL));
    assertNull(matcher.group(CG_HOSTPORT));
    assertEquals("bigplay.svg", matcher.group(CG_PATH));
    assertTrue(matcher.group(CG_SUFFIX).isEmpty());
  }
}
