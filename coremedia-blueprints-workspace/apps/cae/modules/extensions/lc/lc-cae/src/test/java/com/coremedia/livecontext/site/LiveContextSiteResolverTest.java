package com.coremedia.livecontext.site;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.FragmentParametersFactory;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolverImpl;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LiveContextSiteResolverTest {

  private static final Locale LOCALE = Locale.forLanguageTag("en-US");

  private static final String SITE_NAME_1 = "site1";
  private static final String SITE_NAME_2 = "site2";

  @Mock
  private SitesService sitesService;

  @Mock
  private Site site1;

  @Mock
  private Site site2;

  private LiveContextSiteResolverImpl testling = new LiveContextSiteResolverImpl();

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Before
  public void setup() {

    testling.setSitesService(sitesService);
    testling.setCommerceConnectionInitializer(commerceConnectionInitializer);

    when(site2.getLocale()).thenReturn(LOCALE);
    when(site1.getName()).thenReturn(SITE_NAME_1);
    when(site2.getName()).thenReturn(SITE_NAME_2);

  }

  @Test
  public void handleFragmentSiteResolvingWithEnvironment() {
    when(sitesService.getSites()).thenReturn(ImmutableSet.of(site1, site2));

    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;placement=header;environment=site:site2";
    FragmentParameters params = FragmentParametersFactory.create(url);
    Optional<Site> siteFor = testling.findSiteFor(params);
    assertThat(siteFor).contains(site2);
  }
}
