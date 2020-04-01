package com.coremedia.blueprint.ecommerce.cae;

import com.coremedia.blueprint.base.links.UriConstants;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractStoreContextProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceConnection;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCommerceContextInterceptorTest {

  @Mock
  private Site site;

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Mock
  private HttpServletRequest request;

  @Spy
  private AbstractCommerceContextInterceptor testling;

  @Spy
  private AbstractStoreContextProvider storeContextProvider;

  // --- setup ------------------------------------------------------

  @Before
  public void setup() {
    BaseCommerceConnection commerceConnection = new BaseCommerceConnection();
    commerceConnection.setStoreContextProvider(storeContextProvider);
    commerceConnection.setInitialStoreContext(StoreContextBuilderImpl.from(commerceConnection, "any-site-id").build());

    when(commerceConnectionInitializer.findConnectionForSite(site)).thenReturn(Optional.of(commerceConnection));

    testling.setCommerceConnectionInitializer(commerceConnectionInitializer);
  }

  // --- tests ------------------------------------------------------

  @Test
  public void testNormalizePath() {
    String path = "/helios";

    String normalizedPath = AbstractCommerceContextInterceptor.normalizePath(path);

    assertThat(normalizedPath).as("changed path").isEqualTo(path);
  }

  @Test
  public void testNormalizeDynamicFragmentPath() {
    String path = "/cart/helios/action/cart";
    String dynamicPath = "/" + UriConstants.Segments.PREFIX_DYNAMIC + path;

    String normalizedPath = AbstractCommerceContextInterceptor.normalizePath(dynamicPath);

    assertThat(normalizedPath).as("path not normalized").isEqualTo(path);
  }

  @Test
  public void testNormalizePathWithNull() {
    String normalizedPath = AbstractCommerceContextInterceptor.normalizePath(null);

    assertThat(normalizedPath).isNull();
  }

  @Test
  public void testInitStoreContextProvider() {
    // This does not work with the @Mock request.
    MockHttpServletRequest request = new MockHttpServletRequest();

    Optional<CommerceConnection> connection = testling.getCommerceConnectionWithConfiguredStoreContext(site, request);
    assertThat(connection)
            .isNotEmpty()
            .map(CommerceConnection::getStoreContext)
            .isNotEmpty();
  }

  @Test
  public void testInitStoreContextProviderWithPreviewParameters() {
    when(request.getParameter(ValidityPeriodValidator.REQUEST_PARAMETER_PREVIEW_DATE)).thenReturn("12-06-2014 13:00 Europe/Berlin");
    when(request.getParameter(AbstractCommerceContextInterceptor.QUERY_PARAMETER_WORKSPACE_ID)).thenReturn("aWorkspaceId");
    testling.setPreview(true);

    Optional<CommerceConnection> connection = testling.getCommerceConnectionWithConfiguredStoreContext(site, request);

    assertThat(connection)
            .isNotEmpty()
            .map(CommerceConnection::getStoreContext)
            .hasValueSatisfying(
                    context -> {
                      assertThat(context.getPreviewDate()).isPresent();
                      assertThat(context.getWorkspaceId()).isPresent();
                    }
            );
  }
}
