package com.coremedia.blueprint.studio.rest.intercept;

import com.coremedia.blueprint.studio.rest.intercept.word.DocumentEntry;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.BlobService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.ContentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.activation.MimeType;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WordUploadInterceptorTest {
  private InputStream inputStream;

  @Mock
  private ContentRepository repository;

  @Mock
  private CapConnection connection;

  @Mock
  private BlobService blobService;

  @Mock
  private Blob imageBlob;

  @Before
  public void setUp() throws Exception {
    inputStream = WordUploadInterceptorTest.class.getResourceAsStream("upload-test.docx");

    when(repository.getConnection()).thenReturn(connection);
    when(connection.getBlobService()).thenReturn(blobService);
    when(blobService.fromBytes(any(byte[].class), any(String.class))).thenReturn(imageBlob);
  }

  @Test
  public void test() throws Exception {
    WordUploadInterceptor testling = new WordUploadInterceptor(repository);

    List<DocumentEntry> result = testling.extract(inputStream, new MimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), "test");

    assertEquals(result.size(), 3);
    assertEquals(result.get(0).getKey(), "detailText");
    assertNotNull(result.get(0).getValue());
    assertEquals(result.get(1).getValue(), "test");
    assertEquals(result.get(2).getName(), "image1.png");
  }

}
