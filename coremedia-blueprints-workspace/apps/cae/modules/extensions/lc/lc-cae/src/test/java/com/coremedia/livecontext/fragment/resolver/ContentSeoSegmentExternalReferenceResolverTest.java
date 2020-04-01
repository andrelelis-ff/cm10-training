package com.coremedia.livecontext.fragment.resolver;


import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.FragmentParametersFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentSeoSegmentExternalReferenceResolverTest {

  @Mock
  private Site site;

  @Mock
  private Content siteRootFolder;

  @Mock
  private Content linkable;

  @Mock
  private Content navigation;

  @Mock
  private ContentRepository contentRepository;

  @InjectMocks
  private ContentSeoSegmentExternalReferenceResolver testling;

  @Before
  public void beforeEachTest() {
    when(contentRepository.getContent("coremedia:///cap/content/5678")).thenReturn(linkable);
    when(contentRepository.getContent("coremedia:///cap/content/1234")).thenReturn(navigation);
  }

  @Test
  public void testSeoSegmentExternalReferenceNoInfixDelimiter() throws Exception {
    String ref = "cm-seosegment:the-perfect-dinner";
    FragmentParameters params = parametersFor(ref);
    assertThat(testling.include(params)).isFalse();
  }

  @Test
  public void testSeoSegmentExternalReferenceNoIds() throws Exception {
    String ref = "cm-seosegment:the-perfect-dinner--";
    FragmentParameters params = parametersFor(ref);
    assertThat(testling.include(params)).isFalse();
  }

  @Test
  public void testSeoSegmentExternalReferenceTooManyIds() throws Exception {
    String ref = "cm-seosegment:the-perfect-dinner--1234-5678-5678";
    FragmentParameters params = parametersFor(ref);
    assertThat(testling.include(params)).isFalse();
  }

  @Test
  public void testSeoSegmentExternalReferenceBadIds() throws Exception {
    String ref = "cm-seosegment:the-perfect-dinner--123-5678";
    FragmentParameters params = parametersFor(ref);
    assertThat(testling.include(params)).isFalse();

    String ref2 = "cm-seosegment:the-perfect-dinner--1234-567";
    FragmentParameters params2 = parametersFor(ref2);
    assertThat(testling.include(params2)).isFalse();

    String ref3 = "cm-seosegment:the-perfect-dinner--0-0";
    FragmentParameters params3 = parametersFor(ref3);
    assertThat(testling.include(params3)).isFalse();
  }

  @Test
  public void testSeoSegmentExternalReferenceTwoIdsResolver() throws Exception {
    String ref = "cm-seosegment:the-perfect-dinner--1234-5678";
    FragmentParameters params = parametersFor(ref);
    assertThat(testling.include(params)).isTrue();
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertThat(linkableAndNavigation).isNotNull();
    assertThat(linkableAndNavigation.getLinkable()).isEqualTo(linkable);
    assertThat(linkableAndNavigation.getNavigation()).isEqualTo(navigation);
  }

  @Test
  public void testSeoSegmentExternalReferenceOneIdResolver() throws Exception {
    String ref = "cm-seosegment:the-perfect-dinner--5678";
    FragmentParameters params = parametersFor(ref);
    assertThat(testling.include(params)).isTrue();
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertThat(linkableAndNavigation).isNotNull();
    assertThat(linkableAndNavigation.getLinkable()).isEqualTo(linkable);
    assertThat(linkableAndNavigation.getNavigation()).isNull();
  }

  @Test
  public void testSeoSegmentExternalReferenceTwoIdsNoTextResolver() throws Exception {
    String ref = "cm-seosegment:--1234-5678";
    FragmentParameters params = parametersFor(ref);
    assertThat(testling.include(params)).isTrue();
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertThat(linkableAndNavigation).isNotNull();
    assertThat(linkableAndNavigation.getLinkable()).isEqualTo(linkable);
    assertThat(linkableAndNavigation.getNavigation()).isEqualTo(navigation);
  }

  @Test
  public void testSeoSegmentExternalReferenceOneIdNoTextResolver() throws Exception {
    String ref = "cm-seosegment:--5678";
    FragmentParameters params = parametersFor(ref);
    assertThat(testling.include(params)).isTrue();
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertThat(linkableAndNavigation).isNotNull();
    assertThat(linkableAndNavigation.getLinkable()).isEqualTo(linkable);
    assertThat(linkableAndNavigation.getNavigation()).isNull();
  }


  // --- internal ---------------------------------------------------

  private static FragmentParameters parametersFor(String ref) {
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;placement=header;environment=site:site2";
    FragmentParameters params = FragmentParametersFactory.create(url);
    params.setExternalReference(ref);
    return params;
  }
}
