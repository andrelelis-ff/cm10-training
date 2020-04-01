package com.coremedia.blueprint.themeimporter;

import com.coremedia.cap.struct.StructBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ThemeImporterContentHelperTest {
  private ThemeImporterContentHelper testling = new ThemeImporterContentHelper(null, null);

  @Mock
  StructBuilder structBuilder;


  // --- property parsing -------------------------------------------

  @Test
  public void testSimpleProperties() {
    testling.propertiesToStructBuilder("foo=bar", structBuilder);
    verify(structBuilder, times(1)).declareString("foo", Integer.MAX_VALUE, "bar");
  }

  @Test
  public void testHierarchicalProperties() {
    testling.propertiesToStructBuilder("foo.bar=bar", structBuilder);
    verify(structBuilder, times(1)).declareString("foo.bar", Integer.MAX_VALUE, "bar");
  }

  @Test
  public void testTrimSimpleProperties() {
    testling.propertiesToStructBuilder(" foo = bar ", structBuilder);
    verify(structBuilder, times(1)).declareString("foo", Integer.MAX_VALUE, "bar");
  }

  @Test
  public void testEscapedProperties() {
    // Double \\ in String literal corresponds to single \ read from InputStream
    String line = "foo=\\u00C4 \\u00D6 \\u00DC \\u00E4 \\u00F6 \\u00FC \\u00DF";
    testling.propertiesToStructBuilder(line, structBuilder);
    verify(structBuilder, times(1)).declareString("foo", Integer.MAX_VALUE, "Ä Ö Ü ä ö ü ß");
  }

  // propertiesToStructBuilder trims the property values,
  // thus leading and trailing whitespace vanishes here.
  @Test
  public void testBastardPropertiesFromHell() {
    String line = " foo = \\n\\u0020=bar\\nbar \\n\\u0020 ";
    testling.propertiesToStructBuilder(line, structBuilder);
    verify(structBuilder, times(1)).declareString("foo", Integer.MAX_VALUE, "=bar\nbar");
  }

  @Test
  public void testMultilineProperties() {
    String line = "foo = This is \\\na longer \\\nvalue with \\\n multiple lines.";
    testling.propertiesToStructBuilder(line, structBuilder);
    verify(structBuilder, times(1)).declareString("foo", Integer.MAX_VALUE, "This is a longer value with multiple lines.");
  }

  @Test
  public void testWithNewlines() {
    String line = "foo = This is \\na longer \\nvalue with \\n newlines.";
    testling.propertiesToStructBuilder(line, structBuilder);
    verify(structBuilder, times(1)).declareString("foo", Integer.MAX_VALUE, "This is \na longer \nvalue with \n newlines.");
  }

  /**
   * Just for the record: Leading whitespace in subsequent lines is trimmed.
   * <p>
   * No contract on our behalf, but matter-of-fact behaviour of
   * {@link java.util.Properties#load(InputStream)}.  If you need whitespace
   * at multiline-breaks, put it at the end of the leading line.
   */
  @Test
  public void testMultilineProperties2() {
    String line = "foo = No\\\n space behind line break.";
    testling.propertiesToStructBuilder(line, structBuilder);
    verify(structBuilder, times(1)).declareString("foo", Integer.MAX_VALUE, "Nospace behind line break.");
  }


  // --- internal ---------------------------------------------------

  private void checkProperty(ThemeImporterContentHelper.KeyValue actual, String expectedKey, String expectedValue) {
    assertEquals("wrong key", expectedKey, actual.key);
    assertEquals("wrong value", expectedValue, actual.value);
  }
}
