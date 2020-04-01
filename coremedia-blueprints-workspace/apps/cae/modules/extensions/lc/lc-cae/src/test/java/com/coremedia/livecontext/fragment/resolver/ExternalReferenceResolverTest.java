package com.coremedia.livecontext.fragment.resolver;


import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.FragmentParametersFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExternalReferenceResolverTest {

  @Mock
  private Site site;

  @Mock
  private Content siteRootFolder;

  @Mock
  private Content linkable;

  @Mock
  private Content navigation;

  @Mock
  private Content folder;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContextHelper contextHelper;


  @Test
  public void testContentCapIdExternalReferenceResolver() throws Exception {
    String id = "coremedia:///cap/content/1234";
    String ref = ExternalReferenceResolver.CONTENT_ID_FRAGMENT_PREFIX + id;
    when(contentRepository.getContent(id)).thenReturn(linkable);

    ContentCapIdExternalReferenceResolver testling = new ContentCapIdExternalReferenceResolver();
    testling.setContentRepository(contentRepository);
    testling.setContextHelper(contextHelper);

    FragmentParameters params = parametersFor(ref);
    assertTrue(testling.include(params));
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertNotNull(linkableAndNavigation);
    assertNotNull(linkableAndNavigation.getLinkable());
    assertNull(linkableAndNavigation.getNavigation());
  }

  @Test
  public void testContentNumericIdExternalReferenceResolver() throws Exception {
    String ref = ExternalReferenceResolver.CONTENT_ID_FRAGMENT_PREFIX + "1234";
    String capId = "coremedia:///cap/content/1234";
    when(contentRepository.getContent(capId)).thenReturn(linkable);

    ContentNumericIdExternalReferenceResolver testling = new ContentNumericIdExternalReferenceResolver();
    testling.setContentRepository(contentRepository);
    testling.setContextHelper(contextHelper);

    FragmentParameters params = parametersFor(ref);
    assertTrue(testling.include(params));
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertNotNull(linkableAndNavigation);
    assertNotNull(linkableAndNavigation.getLinkable());
    assertNull(linkableAndNavigation.getNavigation());
  }

  @Test
  public void testAbsolutePathExternalReferenceResolver() throws Exception {
    String ref = "cm-path!!action!content";

    when(site.getSiteRootFolder()).thenReturn(siteRootFolder);
    when(contentRepository.getChild("action/content")).thenReturn(linkable);

    ContentPathExternalReferenceResolver testling = new ContentPathExternalReferenceResolver();
    testling.setContentRepository(contentRepository);
    testling.setContextHelper(contextHelper);

    FragmentParameters params = parametersFor(ref);
    assertTrue(testling.include(params));
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertNotNull(linkableAndNavigation);
    assertEquals(linkable, linkableAndNavigation.getLinkable());
    assertNull(linkableAndNavigation.getNavigation());
  }

  @Test
  public void testSiteRelativePathExternalReferenceResolver() throws Exception {
    String ref = "cm-path!action!content";

    when(site.getSiteRootFolder()).thenReturn(siteRootFolder);
    when(siteRootFolder.getChild("action/content")).thenReturn(linkable);

    ContentPathExternalReferenceResolver testling = new ContentPathExternalReferenceResolver();
    testling.setContentRepository(contentRepository);
    testling.setContextHelper(contextHelper);

    FragmentParameters params = parametersFor(ref);
    assertTrue(testling.include(params));
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertNotNull(linkableAndNavigation);
    assertEquals(linkable, linkableAndNavigation.getLinkable());
    assertNull(linkableAndNavigation.getNavigation());
  }

  @Test
  public void testSiteRelativePathToRootFolderExternalReferenceResolver() throws Exception {
    String ref = "cm-path!";

    when(site.getSiteRootFolder()).thenReturn(siteRootFolder);
    when(siteRootFolder.getChild("")).thenReturn(folder);
    when(folder.isFolder()).thenReturn(true);

    ContentPathExternalReferenceResolver testling = new ContentPathExternalReferenceResolver();
    testling.setContentRepository(contentRepository);
    testling.setContextHelper(contextHelper);

    FragmentParameters params = parametersFor(ref);
    assertTrue(testling.include(params));
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertNotNull(linkableAndNavigation);
    assertNull(linkableAndNavigation.getLinkable());
    assertNull(linkableAndNavigation.getNavigation());
  }

  @Test
  public void testRejectPathWithDirectoryUpExternalReferenceResolver() throws Exception {
    String ref = "cm-path!images!..!..!..!you!may!not!pass";

    ContentPathExternalReferenceResolver testling = new ContentPathExternalReferenceResolver();
    testling.setContentRepository(contentRepository);
    testling.setContextHelper(contextHelper);

    FragmentParameters params = parametersFor(ref);
    assertFalse(testling.include(params));
  }

  @Test
  public void testRejectAbsolutePathContainsSitesExternalReferenceResolver() throws Exception {
    String ref = "cm-path!!Sites!shopName!you!may!not!pass";

    ContentPathExternalReferenceResolver testling = new ContentPathExternalReferenceResolver();
    testling.setContentRepository(contentRepository);
    testling.setContextHelper(contextHelper);

    FragmentParameters params = parametersFor(ref);
    assertFalse(testling.include(params));
  }

  @Test
  public void testContentNumericIdWithChannelIdExternalReferenceResolver() throws Exception {
    String ref = ExternalReferenceResolver.CONTENT_ID_FRAGMENT_PREFIX + "1234-5678";

    when(contentRepository.getContent("coremedia:///cap/content/5678")).thenReturn(linkable);
    when(contentRepository.getContent("coremedia:///cap/content/1234")).thenReturn(navigation);

    ContentNumericIdWithChannelIdExternalReferenceResolver testling = new ContentNumericIdWithChannelIdExternalReferenceResolver();
    testling.setContentRepository(contentRepository);
    testling.setContextHelper(contextHelper);

    FragmentParameters params = parametersFor(ref);
    assertTrue(testling.include(params));
    LinkableAndNavigation linkableAndNavigation = testling.resolveExternalRef(params, site);
    assertNotNull(linkableAndNavigation);
    assertNotNull(linkableAndNavigation.getLinkable());
    assertNotNull(linkableAndNavigation.getNavigation());
  }

  private FragmentParameters parametersFor(String ref) {
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;placement=header;environment=site:site2";
    FragmentParameters params = FragmentParametersFactory.create(url);
    params.setExternalReference(ref);
    return params;
  }
}
