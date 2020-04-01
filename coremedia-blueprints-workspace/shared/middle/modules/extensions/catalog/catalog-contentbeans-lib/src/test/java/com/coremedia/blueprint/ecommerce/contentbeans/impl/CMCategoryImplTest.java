package com.coremedia.blueprint.ecommerce.contentbeans.impl;

import com.coremedia.blueprint.base.ecommerce.catalog.AbstractCmsCommerceBean;
import com.coremedia.blueprint.base.ecommerce.catalog.CmsCatalogService;
import com.coremedia.blueprint.base.ecommerce.catalog.CmsCategory;
import com.coremedia.blueprint.base.ecommerce.catalog.CmsProduct;
import com.coremedia.blueprint.ecommerce.contentbeans.CMCategory;
import com.coremedia.blueprint.ecommerce.contentbeans.CMProduct;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CMCategoryImplTest {

  @Mock
  private CmsCatalogService catalogService;

  @Mock
  private Content content;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  private CMCategoryImpl category = new CMCategoryImpl();

  @Before
  public void setUp() throws Exception {
    category = Mockito.spy(new CMCategoryImpl());
    category.setCatalogService(catalogService);
    doReturn(content).when(category).getContent();
    doReturn(contentBeanFactory).when(category).getContentBeanFactory();
  }

  @Test
  public void testGetSubcategoriesNoCmsCategoryFound() {
    assertEquals(ImmutableList.of(), ImmutableList.copyOf(category.getSubcategories()));
  }

  @Test
  public void testGetSubcategories() {
    Content contentChild1 = mock(Content.class, "child1");
    Content contentChild2 = mock(Content.class, "child2");
    ImmutableList<Content> contentChildren = ImmutableList.of(contentChild1, contentChild2);

    CmsCategory cmsCategory = mock(CmsCategory.class);
    when(catalogService.findCategoryByContent(content)).thenReturn(cmsCategory);

    CmsCategory cmsCategoryChild1 = mockCmsCommerceBean(contentChild1, CmsCategory.class);
    CmsCategory cmsCategoryChild2 = mockCmsCommerceBean(contentChild2, CmsCategory.class);
    ImmutableList<Category> cmsCategoryChildren = ImmutableList.<Category>of(cmsCategoryChild1, cmsCategoryChild2);
    when(cmsCategory.getChildren()).thenReturn(cmsCategoryChildren);

    CMCategory cmCategory1 = mock(CMCategory.class);
    CMCategory cmCategory2 = mock(CMCategory.class);
    List<CMCategory> expectedChildren = ImmutableList.of(cmCategory1, cmCategory2);
    when(contentBeanFactory.createBeansFor(contentChildren, CMCategory.class)).thenReturn(expectedChildren);

    assertEquals(expectedChildren, category.getSubcategories());
  }

  @Test
  public void testGetProductsNoCmsCategoryFound() {
    assertEquals(ImmutableList.of(), ImmutableList.copyOf(category.getProducts()));
  }

  @Test
  public void testGetProducts() {
    Content contentChild1 = mock(Content.class, "child1");
    Content contentChild2 = mock(Content.class, "child2");
    ImmutableList<Content> contentChildren = ImmutableList.of(contentChild1, contentChild2);

    CmsCategory cmsCategory = mock(CmsCategory.class);
    when(catalogService.findCategoryByContent(content)).thenReturn(cmsCategory);

    CmsProduct cmsProductChild1 = mockCmsCommerceBean(contentChild1, CmsProduct.class);
    CmsProduct cmsProductChild2 = mockCmsCommerceBean(contentChild2, CmsProduct.class);
    ImmutableList<Product> cmsProductChildren = ImmutableList.<Product>of(cmsProductChild1, cmsProductChild2);
    when(cmsCategory.getProducts()).thenReturn(cmsProductChildren);

    CMProduct cmProduct1 = mock(CMProduct.class);
    CMProduct cmProduct2 = mock(CMProduct.class);
    List<CMProduct> expectedChildren = ImmutableList.of(cmProduct1, cmProduct2);
    when(contentBeanFactory.createBeansFor(contentChildren, CMProduct.class)).thenReturn(expectedChildren);

    assertEquals(expectedChildren, category.getProducts());
  }


  // ----------------------------------------------------------------------

  private static <T extends AbstractCmsCommerceBean> T mockCmsCommerceBean(Content content, Class<T> type) {
    T result = mock(type);
    when(result.getContent()).thenReturn(content);
    return result;
  }

}