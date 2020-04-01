package com.coremedia.blueprint.feeder.cae.assets;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.feeder.MutableFeedable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link AssetNotSearchableFeedablePopulator}
 */
@RunWith(MockitoJUnitRunner.class)
public class AssetNotSearchableFeedablePopulatorTest {

  private AssetNotSearchableFeedablePopulator populator = new AssetNotSearchableFeedablePopulator();

  @Mock
  private AMAsset asset;

  @Mock
  private AMAssetRendition assetRendition;

  @Mock
  private MutableFeedable feedable;

  @Mock
  private Content assetContent;

  @Test
  public void testPopulate() throws Exception {
    List<AMAssetRendition> renditions = Collections.emptyList();

    when(asset.getPublishedRenditions()).thenReturn(renditions);
    populator.populate(feedable, asset);
    verify(feedable, times(1)).setElement(eq(SearchConstants.FIELDS.NOT_SEARCHABLE.toString()), eq(true));

    renditions = Collections.singletonList(assetRendition);
    when(asset.getPublishedRenditions()).thenReturn(renditions);
    populator.populate(feedable, asset);
    verify(feedable, times(1)).setElement(eq(SearchConstants.FIELDS.NOT_SEARCHABLE.toString()), eq(false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPopulate_passingNullSource_throwsIllegalArgumentException() throws Exception {
    populator.populate(feedable, null);
  }
}
