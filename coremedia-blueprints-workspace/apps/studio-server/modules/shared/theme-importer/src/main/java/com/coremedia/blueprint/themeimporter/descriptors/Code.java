package com.coremedia.blueprint.themeimporter.descriptors;

import javax.xml.bind.annotation.XmlAttribute;

public abstract class Code extends Resource {
  private String ieExpression;
  private boolean disableCompress;
  private boolean notLinked;

  public String getIeExpression() {
    return ieExpression;
  }

  @XmlAttribute
  public void setIeExpression(String ieExpression) {
    this.ieExpression = ieExpression;
  }

  public boolean isNotLinked() {
    return notLinked;
  }

  @XmlAttribute
  public void setNotLinked(boolean notLinked) {
    this.notLinked = notLinked;
  }

  /**
   * @return if compression is disabled
   * @deprecated We will be removing the compression of code from the CAE as the frontend workspace provides
   *             options to compress the code before it is uploaded to the content repository.
   */
  @Deprecated
  public boolean isDisableCompress() {
    return disableCompress;
  }

  /**
   * @param disableCompress TRUE to disable compression, otherwise FALSE
   * @deprecated We will be removing the compression of code from the CAE as the frontend workspace provides
                 options to compress the code before it is uploaded to the content repository.
   */
  @XmlAttribute
  @Deprecated
  public void setDisableCompress(boolean disableCompress) {
    this.disableCompress = disableCompress;
  }
}
