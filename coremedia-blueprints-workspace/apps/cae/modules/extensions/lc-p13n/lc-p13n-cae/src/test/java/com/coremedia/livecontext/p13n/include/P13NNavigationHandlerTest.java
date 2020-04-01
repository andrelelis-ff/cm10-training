package com.coremedia.livecontext.p13n.include;

import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class P13NNavigationHandlerTest {

  @InjectMocks
  private P13NNavigationHandler testling;

  @Mock
  private ContextHelper contextHelper;

  @Mock
  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;

  @Mock
  private CMChannel siteRoot;

  @Mock
  private CMChannel channel;

  @Mock
  private CMChannel article;

  @Before
  public void setup(){
    when(navigationSegmentsUriHelper.parsePath(Collections.singletonList("calista"))).thenReturn(siteRoot);
    when(siteRoot.getContext()).thenReturn(siteRoot);
    when(contextHelper.findAndSelectContextFor(siteRoot, channel)).thenReturn(channel);
    when(contextHelper.findAndSelectContextFor(siteRoot, article)).thenReturn(channel);
  }

  @Test
  public void testFindNavigationForChannel() {
    Optional<Navigation> navigation = testling.findNavigation("calista", channel);
    assertThat(navigation.get()).isEqualTo(channel);
  }

  @Test
  public void testFindNavigationForArticle() {
    Optional<Navigation> navigation = testling.findNavigation("calista", article);
    assertThat(navigation.get()).isEqualTo(channel);
  }

  @Test
  public void testFindNavigationWithoutLinkable() {
    Optional<Navigation> navigation = testling.findNavigation("calista", null);
    assertThat(navigation.get()).isEqualTo(siteRoot);
  }

  @Test
  public void testFindNavigationWithoutContext() {
    Optional<Navigation> navigation = testling.findNavigation("", article);
    assertThat(navigation).isEmpty();
  }

}
