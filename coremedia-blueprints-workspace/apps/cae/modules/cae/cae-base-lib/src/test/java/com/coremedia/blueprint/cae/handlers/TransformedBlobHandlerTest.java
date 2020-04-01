package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.transform.NamedTransformBeanBlobTransformer;
import com.coremedia.transform.TransformedBeanBlob;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import edu.umd.cs.findbugs.annotations.NonNull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.cae.web.links.NavigationLinkSupport.ATTR_NAME_CMNAVIGATION;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link TransformedBlobHandler}
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TransformedBlobHandlerTestConfiguration.class)
public class TransformedBlobHandlerTest {

  private TransformedBeanBlob transformedBlob;
  private Blob transformedBlobObject;

  @MockBean
  private TransformImageService transformImageService;
  @MockBean
  private NamedTransformBeanBlobTransformer namedTransformBeanBlobTransformer;

  @Inject
  private MockMvc mockMvc;
  @Inject
  private LinkFormatter linkFormatter;
  @Inject
  private ContentTestHelper contentTestHelper;

  //----
  private static final String DIGEST = "digest";
  private static final String TRANSFORM_NAME = "transformName";
  private static final String WIDTH = "100";
  private static final String HEIGHT = "100";

  @Before
  public void setUp() {
    transformedBlob = mock(TransformedBeanBlob.class);
    transformedBlobObject = mock(Blob.class);
    when(transformedBlobObject.getETag()).thenReturn(DIGEST);
    when(transformedBlob.getETag()).thenReturn(DIGEST);
    when(transformedBlob.getTransformName()).thenReturn(TRANSFORM_NAME);

    when(transformImageService.transformWithDimensions(any(Content.class), anyString(), anyString(), anyInt(), anyInt())).thenReturn(Optional.of(transformedBlobObject));
  }

  /**
   * Test bean resolution and pattern matching:
   * {@link TransformedBlobHandler#handleRequest}
   */
  @Test
  public void testBean() throws Exception {
    when(transformedBlob.getBean()).thenReturn(contentTestHelper.getContentBean(16));
    when(transformedBlob.getOriginal()).thenReturn(contentTestHelper.getContent(16).getBlobRef("data"));

    assertModel(handleRequest("/resource/image/16/transformName/100/100/digest/tw/nae-me-jpg.jpg"), transformedBlobObject);
  }

  @Test
  public void acceptUrlWithOriginalExtensionInsteadOfTransformedExtension() throws Exception {
    // in this test case, the original JPEG is transformed to a PNG
    when(transformedBlob.getContentType()).thenReturn(new MimeType("image/png"));
    when(transformedBlob.getBean()).thenReturn(contentTestHelper.getContentBean(16));
    when(transformedBlob.getOriginal()).thenReturn(contentTestHelper.getContent(16).getBlobRef("data"));

    assertModel(handleRequest("/resource/image/16/transformName/100/100/digest/tw/nae-me-jpg.jpg"), transformedBlobObject);
  }

  /**
   * Test bean resolution with a Japanese segment name (BARBUDA-2637).
   * {@link TransformedBlobHandler#handleRequest}
   */
  @Test
  public void testJapaneseSegmentName() throws Exception {
    // Java literals use UTF-16 code points (requiring 2 bytes per character), whereas in the URL,
    // the segment will be encoded in UTF-8, requiring three bytes per character.
    // The UTF-8, URL encoded segment equivalent to these four characters, is "%E8%A9%A6%E9%A8%93%E7%94%BB%E5%83%8F".
    String japaneseName = "\u8A66\u9A13\u753B\u50CF-jpg";
    String url = "/resource/image/20/transformName/100/100/digest/dw/" + japaneseName + ".jpg";

    when(transformedBlob.getBean()).thenReturn(contentTestHelper.getContentBean(20));
    when(transformedBlob.getOriginal()).thenReturn(contentTestHelper.getContent(20).getBlobRef("data"));
    String link = formatLink(transformedBlob, null, false, ImmutableMap.of(
            TransformedBlobHandler.WIDTH_SEGMENT, WIDTH,
            TransformedBlobHandler.HEIGHT_SEGMENT, HEIGHT
    ));

    ModelAndView modelAndView = handleRequest(url);
    assertModel(modelAndView, transformedBlobObject);
  }

