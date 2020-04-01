package com.coremedia.livecontext.ecommerce.ibm.workspace;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.Workspace;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
class WorkspacesIT extends IbmServiceTestBase {

  @Inject
  private WorkspaceService workspaceService;

  @Inject
  private CatalogService catalogService;

  @Inject
  private IbmCommerceIdProvider ibmCommerceIdProvider;

  @Test
  void testFindAllWorkspaces() {
    if (useTapes() || WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      return;
    }

    Workspace workspace = findWorkspace("Anniversary");
    assertEquals("segment id has wrong format", BaseCommerceBeanType.WORKSPACE, workspace.getId().getCommerceBeanType());
  }

  @Test
  void testFindTestContentInWorkspace() {
    if (useTapes() || WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      return;
    }

    Workspace workspace = findWorkspace("Anniversary");

    StoreContext storeContextWithWorkspaceId = IbmStoreContextBuilder
            .from((StoreContextImpl) storeContext)
            .withWorkspaceId(WorkspaceId.of(workspace.getExternalTechId()))
            .build();

    CommerceId categoryId = ibmCommerceIdProvider.formatCategoryId(storeContextWithWorkspaceId.getCatalogAlias(), "PC_ForTheCook");
    Category category0 = catalogService.findCategoryById(categoryId, storeContextWithWorkspaceId);
    assertNotNull("category \"PC_ForTheCook\" not found", category0);

    List<Category> subCategories = catalogService.findSubCategories(category0);
    Category category1 = findFirst(subCategories, c -> "PC_Anniversary".equals(c.getExternalId()));
    assertNotNull("category \"PC_Anniversary\" not found", category1);

    List<Product> products = catalogService.findProductsByCategory(category1);
    assertNotNull(products);
    assertFalse(products.isEmpty());

    Product product = findFirst(products, p -> "PC_COOKING_HAT".equals(p.getExternalId()));
    assertNotNull("product \"PC_COOKING_HAT\" not found", product);
  }

  private Workspace findWorkspace(String name) {
    UserContext userContext = UserContext.builder().build();
    CurrentUserContext.set(userContext);

    List<Workspace> workspaces = workspaceService.findAllWorkspaces(storeContext);
    assertNotNull(workspaces);
    assertFalse(workspaces.isEmpty());

    Workspace workspace = findFirst(workspaces, w -> w.getName().startsWith(name));
    assertNotNull("workspace \"" + name + "...\" not found", workspace);

    return workspace;
  }

  @Nullable
  private static <T> T findFirst(@NonNull Collection<T> items, @NonNull Predicate<T> predicate) {
    return items.stream()
            .filter(predicate)
            .findFirst()
            .orElse(null);
  }
}
