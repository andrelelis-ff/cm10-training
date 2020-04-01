package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.TempFileService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.mimetype.MimeTypeService;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.activation.MimeType;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DownloadCollectionZipCacheKeyTest {

  private static final String BLOB_ID_NUMBER = "2982";
  private static final String BLOB_ID = "coremedia:///cap/resources/" + BLOB_ID_NUMBER + "/data";
  private static final String JPG = "jpg";
  private static final String ASSET_CONTENT_NAME = "assetContentName";
  private static final String ORIGINAL_RENDITION = "original";

  private DownloadCollectionZipCacheKey cacheKey;

  private Cache cache;

  @Mock
  private CapConnection capConnection;

  @Mock
  private TempFileService tempFileService;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentRepository contentRepositoryAnother;

  @Mock
  private MimeTypeService mimeTypeService;

  @Mock
  private AMAsset asset;

  @Mock
  private AMAsset assetInvalid;

  @Mock
  private Content assetContent;

  @Mock
  private Content assetContentInvalidID;

  @Mock
  private AMAssetRendition amAssetRendition;

  @Mock
  private AMAssetRendition amAssetRenditionNoBlob;

  @Mock
  private AMAssetRendition amAssetRenditionInvalidId;

  @Mock
  private CapBlobRef renditionBlob;

  private static File zipFile;

  private List<AMAssetRendition> renditions;

  @BeforeClass
  public static void prepare() throws IOException {
    zipFile = File.createTempFile("temp-file-name", ".zip");
  }

  @Before
  public void setUp() throws Exception {
    cache = new Cache("DCZCK");
    cache.setCapacity(Object.class.toString(), 10);

    when(mimeTypeService.getExtensionForMimeType(anyString())).thenReturn(JPG);
    when(contentRepository.getConnection()).thenReturn(capConnection);
    when(contentRepositoryAnother.getConnection()).thenReturn(capConnection);
    when(capConnection.getTempFileService()).thenReturn(tempFileService);
    when(tempFileService.createTempFileFor(anyString(), eq("zip"))).thenReturn(zipFile);

    when(renditionBlob.getContentType()).thenReturn(new MimeType("image/jpeg"));
    when(renditionBlob.getInputStream()).thenReturn(IOUtils.toInputStream("some test data for my input stream"));

    when(asset.getContent()).thenReturn(assetContent);
    when(assetInvalid.getContent()).thenReturn(assetContentInvalidID);

    when(amAssetRendition.getBlob()).thenReturn(renditionBlob);
    when(amAssetRendition.getAsset()).thenReturn(asset);
    when(amAssetRendition.getName()).thenReturn(ORIGINAL_RENDITION);

    when(amAssetRenditionNoBlob.getBlob()).thenReturn(null);
    when(amAssetRenditionNoBlob.getAsset()).thenReturn(asset);
    when(amAssetRenditionNoBlob.getName()).thenReturn(ORIGINAL_RENDITION);

    when(assetContent.getId()).thenReturn(BLOB_ID);
    when(assetContent.getName()).thenReturn(ASSET_CONTENT_NAME);

    when(assetContentInvalidID.getId()).thenReturn("MyID:1");
    when(amAssetRenditionInvalidId.getAsset()).thenReturn(assetInvalid);
    when(amAssetRenditionInvalidId.getBlob()).thenReturn(renditionBlob);

    renditions = newArrayList(amAssetRendition);

    cacheKey = new DownloadCollectionZipCacheKey(renditions, contentRepository, mimeTypeService);
  }

  @AfterClass
  public static void cleanUp() {
    //noinspection ResultOfMethodCallIgnored
    zipFile.delete();
  }

  @Test
  public void testEquals() throws Exception {
    DownloadCollectionZipCacheKey cacheKeySame = newDownloadCollectionZipCacheKeyWithRenditions(renditions);

    assertThat(cacheKey).isEqualTo(cacheKeySame);
  }

  @Test
  public void testEqualsFail() throws Exception {
    List<AMAssetRendition> renditionsOther = newArrayList(amAssetRendition, amAssetRenditionInvalidId);
    DownloadCollectionZipCacheKey cacheKeyOther = newDownloadCollectionZipCacheKeyWithRenditions(renditionsOther);

    assertThat(cacheKey).isNotEqualTo(cacheKeyOther);
    assertThat(cacheKeyOther).isNotEqualTo(cacheKey);
    assertThat(cacheKey).isNotEqualTo("Test"); // Deliberately compare against an object of another type.
  }

  @Test
  public void testNotEquals() throws Exception {
    List<AMAssetRendition> renditionsOther = newArrayList();
    DownloadCollectionZipCacheKey cacheKeyOther = new DownloadCollectionZipCacheKey(renditionsOther,
            contentRepositoryAnother, mimeTypeService);

    assertThat(cacheKey).isNotEqualTo(cacheKeyOther);
  }

  @Test
  public void testHashCode() throws Exception {
    DownloadCollectionZipCacheKey cacheKeyOne = newDownloadCollectionZipCacheKeyWithContentRepository(contentRepository);
    assertThat(cacheKeyOne.hashCode() != 0).isTrue();

    DownloadCollectionZipCacheKey cacheKeyTwo = newDownloadCollectionZipCacheKeyWithContentRepository(contentRepositoryAnother);
    assertThat(cacheKeyOne.hashCode()).isNotEqualTo(cacheKeyTwo.hashCode());
  }

  @Test
  public void testEvaluate() throws Exception {
    File evaluatedZipFile = cacheKey.evaluate(cache);

    assertThat(evaluatedZipFile).isEqualTo(zipFile);
  }

  @Test
  public void testCreateDownloadCollectionZip() throws Exception {
    File evaluatedZipFile = cacheKey.evaluate(cache);

    List<String> unzippedFilenames = unZipIt(evaluatedZipFile);
    assertThat(unzippedFilenames).hasSize(1);
    assertThat(unzippedFilenames.get(0))
            .isEqualTo(ASSET_CONTENT_NAME + "_" + ORIGINAL_RENDITION + "_" + BLOB_ID_NUMBER + "." + JPG);
  }

  @Test
  public void testCreateDownloadCollectionZipBlobNull() throws Exception {
    List<AMAssetRendition> renditions = newArrayList(amAssetRenditionNoBlob);
    DownloadCollectionZipCacheKey cacheKey = newDownloadCollectionZipCacheKeyWithRenditions(renditions);

    File evaluatedZipFile = cacheKey.evaluate(cache);

    List<String> unzippedFilenames = unZipIt(evaluatedZipFile);
    assertThat(unzippedFilenames).isEmpty();
  }

  @Test
  public void testCreateDownloadCollectionZipInvalidId() throws Exception {
    List<AMAssetRendition> renditions = newArrayList(amAssetRenditionInvalidId);
    DownloadCollectionZipCacheKey cacheKey = newDownloadCollectionZipCacheKeyWithRenditions(renditions);

    File evaluatedZipFile = cacheKey.evaluate(cache);

    List<String> unzippedFilenames = unZipIt(evaluatedZipFile);
    assertThat(unzippedFilenames).isEmpty();
  }

  @Test
  public void testWeight() throws Exception {
    File evaluatedZipFile = cacheKey.evaluate(cache);
    int weight = cacheKey.weight(null, evaluatedZipFile, 0);

    assertThat(weight).isEqualTo((int) evaluatedZipFile.length());
  }

  @Test
  public void testCacheClass() throws Exception {
    File evaluatedZipFile = cacheKey.evaluate(cache);
    String cacheClass = cacheKey.cacheClass(cache, evaluatedZipFile);

    assertThat(cacheClass).isEqualTo(DownloadCollectionZipCacheKey.CACHE_CLASS_DISK);
  }

  private DownloadCollectionZipCacheKey newDownloadCollectionZipCacheKeyWithRenditions(
          @NonNull List<AMAssetRendition> renditions) {
    return new DownloadCollectionZipCacheKey(renditions, contentRepository, mimeTypeService);
  }

  private DownloadCollectionZipCacheKey newDownloadCollectionZipCacheKeyWithContentRepository(
          @NonNull ContentRepository contentRepository) {
    return new DownloadCollectionZipCacheKey(renditions, contentRepository, mimeTypeService);
  }

  /**
   * Unzip it
   *
   * @param zipFile input zip file
   */
  private List<String> unZipIt(File zipFile) {
    List<String> filenames = newArrayList();

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
      // Get the zipped file list entries.
      ZipEntry ze = zis.getNextEntry();
      while (ze != null) {
        filenames.add(ze.getName());
        ze = zis.getNextEntry();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return filenames;
  }
}
