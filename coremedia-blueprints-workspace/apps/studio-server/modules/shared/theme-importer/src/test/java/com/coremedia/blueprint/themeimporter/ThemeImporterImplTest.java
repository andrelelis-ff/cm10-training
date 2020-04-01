package com.coremedia.blueprint.themeimporter;

import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.themeimporter.ThemeImporterResult;
import com.coremedia.mimetype.TikaMimeTypeService;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@ComponentScan("com.coremedia.cap.common.xml")
@Import(XmlRepoConfiguration.class)
@TestPropertySource(properties = {
        "repository.params.contentschemaxml=classpath:com/coremedia/blueprint/themeimporter/test-doctypes.xml"

})
@ContextConfiguration(classes = ThemeImporterImplTest.class)
public class ThemeImporterImplTest {

  private static final String THEMES = "/Themes";

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private CapConnection capConnection;

  @Mock
  private LocalizationService localizationService;

  @Mock
  private MapToStructAdapter mapToStructAdapter;

  @Mock
  private SettingsJsonToMapAdapter settingsJsonToMapAdapter;

  private InputStream corporateTheme;
  private ThemeImporterImpl themeImporter;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    corporateTheme = getClass().getResource("./corporate-theme.zip").openStream();

    TikaMimeTypeService tikaMimeTypeService = new TikaMimeTypeService();
    tikaMimeTypeService.init();
    themeImporter = new ThemeImporterImpl(capConnection, tikaMimeTypeService, localizationService, mapToStructAdapter, settingsJsonToMapAdapter);
  }

  @After
  public void tearDown() {
    IOUtils.closeQuietly(corporateTheme);
  }

  @Test
  public void extractFromZip() {
    String corporateThemePath = THEMES + "/corporate/Corporate Theme";

    ThemeImporterResult themeImporterResult = themeImporter.importThemes(THEMES, singletonList(corporateTheme), true, true);

    assertThat(themeImporterResult.isSuccessful()).isTrue();
    assertThat(themeImporterResult.getFailedPaths()).isEmpty();
    assertThat(themeImporterResult.getUpdatedContents().keySet(),
            CoreMatchers.hasItems(CoreMatchers.startsWith(corporateThemePath)));

    Content corporateTheme = capConnection.getContentRepository().getChild(corporateThemePath);

    assertThat(corporateTheme).isNotNull();
    checkLinkList(corporateTheme, "css", 1);
    checkLinkList(corporateTheme, "javaScripts", 16);
    checkLinkList(corporateTheme, "javaScriptLibs", 6);
    checkLinkList(corporateTheme, "templateSets", 2);
    checkLinkList(corporateTheme, "resourceBundles", 4);

    checkOneCheckedInVersion(capConnection.getContentRepository().getChild(THEMES));
  }

  @Test
  public void cleanAndExtractFromZip() {
    ContentRepository contentRepository = capConnection.getContentRepository();

    String testPathCleaned = THEMES + "/corporate/x";
    Content testDocumentCleaned = contentRepository.getContentType("CMCSS").create(contentRepository.getRoot(), testPathCleaned);

    String testPathNotCleaned = THEMES + "/cawporate/x";
    Content testDocumentNotCleaned = contentRepository.getContentType("CMCSS").create(contentRepository.getRoot(), testPathNotCleaned);

    ThemeImporterResult themeImporterResult = themeImporter.importThemes(THEMES, singletonList(corporateTheme), true, true);
    assertThat(testDocumentCleaned.isDeleted()).isTrue();
    assertThat(testDocumentNotCleaned.isDeleted()).isFalse();
    assertThat(themeImporterResult.isSuccessful()).isTrue();
  }

  @Test
  public void writeOnlyChanges() throws IOException {
    assertTrue(themeImporter.importThemes(THEMES, singletonList(corporateTheme), true, true).isSuccessful());
    IOUtils.closeQuietly(corporateTheme);
    checkOneCheckedInVersion(capConnection.getContentRepository().getChild(THEMES).getChild("corporate"));
    // same theme again:
    try (InputStream theme = getClass().getResource("./corporate-theme.zip").openStream()) {
      assertThat(themeImporter.importThemes(THEMES, singletonList(theme), true, false).isSuccessful()).isTrue();
    }
    checkOneCheckedInVersion(capConnection.getContentRepository().getChild(THEMES).getChild("corporate"));
  }

  // --- internal ---------------------------------------------------

  private void checkLinkList(Content corporateTheme, String propertyName, int expected) {
    assertThat(corporateTheme.getProperties().get(propertyName)).isNotNull();
    assertThat(((List) corporateTheme.getProperties().get(propertyName)).size()).isEqualTo(expected);
  }

  private void checkOneCheckedInVersion(Content folder) {
    for (Content child : folder.getChildren()) {
      if (child.isFolder()) {
        checkOneCheckedInVersion(child);
      } else {
        assertThat(child.isCheckedIn()).isTrue();
        assertThat(IdHelper.parseVersionId(child.getCheckedInVersion().getId())).as(child.getId() + ", " + child.getPath()).isEqualTo(1);
      }
    }
  }
}
