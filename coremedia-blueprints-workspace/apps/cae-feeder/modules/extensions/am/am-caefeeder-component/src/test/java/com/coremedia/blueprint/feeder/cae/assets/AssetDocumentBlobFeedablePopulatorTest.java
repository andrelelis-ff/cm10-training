package com.coremedia.blueprint.feeder.cae.assets;


import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.assets.contentbeans.AMDocumentAsset;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.cap.feeder.MutableFeedable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AssetDocumentBlobFeedablePopulatorTest {

  @Test
  public void populateWithDownload() {
    AssetDocumentBlobFeedablePopulator populator = new AssetDocumentBlobFeedablePopulator();
    MutableFeedable mutableFeedable = mock(MutableFeedable.class);
    AMDocumentAsset documentAsset = mock(AMDocumentAsset.class);

    AMAssetRendition originalRendition = mock(AMAssetRendition.class);
    when(originalRendition.getName()).thenReturn(AMDocumentAsset.ORIGINAL);
    CapBlobRef originalRenditionBlob = mock(CapBlobRef.class);
    when(originalRendition.getBlob()).thenReturn(originalRenditionBlob);

    AMAssetRendition downloadRendition = mock(AMAssetRendition.class);
    when(downloadRendition.getName()).thenReturn(AMDocumentAsset.DOWNLOAD);
    CapBlobRef downloadRenditionBlob = mock(CapBlobRef.class);
    when(downloadRendition.getBlob()).thenReturn(downloadRenditionBlob);

    List<AMAssetRendition> renditions = new ArrayList<>();
    renditions.add(originalRendition);
    renditions.add(downloadRendition);
    when(documentAsset.getPublishedRenditions()).thenReturn(renditions);

    populator.populate(mutableFeedable, documentAsset);

    verify(documentAsset).getPublishedRenditions();
    verify(mutableFeedable).setElement(null, downloadRenditionBlob);
    verify(mutableFeedable, never()).setElement(null, originalRenditionBlob);
  }

  @Test
  public void populateWithOriginal() {
    AssetDocumentBlobFeedablePopulator populator = new AssetDocumentBlobFeedablePopulator();
    MutableFeedable mutableFeedable = mock(MutableFeedable.class);
    AMDocumentAsset documentAsset = mock(AMDocumentAsset.class);

    AMAssetRendition originalRendition = mock(AMAssetRendition.class);
    when(originalRendition.getName()).thenReturn(AMDocumentAsset.ORIGINAL);
    CapBlobRef originalRenditionBlob = mock(CapBlobRef.class);
    when(originalRendition.getBlob()).thenReturn(originalRenditionBlob);

    List<AMAssetRendition> renditions = new ArrayList<>();
    renditions.add(originalRendition);
    when(documentAsset.getPublishedRenditions()).thenReturn(renditions);

    populator.populate(mutableFeedable, documentAsset);

    verify(documentAsset).getPublishedRenditions();
    verify(mutableFeedable).setElement(null, originalRenditionBlob);
  }

  @Test(expected = IllegalArgumentException.class)
  public void populateWithNullObjects() {
    AssetDocumentBlobFeedablePopulator populator = new AssetDocumentBlobFeedablePopulator();
    populator.populate(null, null);
  }
}
