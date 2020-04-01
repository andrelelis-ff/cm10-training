package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.transform.NamedTransformBeanBlobTransformer;
import com.coremedia.transform.TransformedBeanBlob;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CMMediaImplTest {
  private Content content = mock(Content.class);
  private Blob original = mock(Blob.class);
  private NamedTransformBeanBlobTransformer transformer = mock(NamedTransformBeanBlobTransformer.class);

  private CMMediaImpl media;

  @Before
  public void setUp() throws Exception {
    media = new CMTestMedia(content, original);
    media.setMediaTransformer(transformer);
  }

  @Test
  public void altDefaultsToEmptyString() {
    assertEquals("", media.getAlt());
  }

  @Test
  public void altText() {
    when(content.getString(CMMedia.ALT)).thenReturn("some alt text");
    assertEquals("some alt text", media.getAlt());
  }

  @Test
  public void transformsByNameDelegatesToTransformer() {
    TransformedBeanBlob mockedBlob = mock(TransformedBeanBlob.class);
    String transformationName = "transformationName";
    when(transformer.transform(media, transformationName)).thenReturn(mockedBlob);

    assertSame(mockedBlob, media.getTransformedData(transformationName));
  }

  private static class CMTestMedia extends CMMediaImpl {
    private Content content;
    private Blob data;

    private CMTestMedia(Content content, Blob data) {
      this.content = content;
      this.data = data;
    }

    @Override
    public Content getContent() {
      return content;
    }

    @Override
    public Object getData() {
      return data;
    }
  }
}
