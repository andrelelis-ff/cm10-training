package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.objectserver.beans.AbstractContentBean;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;

/**
 * Base class for beans of document type "AMAsset".
 */
public abstract class AMAssetBase extends AbstractContentBean implements AMAsset {
  private static final String IS_IN_PRODUCTION = "isInProduction";

  @Override
  public int getContentId() {
    return IdHelper.parseContentId(getContent().getId());
  }

  protected Blob getOriginal() {
    return getContent().getBlobRef(ORIGINAL);
  }

  @Override
  public Blob getThumbnail() {
    return getContent().getBlobRef(THUMBNAIL);
  }

  @Override
  public String getTitle() {
    return getContent().getName();
  }

  @Override
  public List<AMTaxonomy> getAssetCategories() {
    List<Content> contents = getContent().getLinksFulfilling(ASSET_TAXONOMY, IS_IN_PRODUCTION);
    return createBeansFor(contents, AMTaxonomy.class);
  }

  @Override
  public String getKeywords() {
    return getContent().getString(KEYWORDS);
  }

  @Override
  public List<CMTaxonomy> getSubjectTaxonomy() {
    List<Content> contents = getContent().getLinksFulfilling(SUBJECT_TAXONOMY, IS_IN_PRODUCTION);
    return createBeansFor(contents, CMTaxonomy.class);
  }

  @Override
  public List<CMLocTaxonomy> getLocationTaxonomy() {
    List<Content> contents = getContent().getLinksFulfilling(LOCATION_TAXONOMY, IS_IN_PRODUCTION);
    return createBeansFor(contents, CMLocTaxonomy.class);
  }

  /**
   * Returns the value of the document property {@link #METADATA}.
   *
   * @return a {@link Struct}
   */
  @Nullable
  @Override
  public Struct getMetadata() {
    return getContent().getStruct(METADATA);
  }
}
