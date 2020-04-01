package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.xml.MarkupUtil;
import com.coremedia.xml.PlaintextSerializer;
import org.xml.sax.SAXException;

import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simply configures a {@link com.coremedia.xml.PlaintextSerializer} in order to
 * have a pretty output of script code that is not escaped. <br/>
 * This means that if the value of a tag contains characters like '<','&','>' those are written directly to the output.
 */
public class ScriptSerializer extends PlaintextSerializer {

  private static final int NBSP = 160;
  /**
   * Flag that we are in a section of leading blanks which are to be ignored.
   */
  private boolean leadingBlanks;

  /**
   * Initializes a new ScriptSerializer with no target writer.
   */
  public ScriptSerializer() {
    this(null);
  }


  /**
   * Initializes a new ScriptSerializer with the given target writer.
   *
   * @param target the target writer
   */
  public ScriptSerializer(Writer target) {
    super(target, MarkupUtil.startElementTranslations(), new HashMap<String, String>());
    super.setCharacterTranslationMap(characterTranslations());
    super.setLeftMargin(0);
    super.setRightMargin(0);
    super.setEncode(false);

    // Put new-lines after the paragraph, not before it.
    // This is crucial for @charset instructions in CSS to work, since
    // according to the spec and implemented like this in Safari, the
    // @charset instruction has to be a the start of the CSS file (no
    // white space allowed!).
    Map<String, String> startTrans = getStartElementTranslationMap();
    startTrans.remove("p");
    startTrans.remove("P");
    Map<String, String> endTrans = getEndElementTranslationMap();
    endTrans.put("p", "\n");
    endTrans.put("P", "\n");
  }


  /**
   * Returns character translations for scripts.
   *
   * @return character translations for scripts.
   */
  protected static Map<Character, String> characterTranslations() {
    // &nbsp; -> " "
    return Collections.singletonMap((char) NBSP, " ");
  }

  @Override
  public void startDocument() throws SAXException {
    leadingBlanks = true;
    super.startDocument();
  }

  @Override
  protected void write(char c) throws SAXException {
    if (!leadingBlanks) {
      super.write(c);
    } else {
      if (!Character.isWhitespace(c)) {
        // End of leading blanks.  All following chars are preserved.
        leadingBlanks = false;
        super.write(c);
      }
    }
  }

  @Override
  protected void write(String s) {
    try {
      if (!leadingBlanks) {
        // Optimization: No need  to care for the single chars after the leadingBlanks section
        super.write(s);
      } else {
        for (int i = 0; i < s.length(); ++i) {
          write(s.charAt(i));
        }
      }
    } catch (SAXException e) {
      throw new IllegalStateException(e);
    }
  }
}