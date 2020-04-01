package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CategoryOverviewTest {

  @Test
  public void equalsHashcode_differentAttributes_differentObject() {
    AMTaxonomy category = mock(AMTaxonomy.class);
    AMTaxonomy differentCategory = mock(AMTaxonomy.class);

    Subcategory subcategory = mock(Subcategory.class);
    Subcategory differentSubcategory = mock(Subcategory.class);
    List<Subcategory> subcategoryList1 = Arrays.asList(subcategory, differentSubcategory);
    List<Subcategory> subcategoryList2 = Arrays.asList(differentSubcategory, subcategory);

    CategoryOverview categoryOverview = new CategoryOverview(category, subcategoryList1);
    CategoryOverview equalCategoryOverview = new CategoryOverview(category, subcategoryList1);
    CategoryOverview differentCategoryOverview = new CategoryOverview(differentCategory, subcategoryList1);
    CategoryOverview differentCategoryOverview2 = new CategoryOverview(category, subcategoryList2);
    CategoryOverview differentCategoryOverview3 = new CategoryOverview(null, subcategoryList2);

    //noinspection EqualsWithItself
    assertTrue("CategoryOverview objects should not be different", categoryOverview.equals(categoryOverview));
    assertTrue("CategoryOverview objects should not be different", categoryOverview.equals(equalCategoryOverview));
    assertFalse("CategoryOverview objects should be different", categoryOverview.equals(differentCategoryOverview));
    assertFalse("CategoryOverview objects should be different", categoryOverview.equals(differentCategoryOverview2));
    assertFalse("CategoryOverview objects should be different", categoryOverview.equals(differentCategoryOverview3));
    assertFalse("CategoryOverview objects should be different", categoryOverview.equals(new Object()));
    //noinspection ObjectEqualsNull
    assertFalse("CategoryOverview objects should be different", categoryOverview.equals(null));
    assertEquals("Equal CategoryOverviews should have equal hashCodes", categoryOverview.hashCode(), equalCategoryOverview.hashCode());
    assertNotEquals("Different CategoryOverviews should have different hashCodes", categoryOverview.hashCode(), differentCategoryOverview.hashCode());
    assertNotEquals("Different CategoryOverviews should have different hashCodes", categoryOverview.hashCode(), differentCategoryOverview2.hashCode());
    assertNotEquals("Different CategoryOverviews should have different hashCodes", categoryOverview.hashCode(), differentCategoryOverview3.hashCode());
  }

  @Test
  public void getters_provideLegalValues_valuesShouldNotBeModified() {
    AMTaxonomy category = mock(AMTaxonomy.class);
    Subcategory subcategory = mock(Subcategory.class);
    List<Subcategory> subcategoryList = Collections.singletonList(subcategory);

    CategoryOverview categoryOverview = new CategoryOverview(category, subcategoryList);

    assertEquals("Category should stay the same", category, categoryOverview.getCategory());
    assertEquals("Number of assets should stay the same", subcategoryList, categoryOverview.getSubcategories());
    assertTrue(categoryOverview.toString().contains(CategoryOverview.class.getSimpleName()));
  }

}