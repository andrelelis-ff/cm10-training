package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class DownloadCollectionOverviewTest {

  @Test
  public void testRenditions() {
    ImmutableList<AMAssetRendition> assetRenditions = ImmutableList.of(mock(AMAssetRendition.class));
    DownloadCollectionOverview assetCollection = new DownloadCollectionOverview(assetRenditions);
    assertEquals(assetRenditions, assetCollection.getRenditions());
  }

  @Test
  public void testSearchTerm() {
    DownloadCollectionOverview assetCollection = new DownloadCollectionOverview(Collections.<AMAssetRendition>emptyList());
    assertEquals("", assetCollection.getSearchTerm());
  }
}
