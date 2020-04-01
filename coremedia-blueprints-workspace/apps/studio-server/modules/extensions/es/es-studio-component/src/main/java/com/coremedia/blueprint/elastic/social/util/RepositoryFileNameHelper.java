package com.coremedia.blueprint.elastic.social.util;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public final class RepositoryFileNameHelper {
  private final ContentRepository contentRepository;
  private final Content parentFolder;
  private final Map<String, Integer> knownOriginalFileNames;
  private StringBuilder fileNameBuilder;
  private String originalFileName;

  /**
   * @param contentRepository the {@link ContentRepository} to operate on
   * @param parentFolder the folder in the repository that is checked for existing file names
   */
  public RepositoryFileNameHelper(final ContentRepository contentRepository, final Content parentFolder) {
    Preconditions.checkNotNull(contentRepository, "'contentRepository' may not be null");
    Preconditions.checkNotNull(parentFolder, "'parentFolder' may not be null");
    Preconditions.checkArgument(parentFolder.isFolder(), "'parentFolder' is not a folder.");

    this.contentRepository = contentRepository;
    this.parentFolder = parentFolder;
    knownOriginalFileNames = new HashMap<>();
  }

  /**
   * <p>Creates a unique file name in the provided parent folder for a desired file name. If the file name exists, a counter is appended in brackets.</p>
   * <p>For example, if this method is called the second time with the file name "foo.jpg" in the folder "/root" on the same instance, "/root/foo.jpg(1)" would be returned.</p>
   *
   * @param potentialFileName the desired file name in the {@link ContentRepository}
   * @return the unique file name
   */
  public String uniqueFileNameFor(final String potentialFileName) {
    Preconditions.checkNotNull(potentialFileName, "'potentialFileName' may not be null.");
    Preconditions.checkArgument(!potentialFileName.isEmpty(), "'potentialFileName' is empty. An empty String is not a valid file path.");
    Preconditions.checkArgument(isFilePath(potentialFileName), "'%s' must be a file path, but is a directory.", potentialFileName);

    originalFileName = stripAppendedCounter(potentialFileName);
    fileNameBuilder = new StringBuilder().append(originalFileName);

    addFileNameAliasesFromRepository();
    appendCounterIfNecessary();
    addOriginalFileName();
    return fileNameBuilder.toString();
  }

  private String stripAppendedCounter(final String potentialFilePath) {
    return potentialFilePath.replaceAll("\\(\\d\\)$", "");
  }

  private void addFileNameAliasesFromRepository() {
    if (fileNameExistsInRepository()) {
      int fileNameCounter = extractHighestCounter();
      knownOriginalFileNames.put(originalFileName, fileNameCounter);
    }
  }

  private boolean fileNameExistsInRepository() {
    String desiredFilePath = fullFilePath();
    return contentRepository.getChild(desiredFilePath) != null;
  }

  private String fullFilePath() {
    return parentFolder.getPath() + "/" + originalFileName;
  }

  private int extractHighestCounter() {
    Integer timesAdded = knownOriginalFileNames.get(originalFileName);
    int counter = (timesAdded == null) ? 1 : timesAdded ;
    while ( null != contentRepository.getChild(fullFilePath() + "(" + counter + ")" ) ) {
      ++counter;
    }
    return counter;
  }

  private void addOriginalFileName() {
    Integer timesAdded = knownOriginalFileNames.get(originalFileName);
    int newVal = (timesAdded == null) ? 1 : ++timesAdded;
    knownOriginalFileNames.put(originalFileName, newVal);
  }

  private boolean isFilePath(String potentialFilePath) {
    char lastChar = potentialFilePath.charAt(potentialFilePath.length() - 1);
    return !(lastChar == '/') && !(lastChar == '\\');
  }

  private void appendCounterIfNecessary() {
    if (fileAliasExistsInRepository()) {
      String timesFileNameHasBeenAdded = String.valueOf(knownOriginalFileNames.get(originalFileName));

      fileNameBuilder.append("(");
      fileNameBuilder.append(timesFileNameHasBeenAdded);
      fileNameBuilder.append(")");
    }
  }

  private boolean fileAliasExistsInRepository() {
    return knownOriginalFileNames.containsKey(originalFileName);
  }
}
