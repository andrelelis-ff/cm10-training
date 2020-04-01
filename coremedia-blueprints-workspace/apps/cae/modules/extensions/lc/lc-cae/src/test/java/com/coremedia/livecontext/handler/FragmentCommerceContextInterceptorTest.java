package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceConnection;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.NoStoreContextAvailable;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.blueprint.common.preview.PreviewDateFormatter;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.contract.ContractService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserContextProvider;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.coremedia.livecontext.fragment.FragmentContext;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.FragmentParametersFactory;
import com.coremedia.livecontext.fragment.links.context.Context;
import com.coremedia.livecontext.fragment.links.context.ContextBuilder;
import com.coremedia.livecontext.fragment.links.context.LiveContextContextHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FragmentCommerceContextInterceptorTest {

  private static final ZoneId ZONE_ID_BERLIN = ZoneId.of("Europe/Berlin");
  private static final ZoneId ZONE_ID_US_PACIFIC = ZoneId.of("US/Pacific");

  private static final String REQUEST_PATH_INFO = "/anyShop";

  @Spy
  @InjectMocks
  private FragmentCommerceContextInterceptor testling;

  @Mock
  private Site site;

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  private BaseCommerceConnection commerceConnection;

  @Mock
  private ContractService contractService;

  @SuppressWarnings("unused")
  @Mock
  private CatalogAliasTranslationService catalogAliasTranslationService;

  @Mock
  private StoreContextProvider storeContextProvider;

  @Mock
  private UserContextProvider userContextProvider;

  private MockHttpServletRequest request;
  private StoreContextImpl storeContext;
  private UserContext userContext;

  @Before
  public void setup() {
    commerceConnection = new BaseCommerceConnection();

    storeContext = StoreContextBuilderImpl.from(commerceConnection, "any-site-id").build();
    userContext = UserContext.builder().build();

    commerceConnection.setVendorName("IBM");
    commerceConnection.setStoreContextProvider(storeContextProvider);
    commerceConnection.setUserContextProvider(userContextProvider);
    commerceConnection.setInitialStoreContext(storeContext);
    commerceConnection.setContractService(contractService);

    when(storeContextProvider.buildContext(any())).thenReturn(StoreContextBuilderImpl.from(storeContext));
    when(userContextProvider.createContext(any())).thenReturn(userContext);
    when(commerceConnectionInitializer.findConnectionForSite(site)).thenReturn(Optional.of(commerceConnection));

    runTestlingInPreviewMode(false);

    createFragmentContext(
            "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;catalogId=catalog");

    request = new MockHttpServletRequest("GET", "test/params;placement=header");
    request.setPathInfo(REQUEST_PATH_INFO);
  }

  @After
  public void tearDown() {
    CurrentStoreContext.remove();
    CurrentUserContext.remove();
  }

  private static void createFragmentContext(@SuppressWarnings("SameParameterValue") @NonNull String url) {
    FragmentContext fragmentContext = new FragmentContext();
    fragmentContext.setFragmentRequest(true);
    fragmentContext.setParameters(FragmentParametersFactory.create(url));
  }

  private void configureFragmentContext(@NonNull Context fragmentContext) {
    try {
      new FragmentContextProvider().doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    } catch (IOException | ServletException e) {
      throw new IllegalStateException(e);
    }
  }

  private void runTestlingInPreviewMode(boolean previewMode) {
    testling.setPreview(previewMode);
    doReturn(previewMode).when(testling).isStudioPreviewRequest(request);
  }

  @Test
  public void testInitUserContextProvider() {
    Context fragmentContext = ContextBuilder.create()
            .withValue("wc.user.id", "userId")
            .withValue("wc.user.loginid", "loginId")
            .build();
    LiveContextContextHelper.setContext(request, fragmentContext);

    testling.initUserContext(commerceConnection, request);

    UserContext userContext = CurrentUserContext.find().orElse(null);
    assertThat(userContext).isNotNull();
    assertThat(userContext.getUserId()).isEqualTo("userId");
    assertThat(userContext.getUserName()).isEqualTo("loginId");
  }

  @Test
  public void testInitStoreContextWithContractIds() {
    runTestlingInPreviewMode(true);

    Collection<Contract> contracts = newArrayList(
            mockContract("contract1"),
            mockContract("contract2")
    );
    when(contractService.findContractIdsForUser(any(UserContext.class), any(StoreContext.class), nullable(String.class)))
            .thenReturn(contracts);

    Context fragmentContext = ContextBuilder.create()
            .withValue("wc.user.id", "userId")
            .withValue("wc.user.loginid", "loginId")
            .withValue("wc.preview.contractIds", "contract1 contract2")
            .build();
    LiveContextContextHelper.setContext(request, fragmentContext);

    configureFragmentContext(fragmentContext);

    testling.initUserContext(commerceConnection, request);

    assertThat(getStoreContext().getContractIds()).containsExactlyInAnyOrder("contract1", "contract2");
  }

  @Test
  public void testInitStoreContextWithContractIdsButDisabledProcessing() throws IOException, ServletException {
    runTestlingInPreviewMode(true);

    testling.setContractsProcessingEnabled(false);

    Context fragmentContext = ContextBuilder.create()
            .withValue("wc.user.id", "userId")
            .withValue("wc.user.loginid", "loginId")
            .withValue("wc.preview.contractIds", "contract1 contract2")
            .build();
    configureFragmentContext(fragmentContext);

    testling.getCommerceConnectionWithConfiguredStoreContext(site, request);
    testling.initUserContext(commerceConnection, request);
    List<String> contractIdsInStoreContext = storeContext.getContractIds();
    assertThat(contractIdsInStoreContext).isEmpty();
  }

  @Test
  public void testInitStoreContextProviderInPreview() {
    ZonedDateTime expectedPreviewDate = zonedDateTime(2014, Month.JULY, 2, 17, 57, 0, ZONE_ID_BERLIN);
    String expectedPreviewDateStr = "02-07-2014 17:57 Europe/Berlin";

    runTestlingInPreviewMode(true);

    Context fragmentContext = ContextBuilder.create()
            .withValue("wc.preview.memberGroups", "memberGroup1, memberGroup2")
            .withValue("wc.preview.timestamp", "2014-07-02 17:57:00.0")
            .withValue("wc.preview.timezone", "Europe/Berlin")
            .withValue("wc.preview.workspaceId", "4711")
            .build();
    LiveContextContextHelper.setContext(request, fragmentContext);
    configureFragmentContext(fragmentContext);

    StoreContext storeContext = getStoreContext();

    assertThat(storeContext.getUserSegments()).contains("memberGroup1, memberGroup2");
    assertThat(storeContext.getWorkspaceId()).contains(WorkspaceId.of("4711"));

    Optional<ZonedDateTime> previewDate = storeContext.getPreviewDate();
    assertThat(previewDate).as("preview date in store context").contains(expectedPreviewDate);

    String requestParam = convertToPreviewDateRequestParameterFormat(previewDate.get());
    assertThat(requestParam).as("preview date in request parameter").isEqualTo(expectedPreviewDateStr);

    assertEqual((Calendar) request.getAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE),
            previewDate.get());
  }

  @Test
  public void testInitStoreContextProviderWithTimeShift() {
    ZonedDateTime expectedPreviewDate = zonedDateTime(2014, Month.JULY, 2, 17, 57, 0, ZONE_ID_US_PACIFIC);
    String expectedPreviewDateStr = "02-07-2014 17:57 US/Pacific";

    runTestlingInPreviewMode(true);

    Context fragmentContext = ContextBuilder.create()
            .withValue("wc.preview.timestamp", "2014-07-02 17:57:00.0")
            .withValue("wc.preview.timezone", "US/Pacific")
            .build();
    LiveContextContextHelper.setContext(request, fragmentContext);
    configureFragmentContext(fragmentContext);

    StoreContext storeContext = getStoreContext();

    Optional<ZonedDateTime> previewDate = storeContext.getPreviewDate();
    assertThat(previewDate).as("preview date in store context").contains(expectedPreviewDate);

    String requestParam = convertToPreviewDateRequestParameterFormat(previewDate.get());
    assertThat(requestParam).as("preview date in request parameter").isEqualTo(expectedPreviewDateStr);

    assertEqual((Calendar) request.getAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE),
            previewDate.get());
  }

  @Test
  public void testConvertPreviewDate() {
    ZonedDateTime expectedPreviewDate = zonedDateTime(2014, Month.JULY, 2, 17, 57, 0, ZONE_ID_BERLIN);
    String expectedPreviewDateStr = "02-07-2014 17:57 Europe/Berlin";

    runTestlingInPreviewMode(true);

    Context fragmentContext = ContextBuilder.create()
            .withValue("wc.preview.memberGroups", "memberGroup1, memberGroup2")
            .withValue("wc.preview.timestamp", "2014-07-02 17:57:00.0")
            .withValue("wc.preview.timezone", "Europe/Berlin")
            .withValue("wc.preview.workspaceId", "4711")
            .build();
    LiveContextContextHelper.setContext(request, fragmentContext);
    configureFragmentContext(fragmentContext);

    StoreContext storeContext = getStoreContext();

    assertThat(storeContext.getUserSegments()).contains("memberGroup1, memberGroup2");
    assertThat(storeContext.getWorkspaceId()).contains(WorkspaceId.of("4711"));

    Optional<ZonedDateTime> previewDate = storeContext.getPreviewDate();
    assertThat(previewDate).as("preview date in store context").contains(expectedPreviewDate);

    String requestParam = convertToPreviewDateRequestParameterFormat(previewDate.get());
    assertThat(requestParam).as("preview date in request parameter").isEqualTo(expectedPreviewDateStr);

    assertEqual((Calendar) request.getAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE),
            previewDate.get());
  }

  @Test
  public void testInitStoreContextProviderInLive() {
    Context fragmentContext = ContextBuilder.create()
            .withValue("wc.preview.memberGroups", "memberGroup1, memberGroup2")
            .withValue("wc.preview.timestamp", "2014-07-02 17:57:00.0")
            .withValue("wc.preview.workspaceId", "4711")
            .build();
    configureFragmentContext(fragmentContext);

    StoreContext storeContext = getStoreContext();

    assertThat(storeContext.getUserSegments()).isNotPresent();
    assertThat(storeContext.getPreviewDate()).isNotPresent();
    assertThat(storeContext.getWorkspaceId()).isNotPresent();
    assertThat(request.getAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE)).isNull();
  }

  @NonNull
  private StoreContext getStoreContext() {
    return testling
            .getCommerceConnectionWithConfiguredStoreContext(site, request)
            .map(CommerceConnection::getStoreContext)
            .orElseThrow(() -> new NoStoreContextAvailable("Store context not available on commerce connection."));
  }

  @NonNull
  private static Contract mockContract(@NonNull String externalTechId) {
    Contract contract = mock(Contract.class);
    when(contract.getExternalTechId()).thenReturn(externalTechId);
    return contract;
  }

  @NonNull
  private static ZonedDateTime zonedDateTime(int year, @NonNull Month month, int dayOfMonth, int hour, int minute,
                                             int second, @NonNull ZoneId zoneId) {
    int nanoOfSecond = 0;
    LocalDateTime localDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    return ZonedDateTime.of(localDateTime, zoneId);
  }

  @NonNull
  private static String convertToPreviewDateRequestParameterFormat(@NonNull ZonedDateTime previewDate) {
    return PreviewDateFormatter.format(previewDate);
  }

  private static void assertEqual(Calendar actual, @NonNull ZonedDateTime expected) {
    Calendar expectedCalendar = GregorianCalendar.from(expected);
    assertThat(actual.compareTo(expectedCalendar)).isEqualTo(0);
  }
}
