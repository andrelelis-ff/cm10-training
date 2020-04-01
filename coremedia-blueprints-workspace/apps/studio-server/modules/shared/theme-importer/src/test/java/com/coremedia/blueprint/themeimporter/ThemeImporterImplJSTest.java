package com.coremedia.blueprint.themeimporter;

import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.themeimporter.ThemeImporterResult;
import com.coremedia.mimetype.TikaMimeTypeService;
import org.apache.commons.io.IOUtils;
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
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@ComponentScan("com.coremedia.cap.common.xml")
@Import(XmlRepoConfiguration.class)
@TestPropertySource(properties = {
        "repository.params.contentschemaxml=classpath:com/coremedia/blueprint/themeimporter/test-doctypes.xml"

})
@ContextConfiguration(classes = ThemeImporterImplJSTest.class)
public class ThemeImporterImplJSTest {

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

  private InputStream jsTheme;
  private ThemeImporterImpl themeImporter;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    jsTheme = getClass().getResource("./testjavascript-theme.zip").openStream();
    TikaMimeTypeService mimeTypeService = new TikaMimeTypeService();
    mimeTypeService.init();
    themeImporter = new ThemeImporterImpl(capConnection, mimeTypeService, localizationService, mapToStructAdapter, settingsJsonToMapAdapter);
  }

  @After
  public void tearDown() {
    IOUtils.closeQuietly(jsTheme);
  }

  @Test
  public void extractFromZip() {
    ThemeImporterResult themeImporterResult = themeImporter.importThemes(THEMES, Collections.singletonList(jsTheme), true, false);

    String jsThemePath = THEMES + "/testjavascript/Testjavascript Theme";
    Content jsTheme = capConnection.getContentRepository().getChild(jsThemePath);
    assertThat(jsTheme).isNotNull();

    List<Content> jsls = jsTheme.getLinks("javaScriptLibs");

    assertThat(jsls).isNotNull();
    assertThat(jsls).hasSize(2);
    checkJs(jsls.get(0), true, "lte IE 9");
    checkJs(jsls.get(1), false, "");

    List<Content> jss = jsTheme.getLinks("javaScripts");

    assertThat(jss).isNotNull();
    assertThat(jss).hasSize(2);
    checkJs(jss.get(0), true, "");
    checkJs(jss.get(1), false, "gte IE 10");

    assertThat(themeImporterResult.getUpdatedContents()).hasSize(6);
    assertThat(themeImporterResult.getUpdatedContents().keySet()).contains("/Themes/testjavascript/vendor/jquery/dist/jquery.js");
    assertThat(themeImporterResult.getFailedPaths()).isEmpty();
  }

  private void checkJs(Content content, boolean inHead, String ieExpression) {
    assertThat(inHead).isEqualTo(content.getBoolean("inHead"));
    assertThat(ieExpression).isEqualTo(content.getString("ieExpression"));
  }
}
