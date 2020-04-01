package com.coremedia.blueprint.elastic.social.cae.tenant;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.blueprint.base.elastic.tenant.TenantSiteMapping;
import com.coremedia.cap.multisite.Site;
import com.coremedia.elastic.core.api.tenant.TenantService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TenantSiteLookupStrategyTest {

  @InjectMocks
  private TenantSiteLookupStrategy strategy = new TenantSiteLookupStrategy();

  @Mock
  private HttpServletRequest servletRequest;

  @Mock
  private TenantSiteMapping tenantSiteMappingHelper;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ElasticSocialPlugin elasticSocialPlugin;

  @Mock
  private TenantService tenantService;

  @Mock
  private Site navigation;

  @Test
  public void getTenant() {
    when(servletRequest.getAttribute(SiteHelper.SITE_KEY)).thenReturn(navigation);
    when(elasticSocialPlugin.getElasticSocialConfiguration(navigation).getTenant()).thenReturn("test");

    assertEquals("test", strategy.getTenant(servletRequest));
  }

  @Test
  public void noTenantAvailable() {
    assertEquals(null, strategy.getTenant(servletRequest));
  }
}
