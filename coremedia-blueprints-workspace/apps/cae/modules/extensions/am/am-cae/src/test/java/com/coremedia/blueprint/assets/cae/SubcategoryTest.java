package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SubcategoryTest {


  @Test
  public void equalsHashcode_differentAttributes_differentObject() {
    AMTaxonomy category = mock(AMTaxonomy.class);

    Subcategory subcategory = new Subcategory(category, 1);
    Subcategory equalSubcategory = new Subcategory(category, 1);
    Subcategory differentSubcategory = new Subcategory(category, 2);

    //noinspection EqualsWithItself
    assertTrue("AMSubcategory objects should not be different", subcategory.equals(subcategory));
    assertTrue("AMSubcategory objects should bit be different", subcategory.equals(equalSubcategory));
    assertFalse("AMSubcategory objects should be different", subcategory.equals(differentSubcategory));
    //noinspection ObjectEqualsNull
    assertFalse("AMSubcategory objects should be different", subcategory.equals(null));
    assertNotEquals("Different AMSubcategories should have different hashCodes", subcategory.hashCode(), differentSubcategory.hashCode());
  }

  @Test
  public void getters_provideLegalValues_valuesShouldNotBeModified() {
    AMTaxonomy category = mock(AMTaxonomy.class);
    long numberOfAssets = 1L;
    Subcategory subcategory = new Subcategory(category, numberOfAssets);

    assertEquals("Category should stay the same", category, subcategory.getCategory());
    assertEquals("Number of assets should stay the same", numberOfAssets, subcategory.getAssetsInCategory());
  }

  @Test
  public void getters_provideIllegalValues_illegalValueNormalized() {
    AMTaxonomy category = mock(AMTaxonomy.class);
    long numberOfAssets = -1L;
    Subcategory subcategory = new Subcategory(category, numberOfAssets);

    assertEquals("Category should stay the same", category, subcategory.getCategory());
    assertEquals("Number of assets should have been set to 0", 0, subcategory.getAssetsInCategory());
    assertTrue(subcategory.toString().contains(Subcategory.class.getSimpleName()));
  }


}