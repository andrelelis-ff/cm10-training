package com.coremedia.livecontext.ecommerce.ibm.p13n;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceRemoteException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.p13n.Segment;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceService;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.Assume;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.function.Function;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.SEGMENT;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test(s) for the {@link com.coremedia.livecontext.ecommerce.p13n.SegmentService}.
 * <p>
 * Unfortunately the WCS uses technical IDs to create segments which differ between WCS7 and WCS8. That why You should
 * avoid using IDs if possible. Currently we're using following IDs for WCS versions 7 and 8:
 * <table>
 * <tr>
 * <th>Segment Name</th>
 * <th>ID WCS7</th>
 * <th>ID WCS8</th>
 * </tr>
 * <tr>
 * <td>Customers who are 40 years of age or older</td>
 * <td>8000000000000000557</td>
 * <td>8407790678950000508</td>
 * </tr>
 * <tr>
 * <td>Customers who are under 40 years of age</td>
 * <td>8000000000000000556</td>
 * <td>8407790678950000507</td>
 * </tr>
 * <tr>
 * <td>Female Customers</td>
 * <td>8000000000000000555</td>
 * <td>8407790678950000506</td>
 * </tr>
 * <tr>
 * <td>Frequent Buyer</td>
 * <td>8000000000000000751</td>
 * <td>8407790678950000652</td>
 * </tr>
 * <tr>
 * <td>Guest Shoppers</td>
 * <td>8000000000000000553</td>
 * <td>8407790678950000504</td>
 * </tr>
 * <tr>
 * <td>Male Customers</td>
 * <td>8000000000000000554</td>
 * <td>8407790678950000505</td>
 * </tr>
 * <tr>
 * <td>Registered Customers</td>
 * <td>8000000000000000551</td>
 * <td>8407790678950000502</td>
 * </tr>
 * <tr>
 * <td>Repeat Customers</td>
 * <td>8000000000000000552</td>
 * <td>8407790678950000503</td>
 * </tr>
 * </table>
 */
@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_SegmentServiceImplIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
public class SegmentServiceImplIT extends IbmServiceTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

  private static final String REGISTERED_CUSTOMERS = "Registered Customers";

  @Autowired
  private SegmentServiceImpl testling;

  @Autowired
  private WorkspaceService workspaceService;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();

    boolean olderThan_7_7 = StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7);
    Assumptions.assumeFalse(olderThan_7_7);

    CurrentUserContext.set(UserContext.builder().build());
  }

  @Test
  void testFindAllSegments() {
    List<Segment> segments = executeFindSegments(f -> testling.findAllSegments(storeContext));
    assertThat(segments)
            .isNotEmpty()
            .last()
            .satisfies(s -> {
              assertThat(s).hasFieldOrPropertyWithValue("name", "Repeat Customers");
              assertThat(s).extracting(repeatCustomer -> repeatCustomer.getId().getCommerceBeanType())
                      .isEqualTo(SEGMENT);
            });
  }

  @Test
  void testFindSegmentById() {
    Segment registeredCustomers = executeFindSegments(f -> testling.findAllSegments(storeContext))
            .stream()
            .filter(s -> REGISTERED_CUSTOMERS.equals(s.getName()))
            .findFirst()
            .orElse(null);

    assertThat(registeredCustomers).isNotNull();
    String externalTechId = registeredCustomers.getExternalTechId();
    assertThat(externalTechId).isNotBlank();

    CommerceId registeredCustomersId = CommerceIdParserHelper.parseCommerceIdOrThrow("ibm:///x/segment/" + externalTechId);
    Segment segment2 = testling.findSegmentById(registeredCustomersId, storeContext);
    assertThat(segment2).isNotNull();

    CommerceId commerceId = segment2.getId();
    assertThat(commerceId.getCommerceBeanType()).isEqualTo(SEGMENT);
    assertThat(segment2.getName()).isEqualTo(REGISTERED_CUSTOMERS);
    assertThat(segment2).isEqualTo(registeredCustomers);
  }

  /**
   * The CommerceRemoteException with message "CWXFR0230E: Internal server error. Details will be stored within the server logs."
   * is ignored in this test.
   * This is due to a NPE in the commerce system, which presumably comes from other tests which brings the commerce system into an unstable state.
   * So this leads to a failing test which is not caused by our product.
   * For more information see Jira: CMS-14842, CMS-15867
   */
  @Test
  void testFindSegmentsByUser() {
    UserContext userContext = UserContext.builder()
            .withUserId(System.getProperty("lc.test.user2.id", "4"))
            .withUserName(testConfig.getUser2Name())
            .build();
    CurrentUserContext.set(userContext);

    List<Segment> segments = executeFindSegments(f -> testling.findSegmentsForCurrentUser(storeContext));
    assertThat(segments).isNotEmpty();
    assertThat(segments.size()).isGreaterThanOrEqualTo(3);
  }

  @Test
  void testFindSegmentsByUserInWS() {
    if (useTapes() || WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      return;
    }

    UserContext userContext = UserContext.builder()
            .withUserId(System.getProperty("lc.test.user2.id", "4"))
            .withUserName(testConfig.getUser2Name())
            .build();
    CurrentUserContext.set(userContext);

    WorkspaceId workspaceId = findAnniversaryWorkspaceId(storeContext);
    StoreContext storeContextWithWorkspaceId = IbmStoreContextBuilder
            .from((StoreContextImpl) storeContext)
            .withWorkspaceId(workspaceId)
            .build();

    List<Segment> segments = testling.findSegmentsForCurrentUser(storeContextWithWorkspaceId);
    assertThat(segments).isNotEmpty()
            .extracting("name")
            .contains("Loyal, early Perfect Chef Customer", "Frequent Buyer", "Male Customers", REGISTERED_CUSTOMERS);
  }

  @NonNull
  private List<Segment> executeFindSegments(Function<StoreContext, List<Segment>> findSegmentsFunction) {
    try {
      return findSegmentsFunction.apply(storeContext);
    } catch (CommerceRemoteException e) {
      // CommerceRemoteExceptions with error code CWXFR0230E occur only sometimes. As they apparently depend on the test
      // environment and don't seem to be caused by a product problem, affected tests are just skipped when they occur.
      Assume.assumeFalse("Ignoring remote exception (error code CWXFR0230E)", contains(e.getMessage(), "CWXFR0230E"));
      // All other exceptions are really unexpected and thus must be thrown.
      throw e;
    }
  }

  @NonNull
  private WorkspaceId findAnniversaryWorkspaceId(@NonNull StoreContext storeContext) {
    return workspaceService.findAllWorkspaces(storeContext).stream()
            .filter(workspace -> workspace.getName().startsWith("Anniversary"))
            .map(CommerceBean::getExternalTechId)
            .map(WorkspaceId::of)
            .findFirst()
            .orElseThrow(IllegalStateException::new);
  }
}
