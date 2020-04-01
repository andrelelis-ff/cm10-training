package com.coremedia.blueprint.assets.cae.handlers;

import com.coremedia.blueprint.assets.cae.DownloadPortalFactory;
import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetImpl;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DownloadPortalHandlerDownloadCollectionTest {

  public static final int ASSET_CONTENT_ID_AVAILABLE = 60001;
  public static final int ASSET_CONTENT_ID_UNKNOWN = 60002;
  public static final int ASSET_CONTENT_ID_DELETED = 60003;
  public static final int ASSET_CONTENT_ID_DESTROYED = 60004;

  @InjectMocks
  private DownloadPortalHandler handler;

  private MockHttpServletResponse response;

  @Mock
  private Content assetContent;

  @Mock
  private Content assetContentDeleted;

  @Mock
  private Content assetContentDestroyed;

  @Mock
  private AMAssetImpl asset;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private DownloadPortalFactory downloadPortalFactory;

  private static File zipFile;

  @Mock
  private AMAssetRendition rendition;

  @Mock
  private ContentType amAssetContentType;

  private List<AMAssetRendition> renditions;

  @BeforeClass
  public static void prepare() throws IOException {
    zipFile = File.createTempFile("temp-file-name", ".tmp");
  }

  @Before
  public void setUp() throws Exception {
    response = new MockHttpServletResponse();

    when(amAssetContentType.isSubtypeOf(anyString())).thenReturn(true);
    when(assetContent.getType()).thenReturn(amAssetContentType);
    when(assetContentDeleted.getType()).thenReturn(amAssetContentType);
    when(assetContentDestroyed.getType()).thenReturn(amAssetContentType);

    when(contentRepository.getContent(eq(ASSET_CONTENT_ID_AVAILABLE + ""))).thenReturn(assetContent);
    when(contentRepository.getContent(eq(ASSET_CONTENT_ID_UNKNOWN + ""))).thenReturn(null);
    when(contentRepository.getContent(eq(ASSET_CONTENT_ID_DELETED + ""))).thenReturn(assetContentDeleted);
    when(contentRepository.getContent(eq(ASSET_CONTENT_ID_DESTROYED + ""))).thenReturn(assetContentDestroyed);
    when(assetContentDeleted.isDeleted()).thenReturn(Boolean.TRUE);
    when(assetContentDestroyed.isDestroyed()).thenReturn(Boolean.TRUE);

    when(rendition.getName()).thenReturn("original");

    renditions = new ArrayList<>();
    renditions.add(rendition);
    when(contentBeanFactory.createBeanFor(assetContent, AMAsset.class)).thenReturn(asset);
    when(asset.getPublishedRenditions()).thenReturn(renditions);
  }

  @AfterClass
  public static void cleanUp(){
    //noinspection ResultOfMethodCallIgnored
    zipFile.delete();
  }

  @Test
  public void testPrepareDownloadingCollectionNullRequest() throws IOException {
    handler.prepareDownloadingCollection(null, response);
    assertNotEquals(HttpServletResponse.SC_OK, response.getStatus());
  }

  @Test
  public void testPrepareDownloadingCollectionEmptyRequest() throws IOException {
    handler.prepareDownloadingCollection("{}", response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
  }

  @Test
  public void testPrepareDownloadingCollectionUnknownAssetId() throws IOException {
    handler.prepareDownloadingCollection("{\"" + ASSET_CONTENT_ID_UNKNOWN + "\":[\"original\"]}", response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
  }

  @Test
  public void testPrepareDownloadingCollectionDeletedAssetId() throws IOException {
    handler.prepareDownloadingCollection("{\"" + ASSET_CONTENT_ID_DELETED + "\":[\"original\"]}", response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
  }

  @Test
  public void testPrepareDownloadingCollectionDestroyedAssetId() throws IOException {
    handler.prepareDownloadingCollection("{\"" + ASSET_CONTENT_ID_DESTROYED + "\":[\"original\"]}", response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
  }

  @Test
  public void testPrepareDownloadingCollectionAvailableAssetId() throws IOException {
    handler.prepareDownloadingCollection("{\"" + ASSET_CONTENT_ID_AVAILABLE + "\":[\"original\"]}", response);
    assertEquals(response.getContentAsString(), HttpServletResponse.SC_OK, response.getStatus());
  }

  @Test
  public void testGetRenditionForName() {
    assertEquals(rendition, DownloadPortalHandler.getRenditionForName(renditions, "original"));
    assertNull(DownloadPortalHandler.getRenditionForName(renditions, "web"));
  }

  @Test
  public void testGetDownloadCollectionMap() {
    assertNull(DownloadPortalHandler.getDownloadCollectionMap(null));
    assertNull(DownloadPortalHandler.getDownloadCollectionMap(""));
    assertNull(DownloadPortalHandler.getDownloadCollectionMap("dgsg{{"));
    assertEquals("{}", DownloadPortalHandler.getDownloadCollectionMap("{}").toString());
    assertEquals("{" + ASSET_CONTENT_ID_UNKNOWN + "=[original]}", DownloadPortalHandler.getDownloadCollectionMap("{\"" + ASSET_CONTENT_ID_UNKNOWN + "\":[\"original\"]}").toString());
  }

  @Test
  public void testGetRenditionsToDownload() {
    Map<String, List<String>> dowloadCollectionMap = new HashMap<>();
    dowloadCollectionMap.put(String.valueOf(ASSET_CONTENT_ID_AVAILABLE), Arrays.asList("original", "web"));

    List<AMAssetRendition> result = handler.getRenditionsToDownload(dowloadCollectionMap);
    assertEquals(1, result.size());
    assertEquals("original", result.get(0).getName());
    assertEquals(renditions, result);
  }

  @Test
  public void testGetRenditionsToDownloadEmpty() {
    Map<String, List<String>> dowloadCollectionMap = new HashMap<>();
    dowloadCollectionMap.put(String.valueOf(ASSET_CONTENT_ID_UNKNOWN), Arrays.asList("original", "web"));

    List<AMAssetRendition> result = handler.getRenditionsToDownload(dowloadCollectionMap);
    assertEquals(0, result.size());
  }

  @Test
  public void testDownloadRenditionCollectionFound() throws IOException {
    File file = File.createTempFile("amDownload", null);
    when(downloadPortalFactory.getPreparedDownload(anyList())).thenReturn(file);
    handler.downloadRenditionCollection("{\"" + ASSET_CONTENT_ID_AVAILABLE + "\":[\"original\"]}", response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
  }

  @Test
  public void testDownloadRenditionCollectionNotFound() throws IOException {
    handler.downloadRenditionCollection("1", response);
    assertNotEquals(HttpServletResponse.SC_OK, response.getStatus());
  }
}
