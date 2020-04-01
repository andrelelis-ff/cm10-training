package com.coremedia.blueprint.cae.richtext.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

public class ScriptSerializerTest {
  ScriptSerializer scriptSerializer;

  private static String startingWithEmptyLines = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                                                 "<p></p>" + // this would normally be written as a line break
                                                 "<p>                        </p>" + // this would normally be written as a line break
                                                 "<p>@charset \"UTF-8\";</p>" +
                                                 "</div>";

  private static final String expectedPlainTextStartingWithEmptyLines = "@charset \"UTF-8\";\n";

  @Before
  public void setUp() {
    scriptSerializer = new ScriptSerializer();
  }

  /**
   * Test whether empty lines (line breaks and lines with whitespace characters) are removed before the first
   * non-empty line.
   *
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  @Test
  public void testStartingWithEmptyLines() throws SAXException, IOException, ParserConfigurationException {
    InputSource is = new InputSource(new StringReader(startingWithEmptyLines));

    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    Writer out = new OutputStreamWriter(bas, "UTF-8");
    scriptSerializer = new ScriptSerializer(out);
    SAXParserFactory.newInstance().newSAXParser().parse(is, scriptSerializer);

    String actual = bas.toString("UTF-8");

    Assert.assertEquals("some xml with leading empty lines", expectedPlainTextStartingWithEmptyLines, actual);
  }
}