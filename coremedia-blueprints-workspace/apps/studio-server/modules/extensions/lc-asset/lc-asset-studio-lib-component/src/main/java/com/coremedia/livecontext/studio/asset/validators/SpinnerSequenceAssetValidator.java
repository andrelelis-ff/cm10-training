package com.coremedia.livecontext.studio.asset.validators;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.asset.util.AssetReadSettingsHelper;
import com.coremedia.rest.cap.validation.ContentTypeValidatorBase;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import com.google.common.annotations.VisibleForTesting;

import java.util.List;

/**
 * Check spinner pictures for consistency.
 */
public class SpinnerSequenceAssetValidator extends ContentTypeValidatorBase {
  @VisibleForTesting
  static final String SEQUENCE_PROPERTY = "sequence";

  private static final String ISSUE_CODE_DIFFERENT_PICTURE_ASSETS = "spinnerSequencePicturesHaveDifferentReferences";

  private final AssetReadSettingsHelper assetHelper;

  public SpinnerSequenceAssetValidator(AssetReadSettingsHelper assetHelper, CapConnection connection) {
    this.assetHelper = assetHelper;
    setConnection(connection);
  }

  /**
   * Check the assigned commerce references of the spinners pictures.
   * <p>
   * Two different sets of commerce references are allowed in all pictures of
   * a spinner, the empty set and a non empty set.
   * <p>
   * Addressed usecases:
   * <ul>
   *   <li>All pictures are consistently tagged with the same set of commerce references,
   *   since the photographer delivered images with identical meta data which
   *   has been adopted during file upload.</li>
   *   <li>One picture (e.g. the first) is tagged with a set of commerce references. The editor did
   *   not bother to tag all the other spinner images, though.</li>
   *   <li>No single picture is tagged with commerce references, only the spinner itself.
   *   </li>
   * </ul>
   * Otherwise, if pictures belong to different commerce references, they seem to be
   * independent and most probably won't be suitable as a spinner sequence.
   */
  @Override
  public void validate(Content content, Issues issues) {
    List<Content> pictures = content.getLinks(SEQUENCE_PROPERTY);
    List<String> commerceReferences = null;
    for (Content pic : pictures) {
      List<String> references = assignedExternalReferences(pic);
      if (!references.isEmpty()) {
        if (commerceReferences==null) {
          commerceReferences = references;
        } else {
          // compare the lists regardless of order and duplicates
          boolean sameSets = references.containsAll(commerceReferences) && commerceReferences.containsAll(references);
          if (!sameSets) {
            issues.addIssue(Severity.WARN, SEQUENCE_PROPERTY, ISSUE_CODE_DIFFERENT_PICTURE_ASSETS);
            return;
          }
        }
      }
    }
  }

  // Not really my business, therefore make it a "blackbox" method wrt. validate().
  private List<String> assignedExternalReferences(Content pic) {
    return assetHelper.getCommerceReferences(pic.getProperties());
  }
}
