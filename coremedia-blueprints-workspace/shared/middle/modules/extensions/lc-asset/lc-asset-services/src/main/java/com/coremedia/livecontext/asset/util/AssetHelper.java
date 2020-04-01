package com.coremedia.livecontext.asset.util;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.cap.struct.StructBuilderMode.LOOSE;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_COMMERCE;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_INHERIT;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_LOCAL_SETTINGS;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_ORIGIN_REFERENCES;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_REFERENCES;
import static com.coremedia.livecontext.asset.util.InheritFlag.DO_NOT_INHERIT;
import static com.coremedia.livecontext.asset.util.InheritFlag.INHERIT;

/**
 * Helper for common livecontext asset operations.
 */
public class AssetHelper {

  private ContentRepository contentRepository;
  private AssetReadSettingsHelper assetReadSettingsHelper;

  /**
   * Update the picture document with a new list of catalog object ids. That means the picture will be assigned
   * to each catalog object as catalog object picture. It handles all kinds of conflicts and corner cases when an update
   * is coming in.
   * <p>
   * The following cases will be handled:
   * <pre>
   *           OLD STATE                                    NEW XMP DATA   RESULT STATE
   *
   * case  5:  null / []                                    null / []      No commerce struct
   * case  6:  null / []                                    [A, B]         inherit:TRUE, ori: [A, B], new: [A, B]
   * case  7:  inherit:TRUE, ori: [A, B], new: [A, B]       [A, C, D]      inherit:TRUE, ori: [A, C, D], new: [A, C, D]
   * case  8:  inherit:TRUE, ori: [A, B], new: [A, B]       null / []      No commerce struct
   * case  9:  inherit:FALSE, ori: [A, B], new: [A, C, D]   [E, F]         inherit:FALSE, ori: [E, F], new: [A, C, D]
   * case 10:  inherit:FALSE, ori: [A, B], new: [A, C, D]   null / []      inherit:FALSE, ori: [], new: [A, C, D]
   * case 11:  inherit:FALSE, ori: [A, B], new: []          [E, F]         inherit:TRUE, ori: [E, F], new: [E, F]
   * case 12:  inherit:FALSE, ori: [], new: [A, C, D]       [E, F]         inherit:FALSE, ori: [E, F], new: [A, C, D]
   * case 13:  new: [A, C, D]                               [E, F]         inherit:FALSE, ori: [E, F], new: [A, C, D]
   * case 14:  inherit:FALSE, ori: [A, B], new: []          []             inherit:FALSE, ori: [], new: []
   * case 15:  inherit:FALSE, ori: [], new: []              [A, B]         inherit:FALSE, ori: [A, B], new: [A, B]
   * </pre>
   * <p>
   * Case 15 is same as case 6 but with an empty commerce struct.
   *
   * @param content               the picture document
   * @param newCommerceReferences the list of catalog object ids that are to be assigned
   * @return the struct property that contains the updated commerce struct
   */
  public Struct updateCMPictureForExternalIds(@Nullable Content content, @NonNull List<String> newCommerceReferences) {
    // load/create localSettins struct
    Struct struct = content == null ? null : content.getStruct(NAME_LOCAL_SETTINGS);

    if (struct == null) {
      if (newCommerceReferences.isEmpty()) {
        // do nothing --> return empty struct
        return getEmptyStruct();
      } else {
        // case 5 (struct empty and externalIds empty)
        return updateStruct(getEmptyStruct(), INHERIT, newCommerceReferences, newCommerceReferences);
      }
    }

    Map<String, Object> contentProperties = content.getProperties();
    boolean noCommerceStructInContent = !assetReadSettingsHelper.hasCommerceStruct(contentProperties);

    if (noCommerceStructInContent) {
      if (newCommerceReferences.isEmpty()) {
        //do nothing --> return original struct
        return struct;
      } else {
        // case 4 and 6
        // upload with first time XMP data
        return updateStruct(struct, INHERIT, newCommerceReferences, newCommerceReferences);
      }
    }

    // upload with existing struct
    List<String> oldCommerceReferences = assetReadSettingsHelper.getCommerceReferences(contentProperties);
    List<String> oldOriginCommerceReferences = assetReadSettingsHelper.getOriginCommerceReferences(contentProperties);
    boolean inherit = assetReadSettingsHelper.readInheritedField(contentProperties);

    return rewriteCommerceStruct(struct, inherit, oldCommerceReferences, oldOriginCommerceReferences,
            newCommerceReferences);
  }

