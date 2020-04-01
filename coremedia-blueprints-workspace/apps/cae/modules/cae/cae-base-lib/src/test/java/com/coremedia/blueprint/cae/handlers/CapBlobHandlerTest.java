package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.user.UserRepository;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.LinkFormatter;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Map;

import static com.coremedia.blueprint.cae.web.links.NavigationLinkSupport.ATTR_NAME_CMNAVIGATION;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * Test for {@link CapBlobHandler}
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = CapBlobHandlerTestConfiguration.class)
@DirtiesContext(classMode = AFTER_CLASS)
@TestPropertySource(properties = "cae.single-node=true")
public class CapBlobHandlerTest {

  private static final String URI_JPG = "/resource/blob/16/ad343795d54b19c1b2e3a5b44513cee1/nae-me-jpg-data.jpg";
  private static final String URI_ANY = "/resource/blob/16/ad343795d54b19c1b2e3a5b44513cee1/nae-me-jpg-data.any";
  private static final String URI_RAW = "/resource/blob/16/ad343795d54b19c1b2e3a5b44513cee1/nae-me-jpg-data.raw";
  private static final String URI_PNG = "/resource/blob/16/ad343795d54b19c1b2e3a5b44513cee1/nae-me-jpg-data.png";

  private static final String FILENAME = "a-file%20?a&b n.a;m=e.xyz";

  @Inject
  private MockMvc mockMvc;
  @Inject
  private LinkFormatter linkFormatter;
  @Inject
  private ContentTestHelper contentTestHelper;
  @Inject
  private UserRepository userRepository;

  @After
  public void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  /**
   * Tests link generation
   */
  @Test
  public void testLink() {
    String link = formatLink(contentTestHelper.getContent(16).getBlobRef("data"));
    assertThat(link).isEqualTo(URI_JPG);
  }

  /**
   * Tests link generation for CMDownload
   */
  @Test
  public void testLinkCMDownload() {
    String link = formatLink(contentTestHelper.getContentBean(28));
    assertThat(link).isEqualTo("/resource/blob/28/0e0839ed3b4062cd4e0b8c3966137751/a-file-20-a-b-n-a-m-e-xyz-data.pdf");
  }

  /**
   * Tests link generation for CMDownload
   */
  @Test
  public void testLinkCMDownloadWithFilename() {
    String link = formatLink(contentTestHelper.getContentBean(24));
    assertThat(link).isEqualTo("/resource/blob/24/0e0839ed3b4062cd4e0b8c3966137751/a-file%2520%3Fa%26b%20n.a%3Bm%3De.xyz");
  }

  /**
   * Tests link generation for CMDownload w/o blob
   */
  @Test
  public void testLinkCMDownloadNoBlob() {
    String link = formatLink(contentTestHelper.getContentBean(26));
    assertThat(link).isEqualTo("#");
  }

  /**
   * Tests link for null ETag.
   */
  @Test
  public void testLinkWithNullETag() {
    CapBlobRef capBlobRef = Mockito.spy(contentTestHelper.getContent(16).getBlobRef("data"));
    when(capBlobRef.getETag()).thenReturn(null);

    String expectedUrl = URI_JPG.replace("ad343795d54b19c1b2e3a5b44513cee1", "-");
    assertThat(formatLink(capBlobRef)).isEqualTo(expectedUrl);
  }

  @Test
  public void testLinkWithDeveloperVariant() {
    Content cmImage = contentTestHelper.getContent(22);
    String link = formatLink(cmImage.getBlobRef("data"));
    assertThat(link).isEqualTo("/resource/crblob/22/5fffa3bba49dab6df7fb54ab6c89104c/name-data.jpg");
  }

  /**
   * Test bean resolution and pattern matching
   */
  @Test
  public void testHandleBlobUrl() throws Exception {
    CapBlobRef capBlobRef = contentTestHelper.getContent("16").getBlobRef("data");

    assertModel(handleRequest(URI_JPG), capBlobRef);

    assertThat(handleRequest(URI_ANY))
            .extracting(HandlerHelper::getRootModel)
            .isInstanceOf(HttpError.class);

    assertModel(handleRequest(URI_RAW), capBlobRef);
  }

  @Test
  public void testHandleFilenameUrl() throws Exception {
    MockHttpServletRequestBuilder req = MockMvcRequestBuilders
            .get("/resource/blob/24/e14c00e5e3d413ca1097137ad0bf44c0/{filename}", FILENAME)
            .requestAttr(ATTR_NAME_CMNAVIGATION, contentTestHelper.getContentBean(4))
            .characterEncoding("UTF-8");

    assertModel(handleRequest(req), contentTestHelper.getContent(24).getBlobRef("data"));
  }

