package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

public class PaginatedAssetsTest {


  @Test
  public void equalsHashcode_differentAttributes_differentObject() {
    AMAsset asset = mock(AMAsset.class);
    AMAsset differentAsset = mock(AMAsset.class);

    List<AMAsset> assetList = Arrays.asList(asset, differentAsset);
    PaginatedAssets paginatedAssets = new PaginatedAssets(assetList, 1, 1, 0);
    PaginatedAssets equalPaginatedAssets = new PaginatedAssets(assetList, 1, 1, 0);
    PaginatedAssets differentPaginatedAssets = new PaginatedAssets(assetList, 1, 2, 0);

    //noinspection EqualsWithItself
    assertTrue("CategoryAssets objects should not be different", paginatedAssets.equals(paginatedAssets));
    assertTrue("CategoryAssets objects should not be different", paginatedAssets.equals(equalPaginatedAssets));
    assertFalse("CategoryAssets objects should be different", paginatedAssets.equals(differentPaginatedAssets));
    //noinspection ObjectEqualsNull
    assertFalse("CategoryAssets objects should be different", paginatedAssets.equals(null));
    assertNotEquals("Different CategoryAssets should have different hashCodes", paginatedAssets.hashCode(), differentPaginatedAssets.hashCode());
  }


  @Test
  public void getters_provideLegalValues_valuesShouldNotBeModified() {
    AMAsset asset = mock(AMAsset.class);
    List<AMAsset> assetList = Collections.singletonList(asset);
    Map<String, String> baseRequestParams = Collections.singletonMap("test", "me");

    PaginatedAssets paginatedAssets = new PaginatedAssets(assetList, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
    paginatedAssets.setBaseRequestParams(baseRequestParams);

    assertEquals("Assets should stay the same", assetList, paginatedAssets.getAssets());
    assertEquals("Current page number should stay the same", Integer.MAX_VALUE, paginatedAssets.getCurrentPage());
    assertEquals("Total number of pages should stay the same", Integer.MAX_VALUE, paginatedAssets.getPageCount());
    assertEquals("Base Requests params should stay the same", baseRequestParams, paginatedAssets.getBaseRequestParams());
    assertTrue(paginatedAssets.toString().contains(PaginatedAssets.class.getSimpleName()));
  }

  @Test
  public void adjustCurrentPageAndPageCountIfInvalidValues() {
    AMAsset asset = mock(AMAsset.class);
    PaginatedAssets paginatedAssets = new PaginatedAssets(Collections.singletonList(asset), 0, 0, 0);

    assertEquals(1, paginatedAssets.getCurrentPage());
    assertEquals(1, paginatedAssets.getPageCount());
  }

  @Test
  public void adjustCurrentPageIfCurrentPageGreaterThanPageCount() {
    AMAsset asset = mock(AMAsset.class);
    PaginatedAssets paginatedAssets = new PaginatedAssets(Collections.singletonList(asset), 3, 2, 0);

    assertEquals(1, paginatedAssets.getCurrentPage());
    assertEquals(2, paginatedAssets.getPageCount());
  }
}