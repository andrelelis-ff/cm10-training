package com.coremedia.blueprint.themeimporter.descriptors;


import edu.umd.cs.findbugs.annotations.NonNull;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StyleSheets {
  private List<Css> css = new ArrayList<>();

  @XmlElement
  @NonNull
  public List<Css> getCss() {
    return css!=null ? css : Collections.emptyList();
  }

  public void setCss(List<Css> css) {
    this.css = css;
  }
}