  @Test
  public void testHandleDeveloperVariantBlobUrlFallthrough() throws Exception {
    CapBlobRef capBlobRef = contentTestHelper.getContent(16).getBlobRef("data");
    assertModel(handleRequest(URI_JPG.replaceFirst("/blob/", "/crblob/")), capBlobRef);

    assertThat(handleRequest(URI_ANY))
            .extracting(HandlerHelper::getRootModel)
            .isInstanceOf(HttpError.class);

    assertModel(handleRequest(URI_RAW), capBlobRef);
  }

  @Test
  public void testHandleDeveloperVariantBlobUrl() throws Exception {
    String uriJpgDeveloperVariant = URI_JPG.replaceFirst("/blob/", "/crblob/");
    CapBlobRef davesBlobRef = contentTestHelper.getContent(316).getBlobRef("data");

    MockHttpServletRequestBuilder davesRequest = MockMvcRequestBuilders.get(new URI(uriJpgDeveloperVariant))
            .requestAttr(UserVariantHelper.class.getName() + ".user", userRepository.getUserByName("dave"));
    assertModel(handleRequest(davesRequest), davesBlobRef);

    assertModel(handleRequest(URI_JPG), contentTestHelper.getContent(16).getBlobRef("data"));
  }

  /**
   * Test bean resolution and pattern matching for a URL including Japanese characters.
   */
  @Test
  public void testHandleBlobUrlWithJapaneseCharacters() throws Exception {
    // Java literals use UTF-16 code points (requiring 2 bytes per character), whereas in the URL,
    // the segment will be encoded in UTF-8, requiring three bytes per character.
    // The UTF-8, URL encoded segment equivalent to these four characters, is "%E8%A9%A6%E9%A8%93%E7%94%BB%E5%83%8F".
    String japaneseName = "\u8A66\u9A13\u753B\u50CF.jpg";
    String url = "/resource/blob/20/ad343795d54b19c1b2e3a5b44513cee1/" + japaneseName + "-jpg-data.jpg";
    assertModel(handleRequest(url), contentTestHelper.getContent(20).getBlobRef("data"));
  }

  /**
   * Test accepting "-" for a null ETag.
   */
  @Test
  public void testHandleBlobUrlWithNullETag() throws Exception {
    String requestUrl = URI_JPG.replace("ad343795d54b19c1b2e3a5b44513cee1", "-");
    assertModel(handleRequest(requestUrl), contentTestHelper.getContent(16).getBlobRef("data"));
  }

  /**
   * Return a "not found" object, if bean is null.
   */
  @Test
  public void testNotFoundIfBeanIsNull() throws Exception {
    assertNotFound(handleRequest("/resource/blob/14/42/14-does-not-exist-data.jpg"));
  }

  /**
   * Return a "not found" object, if wrong extension.
   */
  @Test
  public void testNotFoundIfWrongExtension() throws Exception {
    assertNotFound(handleRequest(URI_PNG));
  }

  @Test
  public void testRedirectIfWrongETag() throws Exception {
    ModelAndView mav = handleRequest("/resource/blob/16/42/nae-me-jpg-data.jpg");
    assertModel(mav, contentTestHelper.getContent(16).getBlobRef("data"));
    assertThat(mav.getViewName()).isEqualTo("redirect:DEFAULT");
  }

  @Test
  public void testNotFoundForInvalidProperty() throws Exception {
    // invalid property name
    assertNotFound(handleRequest("/resource/blob/16/ad343795d54b19c1b2e3a5b44513cee1/nae-me-jpg-invalid.jpg"));
  }

  @Test
  public void testNotFoundForNullBlobRef() throws Exception {
    // accessing non-blob property
    assertNotFound(handleRequest("/resource/blob/18/ad343795d54b19c1b2e3a5b44513cee1/pic18-data.any"));
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

  private void assertNotFound(ModelAndView modelAndView) {
    assertThat(modelAndView)
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

  private String formatLink(Object bean) {
    MockHttpServletRequest request = newRequest(emptyMap());
    return linkFormatter.formatLink(bean, null, request, new MockHttpServletResponse(), false);
  }

  private MockHttpServletRequest newRequest(Map<String, String> parameters) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
    request.setParameters(parameters);
    request.setCharacterEncoding("UTF-8");
    return request;
  }

}
