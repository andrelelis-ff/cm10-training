package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.contentbeans.CMExternalProduct;
import com.coremedia.livecontext.contentbeans.LiveContextExternalProduct;
import com.coremedia.livecontext.contentbeans.ProductDetailPage;
import com.coremedia.livecontext.context.ResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

import static com.coremedia.livecontext.fragment.FragmentHandler.UNRESOLVABLE_PLACEMENT_VIEW_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProductFragmentHandlerTest extends FragmentHandlerTestBase<ProductFragmentHandler> {

  @Mock
  private AugmentationService productAugmentationService;

  @Mock
  private ResolveContextStrategy categoryContextStrategy;

  @Mock
  private Product product;

  @Mock
  private Content augmentedProductContent;

  @Mock
  private LiveContextExternalProduct augmentedProductBean;

  @Mock
  private ContentType cmExternalProductContentType;

  @Mock
  private PageGridPlacement productPlacement;

  @Test(expected = IllegalStateException.class)
  public void handleProductViewFragmentNoSitesFound() {
    FragmentParameters params = getFragmentParameters4Product();
    when(request.getAttribute(SITE_ATTRIBUTE_NAME)).thenReturn(null);
    getTestling().createModelAndView(params, request);
  }

  @Test(expected = IllegalStateException.class)
  public void handleProductViewFragmentNoLiveContextNavigationFound() {
    when(getResolveContextStrategy().resolveContext(any(Site.class), any(Product.class))).thenReturn(Optional.empty());
    FragmentParameters params = getFragmentParameters4Product();
    params.setView(VIEW);
    getTestling().createModelAndView(params, request);
  }

  @Test(expected = IllegalStateException.class)
  public void handleProductViewFragmentWithCategoryNoLiveContextNavigationFound() {
    when(getResolveContextStrategy().resolveContext(any(Site.class), any(Category.class))).thenReturn(Optional.empty());
    FragmentParameters params = getFragmentParameters4Product();
    params.setView(VIEW);
    params.setCategoryId("categoryId");
    getTestling().createModelAndView(params, request);
  }

  @Test
  public void handleProductViewFragment() {
    ModelAndView result = getTestling().createModelAndView(getFragmentParameters4Product(), request);
    assertDefaultPage(result);
    verifyDefault();
  }

  @Test
  public void handleProductViewFragmentWithCategory() {
    when(categoryContextStrategy.resolveContext(site, category)).thenReturn(Optional.of(navigation));
    FragmentParameters params = getFragmentParameters4Product();
    params.setCategoryId("categoryId");
    ModelAndView result = getTestling().createModelAndView(params, request);
    assertDefaultPage(result);
    verifyDefault();
  }

  @Test(expected = IllegalStateException.class)
  public void handleProductPlacementFragmentNoLiveContextNavigationFound() {
    when(getResolveContextStrategy().resolveContext(getSite(), product)).thenReturn(Optional.empty());
    FragmentParameters params = getFragmentParameters4Product();
    params.setPlacement(PLACEMENT);
    getTestling().createModelAndView(params, request);
  }

  @Test(expected = IllegalStateException.class)
  public void handleProductPlacementFragmentWithCategoryNoLiveContextNavigationFound() {
    when(connection.getCatalogService().findCategoryById(any(CommerceId.class), any(StoreContext.class))).thenReturn(null);
    FragmentParameters params = getFragmentParameters4Product();
    params.setPlacement(PLACEMENT);
    params.setCategoryId("categoryId");
    getTestling().createModelAndView(params, request);
  }

  @Test
  public void handleProductPlacementFragmentFoundInAugmentedProduct() {
    FragmentParameters fragmentParameters4Product = getFragmentParameters4Product();
    fragmentParameters4Product.setPlacement(PLACEMENT);

    when(pageGridPlacementResolver.resolvePageGridPlacement(augmentedProductBean, PLACEMENT)).thenReturn(productPlacement);

    ModelAndView result = getTestling().createModelAndView(fragmentParameters4Product, request);
    assertDefaultPlacement(result);
    assertThat(unwrapDynamicIncludeModel(result)).isEqualTo(productPlacement);
    verifyDefault();
  }

  @Test
  public void handleProductPlacementFragmentFoundInAugmentedProductWithCategory() {
    FragmentParameters fragmentParameters4Product = getFragmentParameters4Product();
    fragmentParameters4Product.setPlacement(PLACEMENT);
    fragmentParameters4Product.setCategoryId("categoryId");
    when(categoryContextStrategy.resolveContext(any(Site.class), any(Product.class))).thenReturn(Optional.of(navigation));
    when(pageGridPlacementResolver.resolvePageGridPlacement(augmentedProductBean, PLACEMENT)).thenReturn(productPlacement);

    ModelAndView result = getTestling().createModelAndView(fragmentParameters4Product, request);
    assertDefaultPlacement(result);
    assertThat(unwrapDynamicIncludeModel(result)).isEqualTo(productPlacement);
    verifyDefault();
  }

  @Test
  public void handleProductPlacementFragmentFoundInParentChannel() {
    FragmentParameters fragmentParameters4Product = getFragmentParameters4Product();
    fragmentParameters4Product.setPlacement(PLACEMENT);

    when(productAugmentationService.getContent(product)).thenReturn(null);

    ModelAndView result = getTestling().createModelAndView(fragmentParameters4Product, request);
    assertDefaultPlacement(result);
    assertThat(unwrapDynamicIncludeModel(result)).isEqualTo(placement);
    verifyDefault();
  }

  @Test
  public void handleProductPlacementFragmentNotFound() {
    FragmentParameters fragmentParameters4Product = getFragmentParameters4Product();
    fragmentParameters4Product.setPlacement(PLACEMENT);

    when(pageGridPlacementResolver.resolvePageGridPlacement(augmentedProductBean, PLACEMENT)).thenReturn(null);

    ModelAndView result = getTestling().createModelAndView(fragmentParameters4Product, request);
    assertThat(result.getViewName()).isEqualTo(UNRESOLVABLE_PLACEMENT_VIEW_NAME);
    verifyDefault();
  }

  @Test
  public void handleProductAssetFragment() {
    FragmentParameters fragmentParameters4Product = getFragmentParameters4ProductAssets();
    when(connection.getCatalogService().findCategoryById(any(CommerceId.class), any(StoreContext.class))).thenReturn(category);
    when(connection.getCatalogService().findProductById(any(CommerceId.class), any(StoreContext.class))).thenReturn(product);
    when(product.getCategory()).thenReturn(category);

    ModelAndView result = getTestling().createModelAndView(fragmentParameters4Product, request);
    assertNotNull(result);
    assertNotNull(result.getModel());
    Object self = result.getModel().get("self");
    assertTrue(self instanceof Product);
    ModelMap modelMap = result.getModelMap();
    modelMap.containsAttribute("orientation");
    modelMap.containsAttribute("types");
  }

  @Override
  protected ProductFragmentHandler createTestling() {
    ProductFragmentHandler testling = new ProductFragmentHandler();
    testling.setProductAugmentationService(productAugmentationService);

    return testling;
  }

  @Before
  public void defaultSetup() {
    super.defaultSetup();
    when(connection.getCommerceBeanFactory().createBeanFor(any(CommerceId.class), any(StoreContext.class))).thenReturn(product);
    when(connection.getCatalogService().findCategoryById(any(CommerceId.class), any(StoreContext.class))).thenReturn(category);
    when(connection.getCatalogService().findProductById(any(), any(StoreContext.class))).thenReturn(product);
    when(product.getExternalId()).thenReturn("productId");
    when(productAugmentationService.getContent(product)).thenReturn(augmentedProductContent);
    when(contentBeanFactory.createBeanFor(augmentedProductContent, LiveContextExternalProduct.class)).thenReturn(augmentedProductBean);
    when(contentBeanFactory.createBeanFor(augmentedProductContent, Linkable.class)).thenReturn(augmentedProductBean);
    when(augmentedProductBean.getContent()).thenReturn(augmentedProductContent);
    when(augmentedProductContent.getType()).thenReturn(cmExternalProductContentType);
    when(cmExternalProductContentType.getName()).thenReturn(CMExternalProduct.NAME);
    when(validationService.validate(any())).thenReturn(true);
    when(navigation.getContext()).thenReturn(cmExternalChannelContext);

    when(beanFactory.getBean("pdpPage", ProductDetailPage.class)).thenReturn(new ProductDetailPage(false, sitesService, cache, null, null, null));
    getTestling().setContextStrategy(resolveContextStrategy);
  }

  @After
  public void tearDown() throws Exception {
    defaultTeardown();
  }
}
