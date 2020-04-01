package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.assets.AssetConstants;
import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extension class for beans of document type "AMAsset".
 */
public class AMAssetImpl extends com.coremedia.blueprint.assets.contentbeans.AMAssetBase {

  @Override
  public AMTaxonomy getPrimaryCategory() {
    List<AMTaxonomy> assetCategories = getAssetCategories();
    return assetCategories.isEmpty() ? null : assetCategories.get(0);
  }


  @NonNull
  @Override
  public List<AMAssetRendition> getRenditions() {
    List<AMAssetRendition> result = new ArrayList<>();
    result.add(getRendition(AMAsset.ORIGINAL));
    return result;
  }

  @NonNull
  @Override
  public List<AMAssetRendition> getPublishedRenditions() {
    List<AMAssetRendition> allRenditions = getRenditions();
    List<AMAssetRendition> publishedRenditions = new ArrayList<>();
    for (AMAssetRendition rendition : allRenditions) {
      if (null != rendition.getBlob() && rendition.isPublished()) {
        publishedRenditions.add(rendition);
      }
    }
    return publishedRenditions;
  }

  @Override
  public Calendar getValidFrom() {
    // There is no valid-from constraint for assets
    return null;
  }

  @Override
  public Calendar getValidTo() {
    Struct metadata = this.getMetadata();
    if (null == metadata) {
      return null;
    }
    return CapStructHelper.getDate(metadata, AssetConstants.METADATA_EXPIRATIONDATE_PROPERTY_NAME);
  }

  protected AMAssetRendition getRendition(String renditionName) {
    return new AMAssetRenditionImpl(renditionName, this);
  }

  @NonNull
  @Override
  public List<CMTaxonomy> getAllSubjects() {
    List<CMTaxonomy> directlyLinkedSubjects = getSubjectTaxonomy();
    // deduplicate subjects by mean of a set
    Set<CMTaxonomy> allSubjects = new HashSet<>();
    for (CMTaxonomy directlyLinkedSubject : directlyLinkedSubjects) {
      for (CMTaxonomy subject : directlyLinkedSubject.getTaxonomyPathList()) {
        allSubjects.add(subject);
      }
    }

    List<CMTaxonomy> sortedResultList = new ArrayList<>(allSubjects);
    Collections.sort(sortedResultList, new Comparator<CMTaxonomy>() {
      @Override
      public int compare(CMTaxonomy o1, CMTaxonomy o2) {
        // default provides good enough sorting algorithm
        Collator defaultCollator = Collator.getInstance();
        return defaultCollator.compare(StringUtils.defaultString(o1.getValue()),
                StringUtils.defaultString(o2.getValue()));
      }
    });

    return sortedResultList;
  }

}