  /**
   * Test "not found" when one of the hash-protected URL segments is modified:
   * {@link TransformedBlobHandler#handleRequest}
   */
  @Test
  public void testMessingWithProtectedURLParts() throws Exception {
    when(transformedBlob.getBean()).thenReturn(contentTestHelper.getContentBean(16));
    when(transformedBlob.getOriginal()).thenReturn(contentTestHelper.getContent(16).getBlobRef("data"));
    assertNotFound("extension", handleRequest("/resource/image/16/transformName/100/100/digest/tw/nae-me-jpg.png"));
    assertNotFound("name", handleRequest("/resource/image/16/transformName/100/100/digest/tw/nae-me-jpg-broken.jpg"));
    assertNotFound("digest", handleRequest("/resource/image/16/transformName/100/100/digest/xxxxx/nae-me-jpg.jpg"));
    assertNotFound("width", handleRequest("/resource/image/16/transformName/101/100/digest/tw/nae-me-jpg.jpg"));
    assertNotFound("height", handleRequest("/resource/image/16/transformName/100/101/digest/tw/nae-me-jpg.jpg"));
    assertNotFound("transform", handleRequest("/resource/image/16/invalid/100/100/digest/tw/nae-me-jpg.jpg"));
    assertNotFound("id", handleRequest("/resource/image/18/transformName/100/100/digest/tw/nae-me-jpg.jpg"));
    assertNotFound("hash", handleRequest("/resource/image/16/transformName/100/100/digest/XXX/nae-me-jpg.jpg"));

    verify(transformImageService, never()).transformWithDimensions(any(Content.class), anyString(), anyString(), anyInt(), anyInt());
  }

  /**
   * Test URL generation:
   * {@link TransformedBlobHandler#buildLink(com.coremedia.transform.TransformedBeanBlob, java.util.Map)}
   */
  @Test
  public void testGenerateLink() {
    when(transformedBlob.getBean()).thenReturn(contentTestHelper.getContentBean(16));
    when(transformedBlob.getOriginal()).thenReturn(contentTestHelper.getContent(16).getBlobRef("data"));

    String link = formatLink(transformedBlob, null, false, ImmutableMap.of(
            TransformedBlobHandler.WIDTH_SEGMENT, WIDTH,
            TransformedBlobHandler.HEIGHT_SEGMENT, HEIGHT
    ));
    assertThat(link).isEqualTo("/resource/image/16/transformName/100/100/digest/tw/nae-me-jpg.jpg");
  }

  /**
   * BARBUDA-2590: to fix this bug, the link generator will use the original extension instead of the
   * correct extension of the transformed blob, because generating the link should not actually require the tranformation
   * to be executed.
   */
  @Test
  public void useOriginalExtensionWhenTransformationChangesExtension() throws MimeTypeParseException {
    // in this test case, the original JPEG is transformed to a PNG
    when(transformedBlob.getContentType()).thenReturn(new MimeType("image/png"));
    when(transformedBlob.getBean()).thenReturn(contentTestHelper.getContentBean(16));
    when(transformedBlob.getOriginal()).thenReturn(contentTestHelper.getContent(16).getBlobRef("data"));

    // expect extension .jpg, even though the transformed blob has a different extension
    assertThat(formatLink(transformedBlob, null, false, ImmutableMap.of(
            TransformedBlobHandler.WIDTH_SEGMENT, WIDTH,
            TransformedBlobHandler.HEIGHT_SEGMENT, HEIGHT
    ))).isEqualTo("/resource/image/16/transformName/100/100/digest/tw/nae-me-jpg.jpg");
  }

  /**
   * BARBUDA-2590: generating a link for a transformed blob must not access its size, contents, or content type,
   * as this would trigger the costly transformation to be performed. Link generation must be inexpensive.
   */
  @Test
  public void linkGenerationDoesNotTriggerTransformation() throws IOException {
    testGenerateLink();

    // make sure the "expensive" methods triggering the transformation are never called
    verify(transformedBlob, never()).asBytes();
    verify(transformedBlob, never()).getInputStream();
    verify(transformedBlob, never()).writeOn(any(OutputStream.class));
    verify(transformedBlob, never()).getContentType();
    verify(transformedBlob, never()).getSize();
  }

  // --- internal ---------------------------------------------------

  private ModelAndView handleRequest(String path) throws Exception {
    MockHttpServletRequestBuilder req = MockMvcRequestBuilders
            .get(path)
            .requestAttr(ATTR_NAME_CMNAVIGATION, contentTestHelper.getContentBean(4))
            .characterEncoding("UTF-8");
    return handleRequest(req);
  }

  private ModelAndView handleRequest(MockHttpServletRequestBuilder req) throws Exception {
    return mockMvc.perform(req).andReturn().getModelAndView();
  }

  private void assertNotFound(@NonNull String message, ModelAndView modelAndView) {
    assertThat(modelAndView)
            .as(message)
            .extracting(HandlerHelper::getRootModel)
            .isInstanceOfSatisfying(HttpError.class,
                    e -> assertThat(e)
                            .returns(HttpServletResponse.SC_NOT_FOUND, from(HttpError::getErrorCode)));
  }

  private void assertModel(ModelAndView modelAndView, Object bean) {
    assertThat(modelAndView)
            .extracting(HandlerHelper::getRootModel)
            .isEqualTo(bean);
  }

  protected String formatLink(Object bean, String viewName, boolean forRedirect, Map<String, Object> parameters) {
    MockHttpServletRequest request = newRequest(emptyMap());
    request.setAttribute(ViewUtils.PARAMETERS, newHashMap(parameters));
    return linkFormatter.formatLink(bean, viewName, request, new MockHttpServletResponse(), forRedirect);
  }

  private MockHttpServletRequest newRequest(Map<String, String> parameters) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
    request.setParameters(parameters);
    request.setCharacterEncoding("UTF-8");
    return request;
  }
}
