package com.coremedia.blueprint.assets.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.common.DateConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class AMDocumentAssetImplTest extends AMTestBase {

  private AMDocumentAssetImpl amDocumentAsset;

  @Before
  public void setUp() throws Exception {
    amDocumentAsset = getContentBean(6);
  }

  @Test
  public void properties() throws ParseException {
    assertNotNull(amDocumentAsset);
    assertNotNull(amDocumentAsset.getRenditions());
    assertEquals(2, amDocumentAsset.getRenditions().size());

    assertNotNull(amDocumentAsset.getDownload());
    assertNotNull(amDocumentAsset.getOriginal());
    assertNotNull(amDocumentAsset.getThumbnail());

    assertTrue(amDocumentAsset.getRendition(AMDocumentAsset.DOWNLOAD).isPublished());
    assertEquals(amDocumentAsset.getDownload(), amDocumentAsset.getPublishedRenditions().get(0).getBlob());

    assertEquals(DateConverter.convertToCalendar("2005-12-31T06:00:00+01:00").getTimeInMillis(), amDocumentAsset.getValidTo().getTimeInMillis());
  }

  @Test
  public void taxonomies() {
    List<CMTaxonomy> subjectTaxonomies = amDocumentAsset.getSubjectTaxonomy();
    assertEquals(1, subjectTaxonomies.size());
    CMTaxonomy cmSubjectTaxonomy = subjectTaxonomies.get(0);
    assertEquals("Sport", cmSubjectTaxonomy.getValue());

    List<CMTaxonomy> allSubjects = amDocumentAsset.getAllSubjects();
    assertEquals(1, allSubjects.size());
    assertEquals(cmSubjectTaxonomy, allSubjects.get(0));

    List<CMLocTaxonomy> locationTaxonomies = amDocumentAsset.getLocationTaxonomy();
    assertEquals(1, locationTaxonomies.size());
    CMLocTaxonomy locationTaxonomy = locationTaxonomies.get(0);
    assertEquals("Hamburg", locationTaxonomy.getValue());

    List<AMTaxonomy> assetTaxonomies = amDocumentAsset.getAssetCategories();
    assertEquals(1, assetTaxonomies.size());
    AMTaxonomy assetTaxonomy = assetTaxonomies.get(0);
    assertEquals("amTaxonomy", assetTaxonomy.getValue());
    assertEquals(assetTaxonomy, amDocumentAsset.getPrimaryCategory());
  }

  @Test
  public void equals_hashCode() {
    assertFalse(amDocumentAsset.equals(new AMDocumentAssetImpl()));
    assertFalse(amDocumentAsset.equals(new Object()));
    assertEquals(amDocumentAsset.hashCode(), amDocumentAsset.getContent().hashCode());
  }
}