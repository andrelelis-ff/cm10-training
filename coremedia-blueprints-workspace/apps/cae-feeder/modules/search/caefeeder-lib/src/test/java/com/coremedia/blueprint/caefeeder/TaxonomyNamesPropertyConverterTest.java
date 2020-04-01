package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TaxonomyNamesPropertyConverterTest extends AbstractTaxonomyPropertyConverterTest {

  @Test
  public void testNoTaxonomies() {
    List<CMLocTaxonomy> items = new ArrayList<>();

    TaxonomyPropertyConverter converter = new TaxonomyNamesPropertyConverter();
    converter.setIgnoreParents(true);
    Assert.assertNull(converter.convertValue(items));
  }

  @Test
  public void testConvertValueNoParents() {
    List<CMLocTaxonomy> items = new ArrayList<>();
    items.add(sanFrancisco);
    items.add(michigan);

    TaxonomyPropertyConverter converter = new TaxonomyNamesPropertyConverter();
    converter.setIgnoreParents(true);
    String result = (String) converter.convertValue(items);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("Michigan"));
    Assert.assertTrue(result.contains("San Francisco"));


    List<CMTaxonomy> subject = new ArrayList<>();
    subject.add(formula1);
    result = (String) converter.convertValue(subject);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("Formula 1"));
  }

  @Test
  public void testConvertValueWithParents() {
    List<CMLocTaxonomy> items = new ArrayList<>();
    items.add(sanFrancisco);
    items.add(michigan);

    TaxonomyPropertyConverter converter = new TaxonomyNamesPropertyConverter();
    converter.setTaxonomyPathKeyFactory(taxonomyPathKeyFactory);
    String result = (String) converter.convertValue(items);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("World"));
    Assert.assertTrue(result.contains("United States"));
    Assert.assertTrue(result.contains("California"));
    Assert.assertTrue(result.contains("Michigan"));
    Assert.assertTrue(result.contains("San Francisco"));


    List<CMTaxonomy> subject = new ArrayList<>();
    subject.add(formula1);
    result = (String) converter.convertValue(subject);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("Sport"));
    Assert.assertTrue(result.contains("Motorsport"));
    Assert.assertTrue(result.contains("Formula 1"));
  }

}
