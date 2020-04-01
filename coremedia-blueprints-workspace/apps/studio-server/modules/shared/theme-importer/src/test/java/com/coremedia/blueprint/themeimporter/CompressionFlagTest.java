package com.coremedia.blueprint.themeimporter;

import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@ComponentScan("com.coremedia.cap.common.xml")
@Import(XmlRepoConfiguration.class)
@TestPropertySource(properties = {
        "repository.params.contentschemaxml=classpath:com/coremedia/blueprint/themeimporter/test-doctypes.xml"
})
@ContextConfiguration(classes = CompressionFlagTest.class)
public class CompressionFlagTest {

  private static final String THEMES = "/Themes";
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private CapConnection capConnection;

  private InputStream compressionTheme;
  private ThemeImporterImpl themeImporter;

  @Mock
  private LocalizationService localizationService;

  @Mock
  private MapToStructAdapter mapToStructAdapter;

  @Mock
  private SettingsJsonToMapAdapter settingsJsonToMapAdapter;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    compressionTheme = getClass().getResource("./compression-theme.zip").openStream();

    TikaMimeTypeService tikaMimeTypeService = new TikaMimeTypeService();
    tikaMimeTypeService.init();
    themeImporter = new ThemeImporterImpl(capConnection, tikaMimeTypeService, localizationService, mapToStructAdapter, settingsJsonToMapAdapter);
  }

  @After
  public void tearDown() {
    IOUtils.closeQuietly(compressionTheme);
  }

  @Test
  public void testCompressionFlag() {
    assertThat(themeImporter.importThemes(THEMES, Collections.singletonList(compressionTheme), true, true).isSuccessful()).isTrue();

    // Manual check: The logging contains warnings about
    // unknownuncompressible.js,
    // misconfigureduncompressible.min.js,
    // misconfigureduncompressible.min.css
    // because they are considered as not compressible, but not
    // flagged accordingly in the theme descriptor.

    Content folder = capConnection.getContentRepository().getChild(THEMES).getChild("compression").getChild("js");

    assertThat(folder.getChild("bynameuncompressible.min.js").getBoolean("disableCompress")).isTrue();
    assertThat(folder.getChild("compressible.js").getBoolean("disableCompress")).isFalse();
    assertThat(folder.getChild("knownuncompressible.js").getBoolean("disableCompress")).isTrue();
    assertThat(folder.getChild("misconfigureduncompressible.min.js").getBoolean("disableCompress")).isTrue();
    assertThat(folder.getChild("unknownuncompressible.js").getBoolean("disableCompress")).isTrue();
    assertThat(folder.getChild("unwantedcompressible.js").getBoolean("disableCompress")).isTrue();

    folder = capConnection.getContentRepository().getChild(THEMES).getChild("compression").getChild("css");

    assertThat(folder.getChild("bynameuncompressible.min.css").getBoolean("disableCompress")).isTrue();
    assertThat(folder.getChild("compressible.css").getBoolean("disableCompress")).isFalse();
    assertThat(folder.getChild("misconfigureduncompressible.min.css").getBoolean("disableCompress")).isTrue();
    assertThat(folder.getChild("unwantedcompressible.css").getBoolean("disableCompress")).isTrue();
  }
}
