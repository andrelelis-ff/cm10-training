package com.coremedia.blueprint.assets.contentbeans;


import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.cap.content.Content;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AMAssetRenditionImplTest {

  @Mock
  Content assetContent;

  @Mock
  AMAsset asset;

  @Before
  public void setup() {
    when(asset.getContent()).thenReturn(assetContent);
  }

  @Test
  public void assetRendition() throws MimeTypeParseException {
    String name = "testName";
    String type = "testtype";
    int size = 5;
    AMAsset asset = mock(AMAsset.class);
    Content assetContent = mock(Content.class);
    CapBlobRef blob = mock(CapBlobRef.class);
    when(blob.getSize()).thenReturn(size);
    MimeType mimeType = new MimeType("application", type);
    when(blob.getContentType()).thenReturn(mimeType);

    when(asset.getContent()).thenReturn(assetContent);
    when(assetContent.getBlobRef(name)).thenReturn(blob);

    AMAssetRendition rendition = new AMAssetRenditionImpl(name, asset);
    assertEquals(name, rendition.getName());
    assertEquals(blob, rendition.getBlob());
    assertEquals(mimeType.toString(), rendition.getMimeType());
    assertEquals(size, rendition.getSize());
    assertEquals(asset, rendition.getAsset());
  }

  @Test
  public void assetRendition_assumesIdentity_createsEqualCopy() {
    String renditionName = "print";

    AMAssetRenditionImpl assetRendition = new AMAssetRenditionImpl(renditionName, asset);
    AMAssetRenditionImpl assetRenditionCopy = new AMAssetRenditionImpl();

    assetRenditionCopy.assumeIdentity(assetRendition);

    assertEquals("The copy should equal the original", assetRendition, assetRenditionCopy);
    assertEquals("The copy should have the same hash as the original", assetRendition.hashCode(), assetRenditionCopy.hashCode());
  }

  @Test
  public void assetRendition_notEquals() {
    String renditionName = "print";

    AMAssetRenditionImpl assetRendition = new AMAssetRenditionImpl(renditionName, asset);

    assertFalse("The object should not equal the original", assetRendition.equals(new Object()));
  }

  @Test
  public void renditionWithoutBlob() {
    AMAssetRenditionImpl assetRendition = new AMAssetRenditionImpl("test", asset);
    assertNull(assetRendition.getBlob());
    assertNull(assetRendition.getMimeType());
  }
}
