package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMCollection;
import com.coremedia.blueprint.common.feeds.FeedFormat;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE;

public class CMCollectionImplTest extends ContentBeanTestBase {

  private CMCollection collection;

  @Before
  public void setUp() throws Exception {
    collection = getContentBean(60);
  }

  @Test
  public void testGetFeedFormat() throws Exception {
    Assert.assertEquals(FeedFormat.Rss_2_0, collection.getFeedFormat());
  }

  @Test
  public void testGetFeedTitle() throws Exception {
    Assert.assertEquals("teaserTitle", collection.getFeedTitle());
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, collection.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, collection.getAspects().size());
  }

  @Test
  public void testGetLocalizations() throws Exception {
    Assert.assertEquals(1, collection.getLocalizations().size());
  }

  @Test
  public void testGetVariantsByLocale() throws Exception {
    Assert.assertEquals(1, collection.getVariantsByLocale().size());
  }

  @Test
  public void testGetMaster() throws Exception {
    Assert.assertNull(collection.getMaster());
  }

  @Test
  public void testItems() throws Exception {
    setUpPreviewDate(REQUEST_ATTRIBUTE_PREVIEW_DATE);
    Assert.assertEquals(4, collection.getItems().size());
  }
}
