package com.coremedia.blueprint.themeimporter;

import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.themeimporter.ThemeImporterResult;
import com.coremedia.mimetype.TikaMimeTypeService;
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
@ContextConfiguration(classes = DeletionTest.class)
public class DeletionTest {

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private CapConnection capConnection;

  @Mock
  private LocalizationService localizationService;

  @Mock
  private MapToStructAdapter mapToStructAdapter;

  @Mock
  private SettingsJsonToMapAdapter settingsJsonToMapAdapter;

  private ThemeImporterImpl themeImporter;


  @Before
  public void setUp() {
    initMocks(this);
    TikaMimeTypeService tikaMimeTypeService = new TikaMimeTypeService();
    tikaMimeTypeService.init();
    themeImporter = new ThemeImporterImpl(capConnection, tikaMimeTypeService, localizationService, mapToStructAdapter, settingsJsonToMapAdapter);
  }

  @Test
  public void testDeleteCheckedOut() {
    String themePath = "/any/where";
    String originalPath = "/dir/file";

    ContentRepository contentRepository = capConnection.getContentRepository();
    Content document = contentRepository.getContentType("CMCSS").create(contentRepository.getRoot(), themePath + originalPath);

    assertThat(document.isDeleted()).isFalse();
    assertThat(document.isCheckedOut()).isTrue();

    themeImporter.deleteCodeResource(themePath, originalPath);

    assertThat(document.isDeleted()).isTrue();
    assertThat(document.isCheckedOut()).isFalse();
  }

  @Test
  public void testDeleteCheckedIn() {
    String themePath = "/any/where";
    String originalPath = "/dir/file";

    ContentRepository contentRepository = capConnection.getContentRepository();
    Content document = contentRepository.getContentType("CMCSS").create(contentRepository.getRoot(), themePath + originalPath);
    document.checkIn();

    assertThat(document.isDeleted()).isFalse();
    assertThat(document.isCheckedOut()).isFalse();

    ThemeImporterResult themeImporterResult = themeImporter.deleteCodeResource(themePath, originalPath);

    assertThat(themeImporterResult.getFailedPaths()).isEmpty();
    assertThat(Collections.singletonMap(themePath + originalPath, document)).isEqualTo(themeImporterResult.getUpdatedContents());
    assertThat(document.isDeleted()).isTrue();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteBadPath() {
    String themePath = "/any/where";
    String originalPath = "../file";

    themeImporter.deleteCodeResource(themePath, originalPath);
  }
}
