package com.coremedia.blueprint.themeimporter;

import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.blueprint.themeimporter.descriptors.Code;
import com.coremedia.blueprint.themeimporter.descriptors.Css;
import com.coremedia.blueprint.themeimporter.descriptors.JavaScript;
import com.coremedia.blueprint.themeimporter.descriptors.Resource;
import com.coremedia.blueprint.themeimporter.descriptors.ResourceBundle;
import com.coremedia.blueprint.themeimporter.descriptors.Setting;
import com.coremedia.blueprint.themeimporter.descriptors.ThemeDefinition;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.XmlGrammar;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.themeimporter.ThemeImporter;
import com.coremedia.cap.themeimporter.ThemeImporterResult;
import com.coremedia.cap.themeimporter.ThemeImporterResultImpl;
import com.coremedia.mimetype.MimeTypeService;
import com.coremedia.common.util.PathUtil;
import com.coremedia.common.util.WordAbbreviator;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import com.coremedia.xml.XmlUtil5;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.activation.MimeTypeParseException;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * The theme importer takes a zip file created by frontend workspace tools and uploads it into the content repository.
 */
public class ThemeImporterImpl implements ThemeImporter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ThemeImporterImpl.class);

  private static final Integer INTEGER_TRUE = 1;

  private static final String DISABLE_COMPRESS_PROPERTY = "disableCompress";
  private static final String IE_EXPRESSION_PROPERTY = "ieExpression";
  private static final String IN_HEAD_PROPERTY = "inHead";
  private static final String HTML_ATTRIBUTES_PROPERTY = "htmlAttributes";
  private static final String DATA_URL_PROPERTY = "dataUrl";
  private static final String CODE_PROPERTY = "code";
  private static final String DATA_PROPERTY = "data";
  private static final String ARCHIVE_PROPERTY = "archive";
  private static final String SETTINGS_PROPERTY = "settings";
  private static final String CM_IMAGE_DOCTYPE = "CMImage";
  private static final String CM_RESOURCE_BUNDLE_DOCTYPE = "CMResourceBundle";
  private static final String CM_INTERACTIVE_DOCTYPE = "CMInteractive";
  private static final String CM_TEMPLATE_SET_DOCTYPE = "CMTemplateSet";
  private static final String CM_JAVA_SCRIPT_DOCTYPE = "CMJavaScript";
  private static final String CM_CSS_DOCTYPE = "CMCSS";
  private static final String CM_SETTINGS_DOCTYPE = "CMSettings";
  private static final String CM_THEME_DOCTYPE = "CMTheme";

  private static final String MARKUP_TEMPLATE = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><p>%s</p></div>";
  private static final String NEW_P = "</p><p>";
  private static final String EMPTY_LINES_REGEX = "(\r\r|\n\n|\r\n\r\n)";
  private static final Pattern EMPTY_LINES_PATTERN = Pattern.compile(EMPTY_LINES_REGEX);
  private static final Pattern FIRST_PARAGRAPH_PATTERN = Pattern.compile("(?s)(.*?)"+EMPTY_LINES_REGEX);
  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(?s)\\s+");
  private static final Pattern SETTINGS_JSON = Pattern.compile("(.+).settings.json$");

  /**
   * Name of the metadata directory in a theme zip file.
   */
  static final String THEME_METADATA_DIR = "THEME-METADATA";

  // The pattern means                     url  ("        prtcl :   //hostport        the/path     suffix     "        )
  private static final String URL_REGEX = "url\\([\"\']?(((\\w*):)?(//([^/)\"\'#?]*))?([^)\"\'#?]*)([^)\"']*))[\"\']?\\)";
  // Capturing groups                                   123    3 2 4  5            54 6           67        71
  @VisibleForTesting static final int CG_URL = 1;
  @VisibleForTesting static final int CG_PROTOCOL = 3;
  @VisibleForTesting static final int CG_HOSTPORT = 5;
  @VisibleForTesting static final int CG_PATH = 6;
  @VisibleForTesting static final int CG_SUFFIX = 7;

  @VisibleForTesting static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  private final MimeTypeService mimeTypeService;
  private final CapConnection capConnection;
  private final LocalizationService localizationService;
  private final MapToStructAdapter mapToStructAdapter;
  private final SettingsJsonToMapAdapter settingsJsonToMapAdapter;


  // --- construct and configure ------------------------------------

  public ThemeImporterImpl(@NonNull CapConnection capConnection,
                           @NonNull MimeTypeService mimeTypeService,
                           @NonNull LocalizationService localizationService,
                           @NonNull MapToStructAdapter mapToStructAdapter,
                           @NonNull SettingsJsonToMapAdapter settingsJsonToMapAdapter) {
    this.mimeTypeService = mimeTypeService;
    this.capConnection = capConnection;
    this.localizationService = localizationService;
    this.mapToStructAdapter = mapToStructAdapter;
    this.settingsJsonToMapAdapter = settingsJsonToMapAdapter;
  }


  // --- features ---------------------------------------------------

  @Override
  public ThemeImporterResult importThemes(@NonNull String targetFolder, Collection<InputStream> zips, boolean checkInAfterImport, boolean cleanBeforeImport) {
    ImportData importData;
    try {
      importData = extractImportDataFromStreams(zips);
    } catch (Exception e) {
      return fail(targetFolder, e);
    }
    return importAndCleanUp(targetFolder, importData, checkInAfterImport, cleanBeforeImport);
  }

  @Override
  public ThemeImporterResult importCodeResource(@NonNull String targetFolder, @NonNull String path, InputStream inputStream, boolean checkInAfterImport) {
    ImportData importData;
    try {
      importData = new ImportData(mimeTypeService, capConnection);
      importData.addFileToImport(inputStream, path);
    } catch (Exception e) {
      return fail(targetFolder, e);
    }
    return importAndCleanUp(targetFolder, importData, checkInAfterImport, false);
  }

  @Override
  public ThemeImporterResult deleteCodeResource(String targetFolder, String originalPath) {
    if (PathUtil.isReferringToParent(originalPath)) {
      throw new IllegalArgumentException("path leaving import folder not allowed for imported file");
    }

    ThemeImporterResultImpl result = new ThemeImporterResultImpl();

    String absolutePath = PathUtil.normalizePath("/" + targetFolder + "/" + originalPath);
    Content content = capConnection.getContentRepository().getChild(absolutePath);
    if (content != null && !content.isDeleted()) {
      boolean success = new ThemeImporterContentHelper(capConnection, result).deleteContent(content);
      if (!success) {
        LOGGER.warn("Code resource deletion failed, no changes in content repository");
      }
    }

    return result;
  }


  // --- internal ---------------------------------------------------

  private ImportData extractImportDataFromStreams(Collection<InputStream> zips) throws IOException, MimeTypeParseException, ParserConfigurationException, SAXException {
    ImportData importData = new ImportData(mimeTypeService, capConnection);
    for (InputStream zip : zips) {
      importData.collectFilesToImport(zip);
    }
    return importData;
  }

  private ThemeImporterResult fail(@NonNull String targetFolder, Exception e) {
    LOGGER.error("Theme import failed, no changes in content repository", e);
    ThemeImporterResultImpl failedResult = new ThemeImporterResultImpl();
    failedResult.addFailure(targetFolder);
    return failedResult;
  }

  private ThemeImporterResult importAndCleanUp(@NonNull String targetFolderPath, ImportData importData, boolean checkInAfterImport, boolean cleanBeforeImport) {
    ThemeImporterResultImpl result = new ThemeImporterResultImpl();
    ThemeImporterContentHelper contentHelper = new ThemeImporterContentHelper(capConnection, result);
    String normalizedTargetFolder = normalize(targetFolderPath);
    try {
      if (cleanBeforeImport) {
        for (String affectedTheme : importData.getAffectedThemes()) {
          contentHelper.initiallyDeleteSubfolder(targetFolderPath, affectedTheme);
        }
      }
      processAll(importData, normalizedTargetFolder, contentHelper, result);
      if (checkInAfterImport) {
        contentHelper.checkInAll();
      }
    } catch (Exception e) {
      contentHelper.revertAll();
      LOGGER.error("Theme import failed", e);
      result.addFailure(normalizedTargetFolder);
    }
    return result;
  }

  /**
   * Import the importData
   *
   * @param importData the source data to be processed
   * @param targetFolder the folder to import the theme to
   */
  private void processAll(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper, ThemeImporterResultImpl result) {
    // Linked content first, then linking content.

    processImages(importData, targetFolder, contentHelper);
    processResourceBundles(importData, targetFolder, contentHelper);
    processWebFonts(importData, targetFolder, contentHelper);
    processInteractiveObjects(importData, targetFolder, contentHelper);
    processTemplates(importData, targetFolder, contentHelper);
    processJavaScripts(importData, targetFolder, contentHelper);
    processStyleSheets(importData, targetFolder, contentHelper);
    // settings may reference all kind of aother resources, so they need to be processed last
    processSettings(importData, targetFolder, contentHelper);

    // The theme descriptors are the last files to be processed.
    // processXmlFiles modifies the code resources contents again, assuming
    // that they exist meanwhile.
    processXmlFiles(importData, targetFolder, contentHelper, result);
  }

  private void processImages(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper) {
    for (Map.Entry<String, Blob> image : importData.getImages().entrySet()) {
      contentHelper.updateContent(CM_IMAGE_DOCTYPE, targetFolder, image.getKey(), Collections.singletonMap(DATA_PROPERTY, image.getValue()));
    }
  }

  private void processResourceBundles(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper) {
    for (Map.Entry<String, String> propertyFile : importData.getResourceBundles().entrySet()) {
      Struct localization = contentHelper.propertiesToStruct(propertyFile.getValue());
      Map<String, Object> properties = new HashMap<>();
      properties.put("localizations", localization);
      contentHelper.updateContent(CM_RESOURCE_BUNDLE_DOCTYPE, targetFolder, propertyFile.getKey(), properties);
    }
  }

  private void processWebFonts(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper) {
    for (Map.Entry<String, Blob> webFont : importData.getWebFonts().entrySet()) {
      Map<String, Blob> properties = Collections.singletonMap(DATA_PROPERTY, webFont.getValue());
      contentHelper.updateContent(CM_IMAGE_DOCTYPE, targetFolder, webFont.getKey(), properties);
    }
  }

  private void processInteractiveObjects(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper) {
    for (Map.Entry<String, Blob> interactive : importData.getInteractiveObjects().entrySet()) {
      Map<String, Blob> properties = Collections.singletonMap(DATA_PROPERTY, interactive.getValue());
      contentHelper.updateContent(CM_INTERACTIVE_DOCTYPE, targetFolder, interactive.getKey(), properties);
    }
  }

  private void processTemplates(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper) {
    for (Map.Entry<String, Blob> templateSet : importData.getTemplateSets().entrySet()) {
      Map<String, Blob> properties = Collections.singletonMap(ARCHIVE_PROPERTY, templateSet.getValue());
      contentHelper.updateContent(CM_TEMPLATE_SET_DOCTYPE, targetFolder, templateSet.getKey(), properties);
    }
  }

  private void processJavaScripts(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper) {
    for (Map.Entry<String, String> js : importData.getJavaScripts().entrySet()) {
      Map<String, Object> properties = new HashMap<>();
      properties.put(CODE_PROPERTY, createMarkup(js.getValue(), js.getKey(), targetFolder, contentHelper));
      contentHelper.updateContent(CM_JAVA_SCRIPT_DOCTYPE, targetFolder, js.getKey(), properties);
    }
  }

  private void processStyleSheets(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper) {
    // CSSs may reference one another, they all must exist as content before
    // we start processing them.
    for (Map.Entry<String, String> css : importData.getStyleSheets().entrySet()) {
      contentHelper.ensureContent(CM_CSS_DOCTYPE, targetFolder, css.getKey());
    }
    for (Map.Entry<String, String> css : importData.getStyleSheets().entrySet()) {
      Map<String, Object> properties = new HashMap<>();
      properties.put(CODE_PROPERTY, createMarkup(css.getValue(), css.getKey(), targetFolder, contentHelper));
      contentHelper.updateContent(CM_CSS_DOCTYPE, targetFolder, css.getKey(), properties);
    }
  }

  @NonNull
  private String extractSettingsName(@NonNull String settingsJson) {
    Matcher matcher = SETTINGS_JSON.matcher(settingsJson);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return settingsJson;
  }

  private void processSettings(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper) {
    // Settings may reference one another, they all must exist as content before
    // we start processing them.
    List<ContentWithSettingsJson> settings = importData.getSettings().entrySet().stream().map(
            setting -> new ContentWithSettingsJson(contentHelper.ensureContent(CM_SETTINGS_DOCTYPE, targetFolder, extractSettingsName(setting.getKey())), setting.getValue()))
            .collect(Collectors.toList());

    for (ContentWithSettingsJson setting : settings) {
      Content settingsContent = setting.getContent();
      String settingsJson = setting.getSettingsJson();

      Map<String, Object> properties = new HashMap<>();

      Map<String, Object> mapping = settingsJsonToMapAdapter.getMap(settingsJson, settingsContent.getParent().getPath());
      properties.put(SETTINGS_PROPERTY, mapToStructAdapter.getStruct(mapping));

      contentHelper.updateContent(CM_SETTINGS_DOCTYPE, settingsContent.getPath(), properties);
    }
  }

  private void processXmlFiles(ImportData importData, String targetFolder, ThemeImporterContentHelper contentHelper, ThemeImporterResultImpl result) {
    for (Map.Entry<String, Document> xmlFile : importData.getThemeDescriptors().entrySet()) {
      if (xmlFile.getKey().contains(THEME_METADATA_DIR)) {
        ThemeDefinition themeDefinition = domToThemeDefinition(xmlFile.getValue());
        if (themeDefinition!=null) {
          Content themeDescriptor = createTheme(themeDefinition, targetFolder, contentHelper, importData);
          if (themeDescriptor!=null) {
            result.addThemeDescriptor(themeDescriptor);
          }
        }
      }
    }
  }

  private static ThemeDefinition domToThemeDefinition(Document document) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(ThemeDefinition.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (ThemeDefinition) jaxbUnmarshaller.unmarshal(document);
    } catch (JAXBException e) {
      LOGGER.error("Cannot extract theme from DOM", e);
      return null;
    }
  }

  private static List<Content> fetchCodeResources(Map<? extends Code, Optional<Content>> codes) {
    return codes.entrySet().stream()
            .filter(entrySet -> !entrySet.getKey().isNotLinked())
            .map(Map.Entry::getValue)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
  }

  /**
   * This method defines some conventions that the Studio configuration (the ThemeSelectorForm) and the CAE (the ThemeResourceLinkBuilder)
   * rely on. Make sure to update them if you change something here.
   */
  private Content createTheme(ThemeDefinition themeDefinition, String targetFolder, ThemeImporterContentHelper contentHelper, ImportData importData) {
    String themeName = themeDefinition.getName();
    String baseFolder = targetFolder + themeName; // make sure to update the ThemeResourceLinkBuilder and ThemeSelectorForm accordingly
    Map<String, Object> properties = new HashMap<>();

    properties.put("viewRepositoryName", themeDefinition.getViewRepositoryName());
    properties.put("description", formatDescription(themeDefinition.getDescription(), 512));
    properties.put("detailText", formatDetailText(themeDefinition.getDescription()));
    properties.put("icon", fetchAndDeleteThumbnail(themeDefinition.getThumbnail(), baseFolder, contentHelper));

    Map<? extends JavaScript, Optional<Content>> jScriptLibs = enhanceJavaScripts(themeDefinition.getJavaScriptLibraries().getJavaScripts(), baseFolder, themeName, contentHelper, importData);
    Map<? extends JavaScript, Optional<Content>> jScripts = enhanceJavaScripts(themeDefinition.getJavaScripts().getJavaScripts(), baseFolder, themeName, contentHelper, importData);
    Map<? extends Css, Optional<Content>> csss = enhanceCsss(themeDefinition.getStyleSheets().getCss(), baseFolder, themeName, contentHelper);
    Map<? extends ResourceBundle, Optional<Content>> resourceBundles = enhanceResourceBundles(themeDefinition.getResourceBundles().getResourceBundles(), baseFolder, contentHelper);
    List<Content> templateSets = fetchExistingResourceContents(themeDefinition.getTemplateSets().getTemplateSet(), baseFolder, contentHelper);
    List<Content> settings = fetchExistingSettingsContents(themeDefinition.getSettings().getSettings(), baseFolder, contentHelper);

    properties.put("javaScriptLibs", fetchCodeResources(jScriptLibs));
    properties.put("javaScripts", fetchCodeResources(jScripts));
    properties.put("css", fetchCodeResources(csss));
    properties.put("resourceBundles", resourceBundles.values().stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
    properties.put("templateSets", templateSets);
    properties.put("linkedSettings", settings);

    String themeContentName = StringUtils.capitalize(themeName) + " Theme"; // make sure to update the ThemeResourceLinkBuilder accordingly
    Content themeContent = contentHelper.updateContent(CM_THEME_DOCTYPE, baseFolder, themeContentName, properties);
    if (themeContent!=null) {
      LOGGER.info("Created Theme in {}", themeContent.getPath());
    } else {
      LOGGER.error("Cannot create theme content.");
    }
    return themeContent;
  }

  /**
   * Reduce the plain text description to a simple String.
   * <ol>
   *   <li>Take only the "first paragraph", wrt. empty lines.</li>
   *   <li>Replace multiple whitespaces with a single space, esp. in order to eliminate newline+indentation formatting.</li>
   *   <li>Shorten to the given maximum length.</li>
   * </ol>
   */
  @VisibleForTesting
  static String formatDescription(String description, int maxLength) {
    if (!StringUtils.hasText(description)) {
      return null;
    }
    Matcher matcher = FIRST_PARAGRAPH_PATTERN.matcher(description);
    String firstP = matcher.find() ? matcher.group(1) : description;
    firstP = WHITESPACE_PATTERN.matcher(firstP.trim()).replaceAll(" ");
    return new WordAbbreviator().abbreviateString(firstP, maxLength);
  }

  private static Markup formatDetailText(String description) {
    if (!StringUtils.hasText(description)) {
      return null;
    }
    String detailText = String.format(MARKUP_TEMPLATE, EMPTY_LINES_PATTERN.matcher(description).replaceAll(NEW_P));
    return MarkupFactory.fromString(detailText).withGrammar(XmlGrammar.RICH_TEXT_1_0_NAME);
  }

  private Blob fetchAndDeleteThumbnail(String thumbnailName, String baseFolder, ThemeImporterContentHelper contentHelper) {
    if (!StringUtils.hasText(thumbnailName)) {
      return null;
    }
    Content thumbnail = contentHelper.fetchContent(PathUtil.normalizePath(baseFolder + "/" + thumbnailName));
    Blob result = null;
    if (thumbnail!=null) {
      result = thumbnail.getBlob(DATA_PROPERTY);
      LOGGER.debug("Delete thumbnail {}, because it is supposed to be temporary.", thumbnail);
      contentHelper.deleteContent(thumbnail);
    }
    return result;
  }

  /**
   * Enhance code contents with data from the theme descriptor.
   * <p>
   * The code resources are imported before the theme descriptor, so they are
   * supposed to exist in the content repository when this is invoked.
   *
   * @return the contents that correspond to the resources
   */
  @NonNull
  private Map<? extends Css, Optional<Content>> enhanceCsss(@NonNull List<? extends Css> resources,
                                    String baseFolder,
                                    String themeName,
                                    ThemeImporterContentHelper contentHelper) {
    return resources.stream()
            .collect(toLinkedHashMap(
                    Function.identity(),
                    css -> enhanceCss(baseFolder, css, themeName, contentHelper))
            );
  }

  @NonNull
  private Map<? extends JavaScript, Optional<Content>> enhanceJavaScripts(@NonNull List<? extends JavaScript> resources,
                                           String baseFolder,
                                           String themeName,
                                           ThemeImporterContentHelper contentHelper,
                                           ImportData importData) {
    return resources.stream()
            .collect(toLinkedHashMap(
                    Function.identity(),
                    javaScript -> enhanceJavaScript(baseFolder, javaScript, themeName, contentHelper, importData))
            );
  }

  @NonNull
  private Optional<Content> enhanceCss(String baseFolder,
                             Css code,
                             String themeName,
                             ThemeImporterContentHelper contentHelper) {
    Map<String, Object> codeProperties = extractCssProperties(code);

    // Adjust non-appropriate properties
    if (!INTEGER_TRUE.equals(codeProperties.get(DISABLE_COMPRESS_PROPERTY)) && !isExternalLink(code.getLink())) {
      // Flag not set in theme descriptor, i.e. the CSS code is supposed
      // to be compressible.  Countercheck and possibly overrule:
      String sourcePath = PathUtil.normalizePath(themeName + "/" + code.getLink());
      if (!checkCompressibleCSS(sourcePath)) {
        codeProperties.put(DISABLE_COMPRESS_PROPERTY, INTEGER_TRUE);
      }
    }

    return enhancedCodeToContent(code, baseFolder, CM_CSS_DOCTYPE, codeProperties, contentHelper);
  }

  @NonNull
  private Optional<Content> enhanceJavaScript(String baseFolder,
                                    JavaScript code,
                                    String themeName,
                                    ThemeImporterContentHelper contentHelper,
                                    ImportData importData) {
    Map<String, Object> codeProperties = extractJavaScriptProperties(code);

    // Adjust non-appropriate properties
    if (!INTEGER_TRUE.equals(codeProperties.get(DISABLE_COMPRESS_PROPERTY)) && !isExternalLink(code.getLink())) {
      // Flag not set in theme descriptor, i.e. the javascript code is supposed
      // to be compressible.  Countercheck and possibly overrule:
      String sourcePath = PathUtil.normalizePath(themeName + "/" + code.getLink());
      String jsCode = importData.getJavaScripts().get(sourcePath);
      // jsCode is supposed to be not null here. Do not warn however, because
      // enhancedCodeToContent will complain about the missing content anyway.
      if (jsCode!=null && !checkCompressibleJavaScript(jsCode, sourcePath)) {
        codeProperties.put(DISABLE_COMPRESS_PROPERTY, INTEGER_TRUE);
      }
    }

    return enhancedCodeToContent(code, baseFolder, CM_JAVA_SCRIPT_DOCTYPE, codeProperties, contentHelper);
  }

  @NonNull
  private Optional<Content> enhancedCodeToContent(Code code, String baseFolder, String contentType, Map<String, Object> codeProperties, ThemeImporterContentHelper contentHelper) {
    String link = code.getLink();
    if (!isExternalLink(link)) {
      String path = PathUtil.normalizePath(baseFolder + "/" + link);
      // Since the theme descriptor is processed at last, any content code
      // resources must exist meanwhile.
      Content content = contentHelper.fetchContent(path);
      if (content==null) {
        LOGGER.warn("Referenced code resource {} does not exist, ignore.", path);
        return Optional.empty();
      } else {
        contentHelper.updateContent(contentType, path, codeProperties);
        // Return the content in any case, even if updateContent failed,
        // so that it is included in the theme descriptor's link list.
        return Optional.of(content);
      }
    } else {
      String path = "external/" + stringToDocumentName(link);
      // No corresponding theme internal resource, so this content is created
      // here when it occurs in a theme descriptor.
      return Optional.ofNullable(contentHelper.updateContent(contentType, normalize(baseFolder), path, codeProperties));
    }
  }

  private Map<String, Object> extractCssProperties(Css code) {
    return extractCodeProperties(code);
  }

  private Map<String, Object> extractJavaScriptProperties(JavaScript code) {
    Map<String, Object> codeProperties = extractCodeProperties(code);
    codeProperties.put(IN_HEAD_PROPERTY, code.isInHead() ? 1 : 0);
    if (code.isDefer()) {
      Struct htmlAttributes = capConnection.getStructService().createStructBuilder().declareBoolean("defer", true).build();
      codeProperties.put(HTML_ATTRIBUTES_PROPERTY, htmlAttributes);
    }
    return codeProperties;
  }

  /**
   * Extract code properties from theme descriptor entry.
   * <p>
   * Returns a mutable map, for further enhancements.
   */
  private Map<String, Object> extractCodeProperties(Code code) {
    Map<String, Object> codeProperties = new HashMap<>();
    codeProperties.put(DISABLE_COMPRESS_PROPERTY, code.isDisableCompress() ? 1 : 0);
    String ieExpression = code.getIeExpression();
    codeProperties.put(IE_EXPRESSION_PROPERTY, ieExpression==null ? "" : ieExpression);
    String link = code.getLink();
    if (isExternalLink(link)) {
      codeProperties.put(DATA_URL_PROPERTY, link);
    }
    return codeProperties;
  }

  /**
   * Enhance resource bundles with data from the theme descriptor.
   * <p>
   * The resource bundles are imported before the theme descriptor, so they are
   * supposed to exist in the content repository when this is invoked.
   *
   * @return the a mapping of of processed resource bundles to the created content (if content was created)
   */
  private Map<? extends ResourceBundle, Optional<Content>> enhanceResourceBundles(List<? extends ResourceBundle> rootBundles, String baseFolder, ThemeImporterContentHelper contentHelper) {
    return rootBundles.stream()
            .collect(toLinkedHashMap(
                    Function.identity(),
                    resourceBundle -> {
                      String contentPath = PathUtil.normalizePath(baseFolder + "/" + resourceBundle.getLink());
                      Content content = contentHelper.fetchContent(contentPath);
                      if (content!=null) {
                        localizationService.hierarchizeResourceBundles(content);
                        return Optional.of(content);
                      }
                      return Optional.empty();
                    }
            ));
  }

  private List<Content> fetchExistingResourceContents(List<? extends Resource> resources, String baseFolder, ThemeImporterContentHelper contentHelper) {
    List<Content> result = new ArrayList<>();
    if (resources != null) {
      for (Resource resource : resources) {
        String path = PathUtil.normalizePath(baseFolder + "/" + resource.getLink());
        Content content = contentHelper.fetchContent(path);
        if (content != null) {
          result.add(content);
        } else {
          LOGGER.warn("Referenced code resource {} does not exist, ignore.", path);
        }
      }
    }
    return result;
  }

  private List<Content> fetchExistingSettingsContents(List<? extends Setting> settings, String baseFolder, ThemeImporterContentHelper contentHelper) {
    List<Content> result = new ArrayList<>();
    if (settings != null) {
      for (Setting setting : settings) {
        String path = PathUtil.normalizePath(baseFolder + "/" + extractSettingsName(setting.getLink()));
        Content content = contentHelper.fetchContent(path);
        if (content != null) {
          result.add(content);
        } else {
          LOGGER.warn("Referenced setting {} does not exist, ignore.", path);
        }
      }
    }
    return result;
  }

  private boolean checkCompressibleJavaScript(String jsCode, String name) {
    if (name.endsWith(".min.js")) {
      LOGGER.warn("The name {} indicates that the JavaScript is already minified and thus cannot be compressed significantly.", name);
      return false;
    }
    if (!JavaScriptCompressionValidator.isCompressible(name, jsCode)) {
      LOGGER.warn("Our JS compressor cannot handle {}.  See DEBUG logging for details.", name);
      return false;
    }
    return true;
  }

  private boolean checkCompressibleCSS(String name) {
    if (name.endsWith(".min.css")) {
      LOGGER.warn("The name {} indicates that the CSS is already minified and thus cannot be compressed significantly.", name);
      return false;
    }
    return true;
  }

  private Markup createMarkup(String text, String fileName, String targetFolder, ThemeImporterContentHelper contentHelper) {
    String targetDocumentPath = targetFolder+fileName;
    String markupAsString = String.format(MARKUP_TEMPLATE, urlsToXlinks(text, targetDocumentPath, contentHelper));
    return MarkupFactory.fromString(markupAsString).withGrammar(XmlGrammar.RICH_TEXT_1_0_NAME);
  }

  @VisibleForTesting
  String urlsToXlinks(String line, String targetDocumentPath, ThemeImporterContentHelper contentHelper) {
    Matcher matcher = URL_PATTERN.matcher(line);
    StringBuffer appender = new StringBuffer();  // NOSONAR Matcher#appendReplacement needs a StringBuffer
    StringBuilder result = new StringBuilder();
    int startNoMatch = 0;
    int endNoMatch;

    while (matcher.find()) {
      // Matcher does not support access to the non-matching part during an
      // appendReplacement loop. So we have to escape the part before the match...
      endNoMatch = matcher.start();
      result.append(XmlUtil5.escapeOrOmit(line.substring(startNoMatch, endNoMatch)));

      String uri = matcher.group(CG_URL);
      String protocol = matcher.group(CG_PROTOCOL);
      String hostport = matcher.group(CG_HOSTPORT);
      String path = matcher.group(CG_PATH);
      String suffix = matcher.group(CG_SUFFIX);

      // Replace the "url(...)" by the according RichText snippet.
      // If we don't understand the "url(...)" snippet, keep the original.
      //
      // Impl note: If "url(...)" contains nested parentheses like in
      // "url("+i(181)+");", the regex terminates at the first closing ),
      // i.e. in midst of the actual url value.  This works as long as
      // #urlReplacement fails and we preserve the original text here.  If
      // #urlReplacement ever succeeds in interpreting something like "+i(181",
      // the '+")' would be lost here, leading to unexpected results.
      String richtextifiedUrl = urlReplacement(uri, protocol, hostport, path, suffix, targetDocumentPath, contentHelper);
      String replacement = richtextifiedUrl!=null ? richtextifiedUrl : XmlUtil5.escapeOrOmit(matcher.group(0));

      // ... and append it to the result, incl the non-matching prefix.
      // $s are magic to Matcher#appendReplacement, so we must escape them.
      replacement = replacement.replace("$", "\\$");
      matcher.appendReplacement(appender, replacement);

      // extract the actual url replacement from the appendReplacement result
      result.append(appender.substring(endNoMatch - startNoMatch));

      // loop for next match
      startNoMatch = matcher.end();
      appender.setLength(0);
    }

    // ...and finally not use appendTail but do it manually
    result.append(XmlUtil5.escapeOrOmit(line.substring(startNoMatch)));
    return result.toString();
  }

  /**
   * Try to interpret the given URL fragments and compose a suitable snippet
   * for richtextified code.
   *
   * @return A richtext snippet that wraps the URL, or null if the URL looks meaningless.
   */
  @Nullable
  private String urlReplacement(String uri, String protocol, String hostport, String path, String suffix, String targetDocumentPath, ThemeImporterContentHelper contentHelper) {
    if (protocol == null && hostport == null) {
      return toRichtextInternalLink(path, targetDocumentPath, suffix, contentHelper);
    } else if (DATA_PROPERTY.equals(protocol)) {
      return null;
    } else {
      return toRichtextHref(uri, uri);
    }
  }

  private String toRichtextInternalLink(String path, String targetDocumentPath, String suffix, ThemeImporterContentHelper contentHelper) {
    try {
      boolean isSuitablePath = !StringUtils.isEmpty(path) && !path.startsWith("/");
      String linkPath = isSuitablePath ? PathUtil.concatPath(targetDocumentPath, path) : null;
      Content referencedContent = linkPath!=null ? contentHelper.fetchContent(linkPath) : null;
      if (referencedContent != null) {
        URI linkImportId = new URI("coremedia", "", "/cap/resources/" + contentHelper.id(referencedContent), null);
        if (!StringUtils.isEmpty(suffix)) {
          LOGGER.info("Ignoring link suffix {} of {}, no reasonable way to deal with.", suffix, path);
        }
        if ("css".equals(extension(linkPath))) {
          return toRichtextHref(linkImportId.toString(), path);
        } else {
          return toRichtextImg(linkImportId);
        }
      } else {
        if (linkPath == null) {
          LOGGER.warn("Cannot handle invalid path '{}'", path);
        } else if (linkPath.contains("+") || linkPath.contains("...")) {
          LOGGER.debug("Cannot resolve {}, looks like a pattern or expression", linkPath);
        } else {
          LOGGER.warn("Cannot resolve {}", linkPath);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Cannot handle {}, {}", path, targetDocumentPath, e);
    }
    return null;
  }

  private String toRichtextImg(URI href) {
    StringBuilder builder = new StringBuilder("url(<img xlink:href=\"");
    builder.append(href);
    builder.append("/data\" alt=\"\" xlink:actuate=\"onLoad\" xlink:show=\"embed\" xlink:type=\"simple\"/>");
    builder.append(")");
    return builder.toString();
  }

  private String toRichtextHref(String href, String linktext) {
    return "url(<a xlink:href=\"" + href + "\">" + XmlUtil5.escapeOrOmit(linktext) + "</a>)";
  }

  private static String extension(String uri) {
    int dot = uri.lastIndexOf('.');
    int slash = uri.lastIndexOf('/');
    return slash > dot || dot < 0 || dot >= uri.length() ? "" : uri.substring(dot + 1);
  }

  /**
   * Convert a String into a valid CMS document name.
   */
  @VisibleForTesting
  static String stringToDocumentName(@NonNull String str) {
    String hashStr = hashForUniqueDocumentName(str);

    String name = str.trim();
    name = name.replace("\\", "\\\\");
    name = name.replace("|", "\\|");
    name = name.replace('/', '|');
    name = name.replace('.', '|');

    // 233 chars (16 bit) is the limit of the contentserver. Do not increase!
    int tooLong = name.length() - (233 - hashStr.length());
    String postfix = tooLong <= 0 ? name : name.substring(tooLong);
    // Make sure that you don't end up with half a UTF_16 char.
    if (postfix.length() > 0 && Character.isLowSurrogate(postfix.charAt(0))) {
      postfix = postfix.substring(1);
    }

    return hashStr + postfix;
  }

  private static String hashForUniqueDocumentName(String str) {
    try {
      // This hash is not a matter of security, but only of uniqueness.
      // Therefore, MD5's 16 bytes should suffice.
      byte[] md5s = MessageDigest.getInstance("MD5").digest(str.getBytes(StandardCharsets.UTF_8));
      return DatatypeConverter.printHexBinary(md5s);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Illegal JVM, MessageDigest is required to support MD5.", e);
    }
  }

  /**
   * Only for backward compatibility.
   * <p>
   * This method is buggy and works only for "nice" and "sufficiently unique"
   * links that look pretty much like a file name, like
   * http://example.org/path/mycode.js
   * Otherwise it may fail with exceptions or may be not unique.
   *
   * @deprecated Use stringToDocumentName instead
   */
  @Deprecated
  private static String legacyExternalLinkToDocumentName(String uri) {
    String name = uri;
    name = name.substring(name.lastIndexOf('/') + 1, name.length());
    name = name.substring(0, name.lastIndexOf('.'));
    return name + "." + extension(uri);
  }

  private static String normalize(@NonNull String folder) {
    return PathUtil.normalizePath("/" + folder + "/");
  }

  private static boolean isExternalLink(String link) {
    return link.startsWith("http://") || link.startsWith("https://") || link.startsWith("//");
  }

  private static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedHashMap(
          Function<? super T, ? extends K> keyMapper,
          Function<? super T, ? extends U> valueMapper)
  {
    return Collectors.toMap(
            keyMapper,
            valueMapper,
            (u, v) -> {
              throw new IllegalStateException(String.format("Duplicate key %s", u));
            },
            LinkedHashMap::new
    );
  }

  private class ContentWithSettingsJson {
    private Content content;
    private String settingsJson;

    ContentWithSettingsJson(@NonNull Content content, @NonNull String settingsJson) {
      this.content = content;
      this.settingsJson = settingsJson;
    }

    @NonNull
    Content getContent() {
      return content;
    }

    @NonNull
    String getSettingsJson() {
      return settingsJson;
    }
  }
}