  private Struct rewriteCommerceStruct(@NonNull Struct struct, boolean inherit, List<String> oldCommerceReferences,
                                       List<String> oldOriginCommerceReferences,
                                       @NonNull List<String> newCommerceReferences) {
    // case 7-8 --> inherit = TRUE
    if (inherit) {
      if (newCommerceReferences.isEmpty()) {
        // case 8
        return removeCommerceSubstruct(struct);
      } else {
        // case 7
        return updateStruct(struct, INHERIT, newCommerceReferences, newCommerceReferences);
      }
    }

    // inherit=FALSE && originReferences = []
    if (oldOriginCommerceReferences.isEmpty()) {
      if (oldCommerceReferences.isEmpty()) {
        // case 15
        return updateStruct(struct, INHERIT, newCommerceReferences, newCommerceReferences);
      } else {
        // case 13
        return updateStruct(struct, DO_NOT_INHERIT, newCommerceReferences, oldCommerceReferences);
      }
    }

    if (oldCommerceReferences.isEmpty()) {
      if (newCommerceReferences.isEmpty()) {
        // case 14
        return updateStruct(struct, DO_NOT_INHERIT, newCommerceReferences, newCommerceReferences);
      } else {
        // case 11
        return updateStruct(struct, INHERIT, newCommerceReferences, newCommerceReferences);
      }
    }

    // case 9-10,12
    return updateStruct(struct, DO_NOT_INHERIT, newCommerceReferences, oldCommerceReferences);
  }

  private Struct updateStruct(@NonNull Struct struct, @NonNull InheritFlag inheritFlag, List<String> newOriginReferences,
                              List<String> newReferences) {
    StructBuilder builder = struct.builder().mode(LOOSE);
    builder.set(NAME_COMMERCE, getEmptyStruct());
    builder.enter(NAME_COMMERCE);

    // check what if catalogObjectIds = null
    builder.declareBoolean(NAME_INHERIT, inheritFlag.value);
    builder.declareStrings(NAME_ORIGIN_REFERENCES, Integer.MAX_VALUE, newOriginReferences);
    builder.declareStrings(NAME_REFERENCES, Integer.MAX_VALUE, newReferences);

    return builder.build();
  }

  /**
   * Removes the commerce struct from the given @param#struct
   *
   * @param struct the local settings struct
   * @return A struct with no commerce substruct
   */
  public static Struct removeCommerceSubstruct(@NonNull Struct struct) {
    StructBuilder structBuilder = struct.builder().remove(NAME_COMMERCE);
    return structBuilder.build();
  }

  /**
   * Removes the catalog object data from the picture struct
   *
   * @param content the image document
   * @return The updated struct
   */
  @Nullable
  public Struct updateCMPictureOnBlobDelete(@Nullable Content content) {
    if (content == null) {
      return null;
    }

    Struct struct = content.getStruct(NAME_LOCAL_SETTINGS);
    if (struct == null) {
      return null;
    }

    Map<String, Object> properties = content.getProperties();

    if (assetReadSettingsHelper.hasCommerceStruct(properties)
            && assetReadSettingsHelper.readInheritedField(properties)) {
      struct = updateStruct(struct, DO_NOT_INHERIT, Collections.<String>emptyList(), Collections.<String>emptyList());
    }

    return struct;
  }

  private Struct getEmptyStruct() {
    return contentRepository.getConnection().getStructService().createStructBuilder().build();
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setAssetReadSettingsHelper(AssetReadSettingsHelper assetReadSettingsHelper) {
    this.assetReadSettingsHelper = assetReadSettingsHelper;
  }
}
