package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMDownload;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.objectserver.beans.ContentBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CMDownloadImplTest extends ContentBeanTestBase {

  private CMDownload download;

  @Before
  public void setUp() throws Exception {
    download = getContentBean(88);
  }

  @Test
  public void testGetAspectByName() throws Exception {
    Assert.assertEquals(0, download.getAspectByName().size());
  }

  @Test
  public void testGetAspects() throws Exception {
    Assert.assertEquals(0, download.getAspects().size());
  }

  @Test
  public void testGetLocalizations() throws Exception {
    Assert.assertEquals(1, download.getLocalizations().size());
  }

  @Test
  public void testGetVariantsByLocale() throws Exception {
    Assert.assertEquals(1, download.getVariantsByLocale().size());
  }

  @Test
  public void testGetMaster() throws Exception {
    Assert.assertNull(download.getMaster());
  }

  @Test
  public void testGetData() throws Exception {
    Assert.assertNotNull(download.getData());
  }

  @Test
  public void testFilenameWithoutContext() throws Exception {
    Assert.assertEquals("a-filename", download.getFilename());
  }

  @Test
  public void testFilenameWithLocalSettingTrue() throws Exception {
    ContentBean channel = getContentBean(10);
    setRequestAttribute(channel, NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, ServletRequestAttributes.SCOPE_REQUEST);

    Struct localSettings = getContentRepository().getConnection().getStructService().createStructBuilder()
            .set(CMDownloadImpl.SETTING_USE_CM_DOWNLOAD_FILENAME, true)
            .build();

    Content content = download.getContent();

    content.checkOut();
    content.set("localSettings", localSettings);
    content.checkIn();

    Assert.assertEquals("a-filename", download.getFilename());
  }

  @Test
  public void testFilenameWithInheritedSetting() throws Exception {
    ContentBean channel = getContentBean(10);
    setRequestAttribute(channel, NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, ServletRequestAttributes.SCOPE_REQUEST);

    Content channelContent = channel.getContent();
    Struct newLocalSettings = channelContent.getStruct("localSettings").builder()
            .set(CMDownloadImpl.SETTING_USE_CM_DOWNLOAD_FILENAME, true)
            .build();

    channelContent.checkOut();
    channelContent.set("localSettings", newLocalSettings);
    channelContent.checkIn();

    Assert.assertEquals("a-filename", download.getFilename());
  }
}
