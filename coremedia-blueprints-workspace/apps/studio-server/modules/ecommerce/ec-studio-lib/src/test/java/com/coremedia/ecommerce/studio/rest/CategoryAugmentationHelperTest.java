package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesAutoConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.MappedCatalogsProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.intercept.InterceptService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY;
import static com.coremedia.ecommerce.studio.rest.AugmentationHelperBase.DEFAULT_BASE_FOLDER_NAME;
import static com.coremedia.ecommerce.studio.rest.AugmentationHelperBase.EXTERNAL_ID;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.CATEGORY_PAGEGRID_STRUCT_PROPERTY;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.SEGMENT;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.TITLE;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {XmlRepoConfiguration.class, CategoryAugmentationHelperTest.LocalConfig.class})
@TestPropertySource(properties = {"livecontext.cache.invalidation.enabled=false"})
public class CategoryAugmentationHelperTest {

  private static final String CATEGORY_EXTERNALID = "leafCategory";
  private static final String CATEGORY_ID = "test:///catalog/category/" + CATEGORY_EXTERNALID;
  //External ids of category can contain '/'. See CMS-5075
  private static final String CATEGORY_DISPLAY_NAME = "le/af";
  private static final String ESCAPED_CATEGORY_DISPLAY_NAME = "le_af";
  private static final String ROOT = "root";
  private static final String TOP = "top";

  @Inject
  private ContentRepository contentRepository;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private CategoryAugmentationHelper testling;

  @Mock
  private AugmentationService augmentationService;

  @Mock
  private MappedCatalogsProvider mappedCatalogsProvider;

  @Mock
  private Category rootCategory;

  @Mock
  private Category leafCategory;

  @Mock
  private Catalog catalog;

  @Mock
  private StoreContext storeContext;

  @Mock
  ContentBackedPageGridService pageGridService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    testling.setAugmentationService(augmentationService);
    testling.setMappedCatalogsProvider(mappedCatalogsProvider);
    testling.setPageGridService(pageGridService);

    Content rootCategoryContent = contentRepository.getContent("20");
    when(augmentationService.getContent(rootCategory)).thenReturn(rootCategoryContent);

    //mock category tree
    when(rootCategory.isRoot()).thenReturn(true);
    when(rootCategory.getDisplayName()).thenReturn(ROOT);
    Category topCategory = mock(Category.class);
    when(topCategory.getParent()).thenReturn(rootCategory);
    when(topCategory.getDisplayName()).thenReturn(TOP);
    leafCategory = mock(Category.class, RETURNS_DEEP_STUBS);
    when(leafCategory.getParent()).thenReturn(topCategory);
    when(leafCategory.getDisplayName()).thenReturn(CATEGORY_DISPLAY_NAME);
    when(leafCategory.getExternalId()).thenReturn(CATEGORY_EXTERNALID);
    when(leafCategory.getId()).thenReturn(CommerceIdParserHelper.parseCommerceIdOrThrow(CATEGORY_ID));
    List<Category> breadcrumb = newArrayList(rootCategory, topCategory, leafCategory);
    when(leafCategory.getBreadcrumb()).thenReturn(breadcrumb);
    when(leafCategory.getContext().getSiteId()).thenReturn("theSiteId");
    doReturn(Optional.of(catalog)).when(testling).getCatalog(leafCategory);
    when(catalog.isDefaultCatalog()).thenReturn(true);

