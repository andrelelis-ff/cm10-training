package com.coremedia.blueprint.themeimporter.descriptors;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class Settings {

  @XmlElement
  private List<Setting> setting;

  public List<Setting> getSettings() {
    return this.setting;
  }


  public void setSettings(List<Setting> settings) {
    this.setting = settings;
  }
}
