package com.coremedia.blueprint.elastic.social.util;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryFileNameHelperTest {
  private RepositoryFileNameHelper repositoryFileNameHelper;

  @Before
  public void setUp() throws Exception {
    repositoryFileNameHelper = new RepositoryFileNameHelper(mock(ContentRepository.class), mockFolder());
  }

  @Test(expected = NullPointerException.class)
  public void constructor_nullArg_01() {
    new RepositoryFileNameHelper(null, mockFolder());
  }

  @Test(expected = NullPointerException.class)
  public void constructor_nullArg_02() {
    new RepositoryFileNameHelper(mock(ContentRepository.class) ,null);
  }

  @Test(expected = NullPointerException.class)
  public void uniqueFileNameFor_nullArg() {
    repositoryFileNameHelper.uniqueFileNameFor(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void uniqueFileNameFor_emptyString() {
    repositoryFileNameHelper.uniqueFileNameFor("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void uniqueFileNameFor_unixDirectory() {
    repositoryFileNameHelper.uniqueFileNameFor("/root/");
  }

  @Test(expected = IllegalArgumentException.class)
  public void uniqueFileNameFor_windowsDirectory() {
    repositoryFileNameHelper.uniqueFileNameFor("\\root\\");
  }

  @Test(expected = IllegalArgumentException.class)
  public void uniqueFileNameFor_nonFolderContent() {
    Content nonFolderContent = mock(Content.class);
    when(nonFolderContent.isFolder()).thenReturn(false);
    repositoryFileNameHelper = new RepositoryFileNameHelper(mock(ContentRepository.class), nonFolderContent);
  }

  @Test
  public void uniqueFileNameFor_oneSimpleCall() {
    String fileName = "foo.jpg";
    String uniqueFileName = repositoryFileNameHelper.uniqueFileNameFor(fileName);

    Assert.assertEquals("Unique file name should be original file name", fileName, uniqueFileName);
  }

  @Test
  public void uniqueFileNameFor_calledWithThreeIdenticalFileNames() {
    String originalFileName = "foo.jpg";
    String firstUniqueFileName = repositoryFileNameHelper.uniqueFileNameFor(originalFileName);
    Assert.assertEquals("First file name should be original file name", originalFileName, firstUniqueFileName);

    String secondUniqueFileName = repositoryFileNameHelper.uniqueFileNameFor(originalFileName);
    String expectedUniqueFileName = originalFileName + "(1)";
    Assert.assertEquals("Second file name should be appended a counter", expectedUniqueFileName, secondUniqueFileName);

    String thirdUniqueFileName = repositoryFileNameHelper.uniqueFileNameFor(originalFileName);
    String thridExpectedUniqueFileName = originalFileName + "(2)";
    Assert.assertEquals("Third file name should be appended a counter", thridExpectedUniqueFileName, thirdUniqueFileName);
  }

  @Test
  public void uniqueFileNameFor_fileExistsInRepository() {
    String fileName = "bar.jpg";

    Content parentFolder = mockFolder();
    when(parentFolder.getPath()).thenReturn("/foo");

    String filePath = parentFolder.getPath() + "/" + fileName;
    ContentRepository contentRepository = mock(ContentRepository.class);
    when(contentRepository.getChild(filePath)).thenReturn(mock(Content.class)); // return value not important

    repositoryFileNameHelper = new RepositoryFileNameHelper(contentRepository, parentFolder);

    String expectedFileName = fileName + "(1)";
    String generatedFileName = repositoryFileNameHelper.uniqueFileNameFor(fileName);
    Assert.assertEquals("File name should be appended a counter since file path is known to the repository.", expectedFileName, generatedFileName);
  }

  @Test
  public void uniqueFileNameFor_fileAndItsAliasesExistInRepository() {
    String fileName = "bar.jpg";

    Content parentFolder = mockFolder();
    when(parentFolder.getPath()).thenReturn("/foo");

    String filePath = parentFolder.getPath() + "/" + fileName;
    String filePathAliasOne = parentFolder.getPath() + "/" + fileName +"(1)";
    String filePathAliasTwo = parentFolder.getPath() + "/" + fileName +"(2)";
    String filePathAliasThree = parentFolder.getPath() + "/" + fileName +"(3)";
    ContentRepository contentRepository = mock(ContentRepository.class);
    when(contentRepository.getChild(filePath)).thenReturn(mock(Content.class));           // return value not important
    when(contentRepository.getChild(filePathAliasOne)).thenReturn(mock(Content.class));   // return value not important
    when(contentRepository.getChild(filePathAliasTwo)).thenReturn(mock(Content.class));   // return value not important
    when(contentRepository.getChild(filePathAliasThree)).thenReturn(mock(Content.class)); // return value not important

    repositoryFileNameHelper = new RepositoryFileNameHelper(contentRepository, parentFolder);

    String expectedFileName = fileName + "(4)";
    String generatedFileName = repositoryFileNameHelper.uniqueFileNameFor(fileName);
    Assert.assertEquals("File name should be appended a counter since file path is known to the repository.", expectedFileName, generatedFileName);
  }

  @Test
  public void uniqueFileNameFor_fileExistsInRepositoryAndIsAddedMultipleTimes() {
    String originalFileName = "bar.jpg";

    Content parentFolder = mockFolder();
    when(parentFolder.getPath()).thenReturn("/foo");

    // make the file existent in the repository
    String filePath = parentFolder.getPath() + "/" + originalFileName;
    ContentRepository contentRepository = mock(ContentRepository.class);
    when(contentRepository.getChild(filePath)).thenReturn(mock(Content.class)); // return value not important

    repositoryFileNameHelper = new RepositoryFileNameHelper(contentRepository, parentFolder);

    String firstExpectedFileName = originalFileName + "(1)";
    String firstGeneratedFileName = repositoryFileNameHelper.uniqueFileNameFor(originalFileName);
    Assert.assertEquals("File name should be appended a counter since file path is known to the repository.", firstExpectedFileName, firstGeneratedFileName);

    String secondExpectedFileName = originalFileName + "(2)";
    String secondGeneratedFileName = repositoryFileNameHelper.uniqueFileNameFor(originalFileName);
    Assert.assertEquals("Second file name should be appended a appropriate counter since file path is known to the repository.", secondExpectedFileName, secondGeneratedFileName);

    String thirdExpectedFileName = originalFileName + "(3)";
    String thirdGeneratedFileName = repositoryFileNameHelper.uniqueFileNameFor(originalFileName);
    Assert.assertEquals("Third file name should be appended a appropriate counter since file path is known to the repository.", thirdExpectedFileName, thirdGeneratedFileName);
  }

  // --- Helper methods ------------------------------------------------------------------------------------------------

  private Content mockFolder() {
    Content folder = mock(Content.class);
    when(folder.isFolder()).thenReturn(true);
    return folder;
  }
}
