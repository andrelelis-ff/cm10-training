package com.coremedia.blueprint.themeimporter;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.PathHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettingsJsonToMapAdapterTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private Content content;

  private SettingsJsonToMapAdapter adapter;

  @Before
  public void setUp() {
    adapter = new SettingsJsonToMapAdapter(contentRepository);
  }

  @Test
  public void emptyJson() {
    assertEquals(ImmutableMap.of(), adapter.getMap(null));
  }

  @Test
  public void invalidJson() {
    assertEquals(ImmutableMap.of(), adapter.getMap("{"));
  }

  @Test
  public void testNumber() {
    Map<String, Object> actual = adapter.getMap("{ \"number\": 1.0}");
    assertEquals(ImmutableMap.of("number", 1), actual);
  }

  @Test
  public void testNumberList() {
    Map<String, Object> actual = adapter.getMap("{ \"numbers\": [ 1.0, 3.9, 5 ]}");
    assertEquals(ImmutableMap.of("numbers", ImmutableList.of(1, 3, 5)), actual);
  }

  @Test
  public void testLink() {
    final String path = "/some folder/some content";
    when(contentRepository.getChild(path)).thenReturn(content);
    Map<String, Object> actual = adapter.getMap("{ \"link\": { \"$Link\": \"" + path + "\" }}");
    assertEquals(ImmutableMap.of("link", content), actual);
  }

  @Test
  public void testLinkList() {
    final String path = "/some folder/some content";
    when(contentRepository.getChild(path)).thenReturn(content);
    Map<String, Object> actual = adapter.getMap("{ \"links\": [ { \"$Link\": \"" + path + "\" }, { \"$Link\": \"" + path + "\" } ]}");
    assertEquals(ImmutableMap.of("links", ImmutableList.of(content, content)), actual);
  }

  @Test
  public void testLinkWithBasePath() {
    final String basePath = "/some folder/";
    final String subPath = "some content";

    when(contentRepository.getChild(PathHelper.join(basePath, subPath))).thenReturn(content);
    Map<String, Object> actual = adapter.getMap("{ \"link\": { \"$Link\": \"" + subPath + "\" }}", basePath);
    assertEquals(ImmutableMap.of("link", content), actual);
  }

  @Test
  public void testLinkWithBasePathAbsolute() {
    final String subPath = "/some content";

    when(contentRepository.getChild(subPath)).thenReturn(content);
    Map<String, Object> actual = adapter.getMap("{ \"link\": { \"$Link\": \"" + subPath + "\" }}", "/some folder/");
    assertEquals(ImmutableMap.of("link", content), actual);
  }

  @Test
  public void testInvalidLink() {
    Map<String, Object> actual = adapter.getMap("{ \"link\": { \"$Link\": \"/some invalid content\" }}");
    assertEquals(1, actual.size());
    assertNull(actual.get("link"));
  }

  @Test
  public void testCalendar() {
    final Calendar calendar = Calendar.getInstance();
    // ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT does not have milliseconds
    calendar.set(Calendar.MILLISECOND, 0);
    String dateAsString = DateFormatUtils.format(calendar, DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern());
    Map<String, Object> actual = adapter.getMap("{ \"date\": { \"$Date\": \"" + dateAsString + "\" }}");
    assertEquals(ImmutableMap.of("date", calendar), actual);
  }

  @Test
  public void testCalendarList() {
    final Calendar calendar = Calendar.getInstance();
    // ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT does not have milliseconds
    calendar.set(Calendar.MILLISECOND, 0);
    String dateAsString = DateFormatUtils.format(calendar, DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern());
    Map<String, Object> actual = adapter.getMap("{ \"links\": [ { \"$Date\": \"" + dateAsString + "\" }, { \"$Date\": \"" + dateAsString + "\" } ]}");
    assertEquals(ImmutableMap.of("links", ImmutableList.of(calendar, calendar)), actual);
  }

  @Test
  public void testInvalidCalendar() {
    Map<String, Object> actual = adapter.getMap("{ \"date\": { \"$Date\": \"invalid\" }}");
    assertEquals(1, actual.size());
    assertNull(actual.get("date"));
  }
}
