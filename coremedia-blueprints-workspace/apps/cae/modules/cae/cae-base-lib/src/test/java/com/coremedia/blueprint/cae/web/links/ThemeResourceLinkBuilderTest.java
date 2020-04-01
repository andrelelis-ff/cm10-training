package com.coremedia.blueprint.cae.web.links;

import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMImage;
import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.blueprint.common.services.context.CurrentContextService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.web.links.LinkFormatter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThemeResourceLinkBuilderTest {

  @InjectMocks
  @Spy
  private ThemeResourceLinkBuilder linkBuilder;

  @Mock
  private CurrentContextService currentContextService;

  @Mock
  private LinkFormatter linkFormatter;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private DataViewFactory dataViewFactory;

  private MockHttpServletRequest request;
  private HttpServletResponse response;

  private static final String CONTEXT_PATH = "/blueprint";

  @Before
  public void setUp() {
    request = new MockHttpServletRequest();
    request.setContextPath(CONTEXT_PATH);

    response = new MockHttpServletResponse();
  }

  @Test
  public void getThemeResourceAt_resourceExists_alsoLoadDataView() {
    CMImage imageBean = mock(CMImage.class, "Image Bean");
    CMImage imageDV = mock(CMImage.class, "Image Dataview");
    Content imageContent = mock(Content.class);

    when(contentRepository.getChild("/test")).thenReturn(imageContent);
    when(contentBeanFactory.createBeanFor(imageContent, ContentBean.class)).thenReturn(imageBean);
    when(dataViewFactory.loadCached(imageBean, null)).thenReturn(imageDV);

    ContentBean result = linkBuilder.getThemeResourceAt("/test");

    assertEquals(imageDV, result);
  }

  @Test
  public void getThemeResourceAt_resourceDoesNotExist_returnNull() {
    when(contentRepository.getChild("/test")).thenReturn(null);
    ContentBean result = linkBuilder.getThemeResourceAt("/test");
    assertNull(result);
  }

  @Test
  public void getThemeFolder_themeIsDeleted_returnsNull() {
    CMTheme theme = mock(CMTheme.class);
    Content themeDocument = mock(Content.class, "themeContent");

    when(theme.getContent()).thenReturn(themeDocument);
    when(themeDocument.getParent()).thenReturn(null); // meaning is either deleted or root folder (see Javadoc)

    Content result = linkBuilder.getThemeFolder(theme);

    assertNull(result);
  }

  @Test
  public void getThemeFolder_themeFolderExistsAndIsValid_returnsFolder() {
    CMTheme theme = mock(CMTheme.class);
    Content themeDocument = mock(Content.class, "themeContent");
    Content themeFolder = mock(Content.class, "themeFolder");

    when(theme.getContent()).thenReturn(themeDocument);
    when(themeDocument.getParent()).thenReturn(themeFolder);
    when(themeDocument.getName()).thenReturn("Corporate Theme");
    when(themeFolder.getName()).thenReturn("corporate");

    Content result = linkBuilder.getThemeFolder(theme);

    assertEquals(themeFolder, result);
  }

  @Test
  public void getThemeFromCurrentContext_EitherContextOrThemeUnavailable_returnsNull () {
    // no context
    when(currentContextService.getContext()).thenReturn(null);
    CMTheme result = linkBuilder.getThemeFromCurrentContext(request);
    assertNull(result);

    reset(currentContextService, linkBuilder);

    // no theme for context
    CMContext context = mock(CMContext.class);
    when(currentContextService.getContext()).thenReturn(context);
    result = linkBuilder.getThemeFromCurrentContext(request);
    assertNull(result);
    verify(linkBuilder, times(1)).getDeveloperUser(request);

  }

  @Test(expected = IllegalArgumentException.class)
  public void getLinkToThemeResource_pathTriesToBreakoutOfTheme_throwsException() throws Exception {
    linkBuilder.getLinkToThemeResource("/img/../../someForbiddenResource", request, response);
  }

  @Test
  public void getLinkToThemeResource_usingLocalResource_notCallingLinkFormatter() {
    CMTheme theme = mock(CMTheme.class);
    Content themeFolder = mock(Content.class, "themeFolder");

    linkBuilder.setUseLocalResources(true);
    doReturn(theme).when(linkBuilder).getThemeFromCurrentContext(request);
    doReturn(themeFolder).when(linkBuilder).getThemeFolder(theme);
    when(themeFolder.getName()).thenReturn("corporate");

    String result = linkBuilder.getLinkToThemeResource("/img/picture.jpg", request, response);
    assertEquals(linkBuilder.getLocalResourcePath(request, "corporate", "/img/picture.jpg"), result);

    verifyZeroInteractions(linkFormatter);
  }

  @Test
  public void getLinkToThemeResource_pathToNonExistingResource_returnsEmptyLing() {
    CMTheme theme = mock(CMTheme.class);
    Content themeFolder = mock(Content.class, "themeFolder");

    linkBuilder.setUseLocalResources(false);
    doReturn(theme).when(linkBuilder).getThemeFromCurrentContext(request);
    doReturn(themeFolder).when(linkBuilder).getThemeFolder(theme);
    doReturn(null).when(linkBuilder).getThemeResourceAt("/Themes/corporate/ing/picture.jpg");
    when(themeFolder.getPath()).thenReturn("/Themes/corporate");

    String result = linkBuilder.getLinkToThemeResource("ing/picture.jpg", request, response);

    assertEquals(StringUtils.EMPTY, result);
    verifyZeroInteractions(linkFormatter);
  }

  @Test
  public void getLinkToThemeResource_pathToValidThemeResource_passesCorrectPathToLinkFormatter() {
    CMTheme theme = mock(CMTheme.class);
    Content themeFolder = mock(Content.class, "themeFolder");
    CMImage image = mock(CMImage.class);

    linkBuilder.setUseLocalResources(false);
    doReturn(theme).when(linkBuilder).getThemeFromCurrentContext(request);
    doReturn(themeFolder).when(linkBuilder).getThemeFolder(theme);
    doReturn(image).when(linkBuilder).getThemeResourceAt("/Themes/corporate/img/picture.jpg");
    when(themeFolder.getPath()).thenReturn("/Themes/corporate");

    linkBuilder.getLinkToThemeResource("img/picture.jpg", request, response);

    verify(linkFormatter, times(1)).formatLink(image, null,request,response,false);
  }



}
