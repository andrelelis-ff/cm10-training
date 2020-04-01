package com.coremedia.blueprint.elastic.social.cae.util;


import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.elastic.core.cms.ContentWithSite;
import com.coremedia.objectserver.beans.ContentBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentBeanTransformerTest {
  @InjectMocks
  private ContentBeanTransformer contentBeanTransformer = new ContentBeanTransformer();

  @Mock
  private ContentBean contentBean;

  @Mock
  private Content content;

  @Mock
  private SitesService sitesService;

  @Mock
  private Site site;

  @Mock
  private ContentSiteAspect contentSiteAspect;

  @Test
  public void transform() {
    when(contentBean.getContent()).thenReturn(content);
    when(sitesService.getContentSiteAspect(content)).thenReturn(contentSiteAspect);
    when(contentSiteAspect.getSite()).thenReturn(site);
    Object transformed = contentBeanTransformer.transform(contentBean);
    assertNotNull(transformed);
    assertEquals(content, ((ContentWithSite) transformed).getContent());
    assertEquals(site, ((ContentWithSite)transformed).getSite());
  }

  @Test
  public void getSite() {
    when(contentBean.getContent()).thenReturn(content);
    when(sitesService.getContentSiteAspect(content)).thenReturn(contentSiteAspect);
    when(contentSiteAspect.getSite()).thenReturn(site);
    Site siteFromContent = contentBeanTransformer.getSite(contentBean);

    assertNotNull(siteFromContent);
    assertEquals(site, siteFromContent);
  }

  @Test
  public void getType() {
    assertEquals(ContentBean.class, contentBeanTransformer.getType());
  }
}
