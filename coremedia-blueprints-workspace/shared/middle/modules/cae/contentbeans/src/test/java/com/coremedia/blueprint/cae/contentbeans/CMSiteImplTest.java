package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMSite;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

import static com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE;


public class CMSiteImplTest extends ContentBeanTestBase {

  private CMSite contentBean;

  @Before
  public void setUp() throws Exception {
    contentBean = getContentBean(54);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, contentBean.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, contentBean.getAspects().size());
  }

  @Test
  public void testGetRoot1() throws Exception {
    setUpPreviewDate(REQUEST_ATTRIBUTE_PREVIEW_DATE);
    Assert.assertEquals(10, contentBean.getRoot().getContentId());
  }

  @Test
  public void testGetRoot2() throws Exception {
    setUpPreviewDate(REQUEST_ATTRIBUTE_PREVIEW_DATE, 2010, Calendar.JANUARY, 1);
    Assert.assertNull(contentBean.getRoot());
  }

  @Test
  public void testGetId() throws Exception {
    Assert.assertEquals("theSiteId", contentBean.getId());
  }

  @Test
  public void testGetLocale() throws Exception {
    Assert.assertEquals(Locale.ENGLISH, contentBean.getLocale());
  }
}
