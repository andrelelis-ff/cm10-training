package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AMUtilsTest {

  @Test
  public void testGetDownloadPortalRootDocument() {
    Site site = mock(Site.class);
    SettingsService settingsService = mock(SettingsService.class);
    Content siteRootDocument = mock(Content.class);
    Content downloadPortalRootDocument = mock(Content.class);

    when(site.getSiteRootDocument()).thenReturn(siteRootDocument);
    when(settingsService.nestedSetting(anyList(), eq(Content.class), refEq(siteRootDocument))).thenReturn(downloadPortalRootDocument);

    Content actualContent = AMUtils.getDownloadPortalRootDocument(settingsService, site);
    assertEquals("Expected root page content was not returned", downloadPortalRootDocument, actualContent);
  }

  @Test
  public void testGetPropertiesAsString() {
    GregorianCalendar calendarValue = new GregorianCalendar();

    Map<String, Object> properties = new HashMap<>();
    properties.put("nullProperty", null);
    properties.put("stringProperty", "abc");
    properties.put("intProperty", Integer.MAX_VALUE);
    properties.put("booleanProperty", true);
    properties.put("booleanProperty2", false);
    properties.put("calendarProperty", calendarValue);

    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    formatter.setCalendar(calendarValue);
    String calendarValueAsString = formatter.format(calendarValue.getTime());

    Map<String, String> expectedResult = new HashMap<>();
    // nullProperty will be filtered
    expectedResult.put("stringProperty", "abc");
    expectedResult.put("intProperty", Integer.toString(Integer.MAX_VALUE));
    expectedResult.put("booleanProperty", "true");
    expectedResult.put("booleanProperty2", "false");
    expectedResult.put("calendarProperty", calendarValueAsString);

    assertEquals("Properties are different", expectedResult, AMUtils.getPropertiesAsString(properties));
  }

  @Test
  public void testGetAssetSubtypes() {
    ContentRepository contentRepository = mock(ContentRepository.class);
    ContentType assetBaseType = mock(ContentType.class);
    ContentType contentType1 = mock(ContentType.class);
    ContentType contentType2 = mock(ContentType.class);

    when(contentRepository.getContentType(eq(AMAsset.NAME))).thenReturn(assetBaseType);

    Set<ContentType> subTypes = ImmutableSet.of(contentType1, contentType2);
    when(assetBaseType.getSubtypes()).thenReturn(subTypes);

    when(contentType1.isConcrete()).thenReturn(true);
    when(contentType1.getName()).thenReturn("Test1");

    when(contentType2.isConcrete()).thenReturn(false);

    List<String> expectedAssetSubtypes = ImmutableList.of("Test1");

    List<String> actualAssetSubtypes = AMUtils.getAssetSubtypes(contentRepository);
    assertEquals("Asset subtypes are different", expectedAssetSubtypes, actualAssetSubtypes);
  }
}
