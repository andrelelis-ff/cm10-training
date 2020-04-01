package com.coremedia.blueprint.themeimporter;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.authorization.AccessControl;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.themeimporter.ThemeImporterResultImpl;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Some theme importer related pure content utilities.
 */
class ThemeImporterContentHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(ThemeImporterContentHelper.class);
  private CapConnection capConnection;
  private final ThemeImporterResultImpl result;
  private Collection<Content> toBeCheckedIn = new HashSet<>();


  // --- Construct and configure ------------------------------------

  ThemeImporterContentHelper(CapConnection capConnection, ThemeImporterResultImpl result) {
    this.capConnection = capConnection;
    this.result = result;
  }


  // --- Features ---------------------------------------------------

  int id(Content content) {
    return IdHelper.parseContentId(content.getId());
  }

  Content fetchContent(String path) {
    return capConnection.getContentRepository().getChild(path);
  }

  Content ensureContent(String type, String folder, String path) {
    return modifiableContent(type, normalize(folder)+path);
  }

  Content updateContent(String newType, String folder, String path, Map<String, ?> properties) {
    return updateContent(newType, normalize(folder)+path, properties);
  }

  Content updateContent(String newType, String absolutePath, Map<String, ?> properties) {
    try {
      Content content = modifiableContent(newType, absolutePath);
      if (content != null) {
        Map<String, Object> changedProperties = difference(content, properties);
        if (!changedProperties.isEmpty()) {
          checkOut(content);
          content.setProperties(properties);
          result.addUpdate(absolutePath, content);
        }
      } else {
        result.addFailure(absolutePath);
      }
      return content;
    } catch (Exception e) {
      LOGGER.error("Error creating content {} ", absolutePath, e);
      result.addFailure(absolutePath);
      return null;
    }
  }

  Struct propertiesToStruct(String text) {
    StructBuilder structBuilder = capConnection.getStructService().createStructBuilder();
    propertiesToStructBuilder(text, structBuilder);
    return structBuilder.build();
  }

  /**
   * I'll do my very best...
   */
  boolean deleteContent(Content content) {
    String absolutePath = content.getPath();
    AccessControl accessControl = capConnection.getContentRepository().getAccessControl();
    if (!accessControl.mayDelete(content)) {
      LOGGER.warn("Must not delete content {}", absolutePath);
    } else if (content.isCheckedOut() && !accessControl.mayCheckIn(content)) {
      LOGGER.warn("Content {} is checked out by other user, must not delete", absolutePath);
    } else {
      try {
        if (content.isCheckedOut()) {
          content.checkIn();
        }
        toBeCheckedIn.remove(content);
        if (!content.delete().isSuccessful()) {
          LOGGER.warn("Cannot delete content {}, you should clean up manually afterwards.", content);
        } else {
          result.addUpdate(absolutePath, content);
          return true;
        }
      } catch (Exception e) {
        LOGGER.warn("Cannot delete content {}, you should clean up manually afterwards.", content, e);
      }
    }
    result.addFailure(absolutePath);
    return false;
  }

  // *initially*: Do not use after getOrCreateContent, or additionally remove
  // the query result's contents from toBeCheckedIn.
  void initiallyDeleteSubfolder(String targetFolderPath, String affectedTheme) {
    ContentRepository contentRepository = capConnection.getContentRepository();
    Content targetFolder = contentRepository.getChild(targetFolderPath + '/' + affectedTheme);
    if (targetFolder != null) {
      QueryService queryService = contentRepository.getQueryService();
      queryService.poseContentQuery("isCheckedOut AND BELOW ?0", targetFolder).forEach(Content::checkIn);
      targetFolder.delete();
    }
  }

  void checkInAll() {
    toBeCheckedIn.forEach(Content::checkIn);
  }

  void revertAll() {
    toBeCheckedIn.forEach(this::revert);
  }


  // --- internal ---------------------------------------------------

  private Map<String, Object> difference(Content content, Map<String, ?> newProperties) {
    Map<String, Object> changedProperties = new HashMap<>();
    for (Map.Entry<String, ?> entry : newProperties.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (!equivalent(content.get(key), value)) {
        changedProperties.put(key, value);
      }
    }
    return changedProperties;
  }

  /**
   * equivalent is slightly more tolerant than equals, e.g. "" vs. null.
   */
  private boolean equivalent(Object oldValue, Object newValue) {
    if (Objects.equals(newValue, oldValue)) {
      return true;
    }
    if (isEmptyString(oldValue) && isEmptyString(newValue)) {
      return true;
    }
    return false;
  }

  private boolean isEmptyString(Object o) {
    return o==null || o instanceof String && ((String)o).isEmpty();
  }

  @VisibleForTesting
  void propertiesToStructBuilder(String text, StructBuilder structBuilder) {
    try {
      Properties props = new Properties();
      props.load(new ByteArrayInputStream(text.getBytes(StandardCharsets.ISO_8859_1)));
      props.forEach((key, value) -> structBuilder.declareString(key.toString(), Integer.MAX_VALUE, value.toString().trim()));
    } catch (IOException e) {
      String snippet = text.length()<30 ? text : text.substring(0, 30)+"...";
      throw new IllegalArgumentException("Cannot parse properties \"" + snippet + "\"", e);
    }
  }

  /**
   * Returns the requested modifiable content, or null if this
   * is not possible for whatever reason.
   */
  private Content modifiableContent(String contentType, String absolutePath) {
    ContentRepository repository = capConnection.getContentRepository();
    Content content = repository.getChild(absolutePath);
    if (content != null) {
      if (!content.getType().getName().equals(contentType)) {
        //Set to null, because the type is different
        LOGGER.warn("Cannot update document {} since it is of type {} even though it should be of type {}", absolutePath, content.getType().getName(), contentType);
        result.addFailure(absolutePath);
        content = null;
      } else if (content.isCheckedOut() && !content.isCheckedOutByCurrentSession()) {
        // Maybe the document would need no update anyway, so we could return
        // it for now and handle this case in checkOut. But it means that we
        // do not control this document, state and effects are unpredictable,
        // even if we do not touch it, so better warn early.
        LOGGER.warn("Cannot update document {} since it has been checkout out by somebody else.", absolutePath);
        result.addFailure(absolutePath);
        content = null;
      }
    } else {
      ContentType type = repository.getContentType(contentType);
      if (type != null) {
        content = type.create(repository.getRoot(), absolutePath);
        toBeCheckedIn.add(content);
      } else {
        LOGGER.warn("Cannot create document {} since there is no content type {}", absolutePath, contentType);
        result.addFailure(absolutePath);
      }
    }
    return content;
  }

  private void checkOut(Content content) {
    // checkedOut by other user has already been handled in modifiableContent,
    // so we can keep it simple here.
    if (!content.isCheckedOut()) {
      content.checkOut();
      toBeCheckedIn.add(content);
    }
  }

  private void revert(Content content) {
    if (content.getCheckedOutVersion() !=null) {
      content.revert();
    } else {
      // Cannot revert checked out version 1.
      content.checkIn();
      content.delete();
    }
  }

  private static String normalize(@NonNull String folder) {
    return folder.endsWith("/") ? folder : folder + "/";
  }

  @VisibleForTesting
  class KeyValue {
    String key;
    String value;

    KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
