package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_8;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for BOD REST interface.
 */
@ExtendWith(SpringExtension.class)
abstract class AbstractB2BCatalogServiceIT extends IbmCatalogServiceBaseTest {

  @Inject
  private UserSessionService userSessionService;

  private List<String> getContractIdsForUser(String user, String password) {
    HttpServletRequest request = new MockHttpServletRequest();
    HttpServletResponse response = new MockHttpServletResponse();

    UserContext userContextBeforeLogin = UserContext.builder().withUserId(user).withUserName(user).build();
    CurrentUserContext.set(userContextBeforeLogin);

    boolean loginSuccess = userSessionService.loginUser(request, response, user, password);
    assertTrue(loginSuccess);

    UserContext userContextAfterLogin = CurrentUserContext.find().orElse(null);
    assertNotNull(userContextAfterLogin);
    assertNotNull(userContextAfterLogin.getCookieHeader());

    Collection<Contract> contractIdsForUser = contractService.findContractIdsForUser(userContextAfterLogin,
            storeContext);
    return contractIdsForUser.stream()
            .map(contract -> {
              assert contract != null;
              return contract.getExternalTechId();
            })
            .collect(toList());
  }

  protected void testFindSubCategoriesWithContract() throws Exception {
    if (useTapes() || StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_8)) {
      return;
    }

    StoreContextImpl b2bStoreContext = testConfig.getB2BStoreContext(connection);

    Category category = findAndAssertCategory("Lighting", null, b2bStoreContext);
    assertNotNull(category);

    category = findAndAssertCategory("Fasteners", null, b2bStoreContext);
    assertNotNull(category);

    List<Category> subCategoriesNoContract = testling.findSubCategories(category);
    assertTrue(subCategoriesNoContract.size() >= 3);

    // Test b2b categories with contract.
    List<String> contractIds = getContractIdsForUser("bmiller", "passw0rd");
    StoreContextImpl b2bStoreContextWithContractIds = IbmStoreContextBuilder
            .from(b2bStoreContext)
            .withContractIds(contractIds)
            .build();

    category = findAndAssertCategory("Fasteners", null, b2bStoreContextWithContractIds);

    List<Category> subCategoriesWithContract = testling.findSubCategories(category);
    assertEquals(2, subCategoriesWithContract.size());
    assertEquals(BaseCommerceBeanType.CATEGORY, subCategoriesWithContract.get(0).getId().getCommerceBeanType());
    assertEquals("Bolts", subCategoriesWithContract.get(0).getName());
    assertEquals("Screws", subCategoriesWithContract.get(1).getName());
  }
}
