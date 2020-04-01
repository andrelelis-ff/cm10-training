package com.coremedia.blueprint.elastic.social.util;

import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class BbCodeToCoreMediaRichtextTransformer {
  private static final String RICHTEXT_XML_DECLARATION = "<?xml version=\"1.0\" ?>";
  private static final String RICHTEXT_START_TAG = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
  private static final String RICHTEXT_END_TAG = "</div>";

  // Setup character-to-Html-Entity mapping
  private static final Map<String, String> CHAR_TO_HTML_ENTITY = Collections.unmodifiableMap(new HashMap<String, String>() {{
    put("&", "&amp;");
    put("<", "&lt;");
    put(">", "&gt;");
  }});

  // Setup bbCode-Tag-to-Html mapping
  private static final Map<String, String> BBCODE_REGEX_TO_HTML_REPLACEMENT = Collections.unmodifiableMap(new HashMap<String, String>() {{
    put("\\[url\\](.*?)\\[\\/url]", "<a xlink:href=\"$1\" xlink:show=\"replace\">$1</a>");
    put("\\[url\\=([^\\]]+)](.*?)\\[\\/url]", "<a xlink:href=\"$1\" xlink:show=\"replace\">$2</a>");
    put("\\[b\\](.*?)\\[\\/b]", "<strong>$1</strong>");
    put("\\[i\\](.*?)\\[\\/i]", "<em>$1</em>");
    put("\\[cmQuote\\](.*?)\\[\\/cmQuote]", "</p> <blockquote><p>$1</p></blockquote> <p>");
    put("\\[img\\](.*?)\\[\\/img]", "<img src=\"$1\" />");
    put("\\[quote\\](.*?)\\[\\/quote\\]", "<em>$1</em>");
    put("\\[quote author='(.*?)' date='(.*?)'\\](.*?)\\[\\/quote\\]", "$3");
  }});

  private BbCodeToCoreMediaRichtextTransformer() {
    // prevent client-side instantiation
  }

  /**
   * @return a new {@link BbCodeToCoreMediaRichtextTransformer} instance
   */
  public static BbCodeToCoreMediaRichtextTransformer newInstance() {
    return new BbCodeToCoreMediaRichtextTransformer();
  }

  /**
   * Converts BB-Code into "CoreMedia Richtext 1.0".
   *
   * @param bbCode The BB-Code to be converted into "CoreMedia Richtext 1.0"
   * @return XML containing "CoreMedia Richtext 1.0" representing the passed 'bbCode'
   */
  public Markup transform(final String bbCode) {
    validateBbCode(bbCode);

    String richtextContent = convertHtmlEntities(bbCode);
    richtextContent = convertLineBreaks(richtextContent);
    richtextContent = convertHtmlTags(richtextContent);

    final String richtextXmlAsString = wrapRichtextContent(richtextContent);
    return MarkupFactory.fromString(richtextXmlAsString);
  }

  private static void validateBbCode(final String bbCode) {
    if (bbCode == null) {
      throw new IllegalArgumentException("Argument 'bbCode' must not be null.");
    }
  }

  private String convertHtmlEntities(String richtextContent) {
    return replaceAllFromMap(richtextContent, CHAR_TO_HTML_ENTITY);
  }

  private static String replaceAllFromMap(String richtextContent, Map<String, String> foo) {
    String result = richtextContent;
    for (final Map.Entry<String, String> bbCodeToHtmlMapping : foo.entrySet()) {
      result = result.replaceAll(bbCodeToHtmlMapping.getKey(), bbCodeToHtmlMapping.getValue());
    }
    return result;
  }

  private String convertLineBreaks(String richtextContent) {
    String result = richtextContent;
    result = result.replaceAll("\r\n", "<br/>");
    result = result.replaceAll("\r", "<br/>");
    result = result.replaceAll("\n", "<br/>");
    return result;
  }

  private String convertHtmlTags(String richtextContent) {
    return replaceAllFromMap(richtextContent, BBCODE_REGEX_TO_HTML_REPLACEMENT);
  }

  // wraps the "plain content" in in minimal XML to make it "CoreMedia Richtext 1.0"-compliant
  private String wrapRichtextContent(String richtextContent) {
    final StringBuilder richtextBuilder = new StringBuilder();
    richtextBuilder.append(RICHTEXT_XML_DECLARATION);
    richtextBuilder.append(RICHTEXT_START_TAG);
    richtextBuilder.append("<p>"); // we need a surrounding html element to prevent rendering errors
    richtextBuilder.append(richtextContent);
    richtextBuilder.append("</p>");
    richtextBuilder.append(RICHTEXT_END_TAG);
    return richtextBuilder.toString();
  }
}
