package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TaxonomyIdsPropertyConverterTest extends AbstractTaxonomyPropertyConverterTest {

  @Test
  public void testNoTaxonomies() {
    List<CMLocTaxonomy> items = new ArrayList<>();

    TaxonomyPropertyConverter converter = new TaxonomyIdsPropertyConverter();
    converter.setIgnoreParents(true);
    Assert.assertNull(converter.convertValue(items));
  }

  @Test
  public void testConvertValueNoParents() {
    List<CMLocTaxonomy> items = new ArrayList<>();
    items.add(sanFrancisco);
    items.add(michigan);

    TaxonomyPropertyConverter converter = new TaxonomyIdsPropertyConverter();
    converter.setIgnoreParents(true);
    String result = (String) converter.convertValue(items);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("70"));
    Assert.assertTrue(result.contains("72"));


    List<CMTaxonomy> subject = new ArrayList<>();
    subject.add(formula1);
    result = (String) converter.convertValue(subject);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("80"));
  }

  @Test
  public void testConvertValueWithParents() {
    List<CMLocTaxonomy> items = new ArrayList<>();
    items.add(sanFrancisco);
    items.add(michigan);

    TaxonomyPropertyConverter converter = new TaxonomyIdsPropertyConverter();
    converter.setTaxonomyPathKeyFactory(taxonomyPathKeyFactory);
    String result = (String) converter.convertValue(items);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("62"));
    Assert.assertTrue(result.contains("64"));
    Assert.assertTrue(result.contains("68"));
    Assert.assertTrue(result.contains("70"));
    Assert.assertTrue(result.contains("72"));


    List<CMTaxonomy> subject = new ArrayList<>();
    subject.add(formula1);
    result = (String) converter.convertValue(subject);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("76"));
    Assert.assertTrue(result.contains("78"));
    Assert.assertTrue(result.contains("80"));
  }
}
