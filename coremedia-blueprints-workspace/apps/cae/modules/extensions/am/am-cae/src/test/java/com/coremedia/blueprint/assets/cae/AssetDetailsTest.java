package com.coremedia.blueprint.assets.cae;


import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AssetDetailsTest {

  @Test
  public void assetDetails() {
    AMAsset asset = mock(AMAsset.class);
    AMTaxonomy taxonomy = mock(AMTaxonomy.class);
    Map<String, String> metadata = new HashMap<>();
    metadata.put("property", "value");

    AssetDetails details = new AssetDetails(asset, taxonomy);
    details.setMetadataProperties(metadata);

    assertEquals(asset, details.getAsset());
    assertEquals(taxonomy, details.getCategory());
    assertEquals(metadata, details.getMetadataProperties());
  }

  @Test
  public void equalsHashCode() {
    AMAsset asset1 = mock(AMAsset.class);
    AMTaxonomy taxonomy1 = mock(AMTaxonomy.class);
    AssetDetails details1 = new AssetDetails(asset1, taxonomy1);

    //noinspection EqualsWithItself
    assertTrue(details1.equals(details1));
    //noinspection ObjectEqualsNull
    assertFalse(details1.equals(null));
    assertTrue(details1.toString().contains(AssetDetails.class.getSimpleName()));

    AssetDetails details1_clone = new AssetDetails(asset1, taxonomy1);
    assertEquals(details1.hashCode(), details1_clone.hashCode());
    assertTrue(details1.equals(details1_clone));

    AMAsset asset2 = mock(AMAsset.class);
    AMTaxonomy taxonomy2 = mock(AMTaxonomy.class);
    AssetDetails details2 = new AssetDetails(asset2, taxonomy2);
    assertFalse(details1.equals(details2));
    assertNotEquals(details1.hashCode(), details2.hashCode());

    AssetDetails details3 = new AssetDetails(asset1, null);
    assertFalse(details1.equals(details3));
  }
}
