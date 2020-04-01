package com.coremedia.blueprint.themeimporter.descriptors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ThemeDefinition {
  private int modelVersion;
  private String name;
  private String viewRepositoryName;
  private String description;
  private String thumbnail;
  private StyleSheets styleSheets = new StyleSheets();
  private JavaScripts javaScripts = new JavaScripts();
  private JavaScriptLibraries javaScriptLibraries = new JavaScriptLibraries();
  private ResourceBundles resourceBundles = new ResourceBundles();
  private TemplateSets templateSets = new TemplateSets();
  private Settings settings = new Settings();

  public int getModelVersion() {
    return modelVersion;
  }

  @XmlAttribute(required = true)
  public void setModelVersion(int modelVersion) {
    this.modelVersion = modelVersion;
  }

  public String getName() {
    return name;
  }

  @XmlElement(required = true)
  public void setName(String name) {
    this.name = name;
  }

  public String getViewRepositoryName() {
    return viewRepositoryName;
  }

  @XmlElement(required = true)
  public void setViewRepositoryName(String viewRepositoryName) {
    this.viewRepositoryName = viewRepositoryName;
  }

  public String getDescription() {
    return description;
  }

  @XmlElement
  public void setDescription(String description) {
    this.description = description;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  @XmlElement
  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public StyleSheets getStyleSheets() {
    return styleSheets;
  }

  @XmlElement
  public void setStyleSheets(StyleSheets styleSheets) {
    this.styleSheets = styleSheets;
  }

  public JavaScripts getJavaScripts() {
    return javaScripts;
  }

  @XmlElement
  public void setJavaScripts(JavaScripts javaScripts) {
    this.javaScripts = javaScripts;
  }

  public JavaScriptLibraries getJavaScriptLibraries() {
    return javaScriptLibraries;
  }

  @XmlElement
  public void setJavaScriptLibraries(JavaScriptLibraries javaScriptLibraries) {
    this.javaScriptLibraries = javaScriptLibraries;
  }

  public ResourceBundles getResourceBundles() {
    return resourceBundles;
  }

  @XmlElement
  public void setResourceBundles(ResourceBundles resourceBundles) {
    this.resourceBundles = resourceBundles;
  }

  public TemplateSets getTemplateSets() {
    return templateSets;
  }

  @XmlElement
  public void setTemplateSets(TemplateSets templateSets) {
    this.templateSets = templateSets;
  }

  public Settings getSettings() {
    return settings;
  }

  @XmlElement
  public void setSettings(Settings settings) {
    this.settings = settings;
  }
}
