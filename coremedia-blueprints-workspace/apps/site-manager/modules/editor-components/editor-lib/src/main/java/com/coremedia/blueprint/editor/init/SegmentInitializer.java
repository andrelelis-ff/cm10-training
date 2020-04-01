package com.coremedia.blueprint.editor.init;

import hox.corem.editor.initialization.Initializer;
import hox.corem.editor.proxy.DocumentModel;
import hox.corem.editor.proxy.PropertyTypeModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Initializes segment properties with the document name.
 * <p/>
 * Umlauts and spaces are escaped.
 */
public class SegmentInitializer implements Initializer {

  private Map<String, String> characterReplacementMap = new HashMap<>();

  public SegmentInitializer() {
    characterReplacementMap.put("\u00E4", "ae");      // 'a umlaut' -> ae
    characterReplacementMap.put("\u00F6", "oe");      // 'o umlaut' -> oe
    characterReplacementMap.put("\u00FC", "ue");      // 'u umlaut' -> ue
    characterReplacementMap.put("\u00DF", "ss");      // 'sharp s'  -> ss
    characterReplacementMap.put("\u00C4", "Ae");      // 'A umlaut' -> Ae
    characterReplacementMap.put("\u00D6", "Oe");      // 'O umlaut' -> Oe
    characterReplacementMap.put("\u00DC", "Ue");      // 'U umlaut' -> Ue
    characterReplacementMap.put(" ", "_");
    characterReplacementMap.put(",", "_");
  }

  public void setCharacterReplacementMap(Map<String, String> characterReplacementMap) {
    this.characterReplacementMap = characterReplacementMap;
  }

  public Object convertSegment(Object value) {
    boolean hasNonDigit = false;
    StringBuilder sb = new StringBuilder();
    for (char ch : value.toString().toCharArray()) {
      String replacement = characterReplacementMap.get(String.valueOf(ch));
      sb.append(replacement == null ? ch : replacement);
      hasNonDigit |= !Character.isDigit(ch);
    }
    if (!hasNonDigit) {
      sb.insert(0, '-');
    }
    return sb.toString();
  }

  @Override
  public Object getInitialValue(DocumentModel document, PropertyTypeModel propertyType) {
    return convertSegment(document.getName());
  }
}
