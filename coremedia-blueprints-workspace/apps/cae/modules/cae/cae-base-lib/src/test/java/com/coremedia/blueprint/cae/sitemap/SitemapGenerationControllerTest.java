package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.blueprint.cae.common.predicates.ValidContentPredicate;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.common.util.Predicate;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.objectserver.web.links.LinkScheme;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import static com.coremedia.blueprint.cae.sitemap.SitemapGenerationControllerTest.LocalConfig.PROFILE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CACHE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CONTENT_BEAN_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.DATA_VIEW_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.ID_PROVIDER;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.LINK_FORMATTER;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = SitemapGenerationControllerTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public class SitemapGenerationControllerTest {
  @Configuration
  @ImportResource(
          value = {
                  CONTENT_BEAN_FACTORY,
                  DATA_VIEW_FACTORY,
                  ID_PROVIDER,
                  LINK_FORMATTER,
                  CACHE,
                  "classpath:/framework/spring/blueprint-contentbeans.xml",
                  "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
                  "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-cae-services.xml",
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import(XmlRepoConfiguration.class)
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "SitemapGenerationControllerTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/blueprint/cae/controller/testurls/testcontent.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }
  }

  private ContentUrlGenerator urlGenerator;
  private SitemapGenerationController testling;

  @Inject
  private MockHttpServletRequest request;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private MockHttpServletResponse response;

  @Inject
  private ContentBeanFactory contentBeanFactory;

  @Inject
  private SiteResolver siteResolver;

  private ValidationService validationServiceAlwaysTrue = new ValidationService() {
    @Override
    public List filterList(List source) {
      return source;
    }

    @Override
    public boolean validate(Object source) {
      return true;
    }
  };
  @Before
  public void setUp() throws Exception {
    LinkFormatter linkFormatter = new LinkFormatter();
    linkFormatter.setSchemes(singletonList(new GeneralPurposeLinkScheme()));

    ValidContentPredicate validContentPredicate = new ValidContentPredicate();
    validContentPredicate.setContentBeanFactory(contentBeanFactory);
    List<Predicate<Content>> predicates = new ArrayList<>();
    predicates.add(validContentPredicate);

    urlGenerator = new ContentUrlGenerator();
    urlGenerator.setContentBeanFactory(contentBeanFactory);
    urlGenerator.setExclusionPaths(new ArrayList<String>());
    urlGenerator.setLinkFormatter(linkFormatter);
    urlGenerator.setPredicates(predicates);
    urlGenerator.setValidationService(validationServiceAlwaysTrue);

    SitemapSetup sitemapSetup = new SitemapSetup();
    sitemapSetup.setSitemapRendererFactory(new PlainSitemapRendererFactory());
    sitemapSetup.setUrlGenerators(singletonList(urlGenerator));
    SpringBasedSitemapSetupFactory setupFactory = new SpringBasedSitemapSetupFactory();
    setupFactory.setSitemapSetup(sitemapSetup);

    testling = new SitemapGenerationController();
    testling.setSiteResolver(siteResolver);
    testling.setSitemapSetupFactory(setupFactory);

    request.setPathInfo("/internal/theSiteSegment/sitemap-org");
  }

  @Test
  public void testNoParams() throws Exception {
    request.addParameter(SitemapRequestParams.PARAM_EXCLUDE_FOLDERS, (String) null);

    testling.handleRequestInternal(request, response);

    List<String> urlList = convertToList();
    assertNotNull(urlList);
    assertEquals(7, urlList.size());
  }

  @Test
  public void testGzipParam() throws Exception {
    request.addParameter(SitemapRequestParams.PARAM_EXCLUDE_FOLDERS, (String) null);
    request.addParameter(SitemapRequestParams.PARAM_GZIP_COMPRESSION, "true");

    testling.handleRequestInternal(request, response);

    List<String> urlList = convertGzipToList();
    assertNotNull(urlList);
    assertEquals(7, urlList.size());
  }

  @Test
  public void testNoSuchSite() throws Exception {
    request.setPathInfo("/internal/noSuchSite/sitemap-org");
    request.addParameter(SitemapRequestParams.PARAM_EXCLUDE_FOLDERS, (String) null);

    testling.handleRequestInternal(request, response);

    assertThat(response.getStatus(), Matchers.equalTo(HttpServletResponse.SC_NOT_FOUND));
  }

  @Test
  public void testParamExcludeFolders() throws Exception {
    request.addParameter(SitemapRequestParams.PARAM_EXCLUDE_FOLDERS, "Contact");

    testling.handleRequestInternal(request, response);

    List<String> urlList = convertToList();

    assertNotNull(urlList);
    assertEquals(6, urlList.size());
  }

  @Test
  public void testParamExcludeMultipleFolders() throws Exception {
    request.addParameter(SitemapRequestParams.PARAM_EXCLUDE_FOLDERS, "Contact,Navigation");

    testling.handleRequestInternal(request, response);

    List<String> urlList = convertToList();

    assertNotNull(urlList);
    assertEquals(5, urlList.size());
  }

  @Test
  public void testParamExclusionPaths() throws Exception {
    request.addParameter(SitemapRequestParams.PARAM_EXCLUDE_FOLDERS, (String) null);
    urlGenerator.setExclusionPaths(singletonList("About/Contact"));

    testling.handleRequestInternal(request, response);

    List<String> urlList = convertToList();

    assertNotNull(urlList);
    assertEquals(6, urlList.size());
  }

  @Test
  public void testValidationService() throws Exception {
    ValidationService validationServiceRemoveCMChannel = new ValidationService() {
      @Override
      public List filterList(List list) {
        for (int i = 0; i < list.size(); i++) {
          Object o = list.get(i);
          if (o instanceof CMChannel) {
            list.remove(o);
          }
        }
        return list;
      }

      @Override
      public boolean validate(Object source) {
        return !(source instanceof CMChannel);
      }
    };
    urlGenerator.setValidationService(validationServiceRemoveCMChannel);
    request.addParameter(SitemapRequestParams.PARAM_EXCLUDE_FOLDERS, (String) null);

    testling.handleRequestInternal(request, response);

    List<String> urlList = convertToList();
    assertNotNull(urlList);
    assertEquals(6, urlList.size());
  }

  /**
   * Link scheme for tests. This link scheme renders links for all content beans with the pattern
   * http://www.coremedia.com/<content type>/<content id>
   */
  class GeneralPurposeLinkScheme implements LinkScheme {

    @Nullable
    @Override
    public String formatLink(Object bean, String view, @NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response, boolean forRedirect) throws URISyntaxException {
      ContentBean contentBean = (ContentBean) bean;
      StringBuilder stringBuilder = new StringBuilder("http://www.coremedia.com/");
      stringBuilder.append(contentBean.getContent().getType().getName()).append("/").append(IdHelper.parseContentId(contentBean.getContent().getId()));

      return stringBuilder.toString();
    }
  }

  /**
   * Convert output list to a list object to verify the results.
   *
   * @return A list where each entry contains one line of the print writer.
   */
  private List<String> convertToList() throws UnsupportedEncodingException {
    return asList(response.getContentAsString());
  }

  /**
   * Converts line separated string to an array.
   *
   * @param value A list of values, separated by linefeed.
   * @return The array of each line.
   */
  private List<String> asList(String value) {
    Scanner scanner = new Scanner(value);
    List<String> result = new ArrayList<>();

    while (scanner.hasNextLine()) {
      result.add(scanner.nextLine());
    }

    return result;
  }

  /**
   * Convert output list to a list object to verify the results.
   *
   * @return A list where each entry contains one line of the print writer.
   */
  private List<String> convertGzipToList() throws IOException {
    GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(response.getContentAsByteArray()));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (int value = 0; value != -1; ) {
      value = gzipInputStream.read();
      if (value != -1) {
        baos.write(value);
      }
    }
    gzipInputStream.close();
    baos.close();
    return asList(new String(baos.toByteArray(), StandardCharsets.UTF_8));
  }

  /**
   * Delegates the mocked Servlet output stream to a byte array output stream.
   */
  class SimpleServletOutputStream extends ServletOutputStream {
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    public void write(int b) throws IOException {
      out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      out.write(b);
    }

    public String toString() {
      return new String(out.toByteArray());
    }

    public byte[] toByteArray() {
      return out.toByteArray();
    }

    @Override
    public boolean isReady() {
      return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
  }

}
