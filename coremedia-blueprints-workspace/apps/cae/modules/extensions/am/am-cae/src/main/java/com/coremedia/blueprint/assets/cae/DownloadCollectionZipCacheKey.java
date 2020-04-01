package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.common.TempFileService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.mimetype.MimeTypeService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.MoreObjects.firstNonNull;

final class DownloadCollectionZipCacheKey extends CacheKey<File> {

  private static final Logger LOG = LoggerFactory.getLogger(DownloadCollectionZipCacheKey.class);

  private static final String TEMP_FILE_PREFIX = "amDownloadPortalCollectionZip";
  private static final String ZIP_FILE_EXTENSION = "zip";

  public static final int DOWNLOAD_COLLECTION_DATA_EXPIRATION_MINUTES = 10;

  static final String CACHE_CLASS_DISK = "com.coremedia.cap.disk";
  private static final String DEFAULT_EXTENSION = "raw";

  private List<AMAssetRendition> renditions;
  private ContentRepository contentRepository;
  private TempFileService tempFileService;
  private MimeTypeService mimeTypeService;

  public DownloadCollectionZipCacheKey(@NonNull List<AMAssetRendition> renditions,
                                       @NonNull ContentRepository contentRepository,
                                       @NonNull MimeTypeService mimeTypeService) {
    this.renditions = renditions;
    this.contentRepository = contentRepository;
    tempFileService = contentRepository.getConnection().getTempFileService();
    this.mimeTypeService = mimeTypeService;
  }

  @Override
  public boolean equals(Object o) { //NOSONAR - ignore method complexity
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DownloadCollectionZipCacheKey that = (DownloadCollectionZipCacheKey) o;
    return contentRepository.equals(that.contentRepository) && renditions.equals(that.renditions);
  }

  @Override
  public int hashCode() {
    return renditions.hashCode() + contentRepository.hashCode();
  }

  @Override
  public int weight(Object key, File value, int numDependents) {
    return (int) value.length();
  }

  @Override
  public String cacheClass(Cache cache, File value) {
    return CACHE_CLASS_DISK;
  }

  @Override
  public File evaluate(Cache cache) throws Exception {
    String name = TEMP_FILE_PREFIX + hashCode();

    File f = tempFileService.createTempFileFor(name, ZIP_FILE_EXTENSION);
    boolean ok = false;

    try (FileOutputStream fos = new FileOutputStream(f)) {
      Cache.disableDependencies();
      createDownloadCollectionZip(fos);

      long fileSize = f.length();
      if (fileSize > Integer.MAX_VALUE) {
        throw new IOException(String.format("Writing ZIP result size is too large: %d (maximum size is %d)",
                fileSize, Integer.MAX_VALUE));
      }
      ok = true;
      return f;
    } finally {
      if (!ok) {
        tempFileService.release(f);
      }

      Cache.enableDependencies();
      Cache.cacheFor(DOWNLOAD_COLLECTION_DATA_EXPIRATION_MINUTES, TimeUnit.MINUTES);
    }
  }

  private void createDownloadCollectionZip(@NonNull OutputStream outputStream) throws IOException {

    try (
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)
    ) {
      for (AMAssetRendition rendition : renditions) {
        addBlobToZipFile(zipOutputStream, rendition);
      }
      zipOutputStream.finish();
      zipOutputStream.flush();
    } catch (Exception e) {
      LOG.error("An exception occurred while zipping asset download collection", e);
    }
  }

  private void addBlobToZipFile(@NonNull ZipOutputStream zipOutputStream,
                                @NonNull AMAssetRendition rendition) throws IOException {
    Blob blob = rendition.getBlob();
    Content assetContent = rendition.getAsset().getContent();
    if (blob == null) {
      LOG.warn("No blob for AMAssetRendition with name {} for asset with id {}, it will be skipped in the zip file",
              rendition.getName(), assetContent.getId());
      return;
    }
    try (InputStream blobInputStream = blob.getInputStream()) {
      String extension = getExtension(blob.getContentType().toString(), DEFAULT_EXTENSION);
      int id = IdHelper.parseContentId(assetContent.getId());
      String zipEntryName = String.format("%s_%s_%s.%s", assetContent.getName(), rendition.getName(), id, extension);
      zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
      IOUtils.copy(blobInputStream, zipOutputStream);
    } catch (Exception e) {
      LOG.error("An exception occurred while adding zip entry for rendition {} of asset {} to download collection zip",
              rendition.getName(), assetContent.getId(), e);
    } finally {
      zipOutputStream.closeEntry();
    }
  }

  private String getExtension(String contentType, String fallback) {
    String extension = mimeTypeService.getExtensionForMimeType(contentType);
    return firstNonNull(extension, fallback);
  }
}