    when(rootCategory.getContext()).thenReturn(storeContext);
    when(storeContext.getSiteId()).thenReturn("theSiteId");
  }

  @Test
  public void testAugment() {
    testling.augment(leafCategory);

    Content externalChannel = contentRepository.getChild("/Sites/Content Test/" + DEFAULT_BASE_FOLDER_NAME + "/"
            + ROOT + "/" + TOP + "/" + ESCAPED_CATEGORY_DISPLAY_NAME + "/" + ESCAPED_CATEGORY_DISPLAY_NAME + " (" + CATEGORY_EXTERNALID + ")");
    assertThat(externalChannel).isNotNull();
    assertThat(externalChannel.getName()).isEqualTo(ESCAPED_CATEGORY_DISPLAY_NAME  + " (" + CATEGORY_EXTERNALID + ")");
    assertThat(externalChannel.getString(EXTERNAL_ID)).isEqualTo(CATEGORY_ID);
    assertThat(externalChannel.getString(TITLE)).isEqualTo(CATEGORY_DISPLAY_NAME);
    assertThat(externalChannel.getString(SEGMENT)).isEqualTo(CATEGORY_DISPLAY_NAME);

    // Assert the initialized layout for category pages.

    Struct categoryPageGridStruct = externalChannel.getStruct(CATEGORY_PAGEGRID_STRUCT_PROPERTY);
    assertThat(categoryPageGridStruct).isNotNull();

    Struct categoryPlacements2Struct = categoryPageGridStruct.getStruct(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    assertThat(categoryPlacements2Struct).isNotNull();

    Content categoryLayout = (Content) categoryPlacements2Struct.get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
    assertThat(categoryLayout).isNotNull();
    assertThat(categoryLayout.getName()).isEqualTo("CategoryLayoutSettings");

    // Assert the initialized layout for product pages.

    Struct productPageGridStruct = externalChannel.getStruct(CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY);
    assertThat(productPageGridStruct).isNotNull();

    Struct productPlacements2Struct = productPageGridStruct.getStruct(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    assertThat(productPlacements2Struct).isNotNull();

    Content productLayout = (Content) productPlacements2Struct.get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
    assertThat(productLayout).isNotNull();
    assertThat(productLayout.getName()).isEqualTo("ProductLayoutSettings");
  }

  @Test
  public void testLookupAugmentedRootCategoryInOtherCatalogsHit() {
    Category bCategory = mock(Category.class);
    List<Category> listOfRootCategories = new ArrayList<>();
    listOfRootCategories.add(0, rootCategory);
    listOfRootCategories.add(1, bCategory);
    Content bCategoryAugmentation = mock(Content.class);
    when(mappedCatalogsProvider.getConfiguredRootCategories(any(StoreContext.class))).thenReturn(listOfRootCategories);
    when(augmentationService.getContent(rootCategory)).thenReturn(null);
    when(augmentationService.getContent(bCategory)).thenReturn(bCategoryAugmentation);

    Optional<Content> content = testling.lookupAugmentedRootCategoryInOtherCatalogs(leafCategory);

    assertThat(content).isPresent();
    assertThat(content.get()).isEqualTo(bCategoryAugmentation);
  }

  @Test(expected = CommerceAugmentationException.class)
  public void testInitializeLayoutSettingsWithInvalidState() {
    when(augmentationService.getContent(rootCategory)).thenReturn(null);

    testling.initializeLayoutSettings(leafCategory, emptyMap());
  }

  @Test
  public void testInitializeRootCategoryContent() {
    Content layoutFromSiteRoot = contentRepository.getContent("224");
    when(augmentationService.getContent(rootCategory)).thenReturn(null);
    when(pageGridService.getLayout(any(Content.class), eq(PAGE_GRID_STRUCT_PROPERTY))).thenReturn(layoutFromSiteRoot);

    Map properties = new HashMap();
    testling.initializeRootCategoryContent(rootCategory, properties);

    assertThat(properties).isNotEmpty();
    Struct placement = (Struct) properties.get("placement");
    Struct pdpPageGrid = (Struct) properties.get("pdpPagegrid");
    assertThat(placement).isNotNull();
    assertThat(pdpPageGrid).isNotNull();
    assertThat(placement).isEqualTo(pdpPageGrid);

    Content layoutPalcement = (Content) placement.getStruct(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME).get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
    assertThat(layoutPalcement).isNotNull();
    assertThat(layoutPalcement).isEqualTo(layoutFromSiteRoot);
  }

  @Configuration
  @ImportResource(value = {
          "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
  }, reader = ResourceAwareXmlBeanDefinitionReader.class)
  @Import(BaseCommerceServicesAutoConfiguration.class)
  public static class LocalConfig {

    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/ecommerce/studio/rest/ec-studio-lib-test-content.xml");
    }

    @Bean
    CategoryAugmentationHelper categoryAugmentationResource() {
      return spy(new CategoryAugmentationHelper());
    }

    @Bean
    public InterceptService interceptService() {
      return null;
    }

    @Bean
    public ContentBackedPageGridService contentBackedPageGridService() {
      return mock(ContentBackedPageGridService.class);
    }
  }
}
