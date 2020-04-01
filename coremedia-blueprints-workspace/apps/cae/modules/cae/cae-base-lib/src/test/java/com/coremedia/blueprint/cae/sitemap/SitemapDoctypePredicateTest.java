package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.InvalidIdException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SitemapDoctypePredicateTest {
  @Test(expected = IllegalStateException.class)
  public void invalidIncludesProvided() throws Exception {
    testling.setIncludes(Collections.singletonList(INVALID_DOC_TYPE));
    testling.afterPropertiesSet();
  }

  @Test(expected = IllegalStateException.class)
  public void invalidExcludesProvided() throws Exception {
    testling.setExcludes(Collections.singletonList(INVALID_DOC_TYPE));
    testling.afterPropertiesSet();
  }

  @Test
  public void validExcludesAndIncludes() throws Exception {
    testling.afterPropertiesSet();

    verify(capConnection, times(6)).getContentRepository();
    verify(contentRepository).getContentType(BUSINESS_END);
    verify(contentRepository).getContentType(BILLION_YEAR_BUNKER);
  }

  @Test
  public void notAContentObject() {
    assertFalse(testling.include("no content object"));
  }

  @Test
  public void neitherIncludedNorExcluded() {
    when(content.getType()).thenReturn(thinkingCap);

    assertFalse("Neither included nor excluded must lead to false.", testling.include(content));
  }

  @Test
  public void includedAndNotExcluded() {
    when(content.getType()).thenReturn(billionYearBunker);

    assertTrue("Included and not excluded must be true", testling.include(content));
  }

  @Test
  public void includedAndExcluded() {
    when(content.getType()).thenReturn(pointOfViewGun);

    assertFalse("Included and excluded must be false.", testling.include(content));
  }

  @Test
  public void notIncludedButExcluded() {
    when(content.getType()).thenReturn(crisisInducer);

    assertFalse("Not included but excluded must be false.", testling.include(content));
  }

  @Before
  public void defaultSetup() {
    testling = new SitemapDoctypePredicate();
    testling.setCapConnection(capConnection);
    testling.setIncludes(ImmutableList.of(BILLION_YEAR_BUNKER, BUSINESS_END, POINT_OF_VIEW_GUN));
    testling.setExcludes(ImmutableList.of(KILL_O_ZAP, CRISIS_INDUCER, POINT_OF_VIEW_GUN));

    when(capConnection.getContentRepository()).thenReturn(contentRepository);
    when(contentRepository.getContentType(INVALID_DOC_TYPE)).thenThrow(InvalidIdException.class);

    when(thinkingCap.getName()).thenReturn(THINKING_CAP);
    when(thinkingCap.isSubtypeOf(THINKING_CAP)).thenReturn(true);
    when(billionYearBunker.getName()).thenReturn(BILLION_YEAR_BUNKER);
    when(billionYearBunker.isSubtypeOf(BILLION_YEAR_BUNKER)).thenReturn(true);
    when(pointOfViewGun.getName()).thenReturn(POINT_OF_VIEW_GUN);
    when(pointOfViewGun.isSubtypeOf(POINT_OF_VIEW_GUN)).thenReturn(true);
    when(crisisInducer.getName()).thenReturn(CRISIS_INDUCER);
    when(crisisInducer.isSubtypeOf(CRISIS_INDUCER)).thenReturn(true);
  }

  private SitemapDoctypePredicate testling;

  @Mock
  private CapConnection capConnection;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentType thinkingCap;

  @Mock
  private ContentType billionYearBunker;

  @Mock
  private ContentType pointOfViewGun;

  @Mock
  private ContentType crisisInducer;

  @Mock
  private Content content;

  private static final String INVALID_DOC_TYPE = "by-products-of-designer-people";
  private static final String BILLION_YEAR_BUNKER = "billion year bunker";
  private static final String BUSINESS_END = "business end";
  private static final String KILL_O_ZAP = "Kill-o-Zap blaster pistol";
  private static final String CRISIS_INDUCER = "crisis inducer";
  private static final String THINKING_CAP = "thinking cap";
  private static final String POINT_OF_VIEW_GUN = "point of view gun";
}
