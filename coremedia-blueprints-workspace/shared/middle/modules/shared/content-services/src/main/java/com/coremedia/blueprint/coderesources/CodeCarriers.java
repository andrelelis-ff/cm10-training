package com.coremedia.blueprint.coderesources;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;

class CodeCarriers {
  private Content themeCarrier;
  private Content codeCarrier;

  CodeCarriers() {
  }

  CodeCarriers(Content themeCarrier, Content codeCarrier) {
    setThemeCarrier(themeCarrier);
    setCodeCarrier(codeCarrier);
  }

  Content getThemeCarrier() {
    return themeCarrier;
  }

  void setThemeCarrier(Content themeCarrier) {
    this.themeCarrier = themeCarrier;
  }

  Content getCodeCarrier() {
    return codeCarrier;
  }

  void setCodeCarrier(Content codeCarrier) {
    this.codeCarrier = codeCarrier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CodeCarriers that = (CodeCarriers) o;
    return java.util.Objects.equals(themeCarrier, that.themeCarrier) &&
            java.util.Objects.equals(codeCarrier, that.codeCarrier);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(themeCarrier, codeCarrier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append("[");
    sb.append(themeCarrier==null ? null : IdHelper.parseContentId(themeCarrier.getId()));
    sb.append(", ");
    sb.append(codeCarrier==null ? null : IdHelper.parseContentId(codeCarrier.getId()));
    sb.append("]");
    return sb.toString();
  }
}
