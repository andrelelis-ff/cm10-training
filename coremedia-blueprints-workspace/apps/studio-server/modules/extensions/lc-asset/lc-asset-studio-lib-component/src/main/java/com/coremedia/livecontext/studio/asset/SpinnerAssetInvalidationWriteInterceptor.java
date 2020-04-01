package com.coremedia.livecontext.studio.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.ecommerce.studio.rest.cache.CommerceCacheInvalidationSource;
import com.coremedia.livecontext.asset.util.AssetReadSettingsHelper;
import com.coremedia.livecontext.asset.util.AssetWriteSettingsHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_LOCAL_SETTINGS;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

public class SpinnerAssetInvalidationWriteInterceptor extends ContentWriteInterceptorBase {

  private static final Logger LOG = LoggerFactory.getLogger(SpinnerAssetInvalidationWriteInterceptor.class);

  @VisibleForTesting
  static final String SEQUENCE_SPINNER_PROPERTY = "sequence";

  private final CommerceCacheInvalidationSource commerceCacheInvalidationSource;
  private final CommerceConnectionSupplier commerceConnectionSupplier;
  private final AssetReadSettingsHelper assetReadSettingsHelper;
  private final AssetWriteSettingsHelper assetWriteSettingsHelper;
  private final ContentRepository contentRepository;

  SpinnerAssetInvalidationWriteInterceptor(CommerceCacheInvalidationSource commerceCacheInvalidationSource,
                                           CommerceConnectionSupplier commerceConnectionSupplier,
                                           AssetReadSettingsHelper assetReadSettingsHelper,
                                           AssetWriteSettingsHelper assetWriteSettingsHelper,
                                           ContentRepository contentRepository) {
    this.commerceCacheInvalidationSource = commerceCacheInvalidationSource;
    this.commerceConnectionSupplier = commerceConnectionSupplier;
    this.assetReadSettingsHelper = assetReadSettingsHelper;
    this.assetWriteSettingsHelper = assetWriteSettingsHelper;
    this.contentRepository = contentRepository;
  }

  @Override
  public void intercept(@NonNull ContentWriteRequest request) {
    if (!checkPreconditions(request)) {
      return;
    }

    Content content = request.getEntity();
    Map<String, Object> oldProperties = content.getProperties();
    Map<String, Object> newProperties = request.getProperties();

    if (sequencePropertyChanged(request)) {
      rewriteOriginalExternalReferences(oldProperties, newProperties);
      invalidateExternalReferences(content, oldProperties, newProperties);
    }

    if (inheritedChanged(oldProperties, newProperties)) {
      rewriteOriginalExternalReferences(oldProperties, newProperties);
      invalidateExternalReferences(content, oldProperties, newProperties);
    }

    if (assignedCommerceReferencesChanged(oldProperties, newProperties)) {
      invalidateExternalReferences(content, oldProperties, newProperties);
    }
  }

  @VisibleForTesting
  boolean assignedCommerceReferencesChanged(@NonNull Map<String, Object> oldProperties,
                                            @NonNull Map<String, Object> newProperties) {
    if (!assetReadSettingsHelper.hasCommerceStruct(newProperties)) {
      // If those properties do not exist it means that there are no
      // changes in the struct, so no changes in the inherited field.
      return false;
    }

    if (!assetReadSettingsHelper.hasCommerceStruct(oldProperties)) {
      // This means no commerce struct exists in content, so the state
      // from the request is more current.
      return true;
    }

    if (!assetReadSettingsHelper.hasReferencesList(newProperties)) {
      // If no references exists which must be written to the commerce
      // references list, no new commerce references exist.
      return false;
    }

    List<String> oldCommerceReferences = assetReadSettingsHelper.getCommerceReferences(oldProperties);
    List<String> newCommerceReferences = assetReadSettingsHelper.getCommerceReferences(newProperties);

    return !CollectionUtils.isEqualCollection(oldCommerceReferences, newCommerceReferences);
  }

  @VisibleForTesting
  boolean sequencePropertyChanged(@NonNull ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    return properties.containsKey(SEQUENCE_SPINNER_PROPERTY);
  }

  @VisibleForTesting
  boolean checkPreconditions(@NonNull ContentWriteRequest request) {
    Content content = request.getEntity();
    Map<String, Object> properties = request.getProperties();

    return content != null && properties != null;
  }

  @VisibleForTesting
  boolean inheritedChanged(@Nullable Map<String, Object> oldProperties,
                           @Nullable Map<String, Object> newProperties) {
    if (!assetReadSettingsHelper.hasCommerceStruct(newProperties)) {
      // If those properties do not exist it means that there are no
      // changes in the struct, so no changes in the inherited field.
      return false;
    }

    if (!assetReadSettingsHelper.hasCommerceStruct(oldProperties)) {
      // This means no commerce struct exists in content, so the state
      // from the request is more current.
      return true;
    }

    boolean oldInherit = assetReadSettingsHelper.readInheritedField(oldProperties);
    boolean newInherit = assetReadSettingsHelper.readInheritedField(newProperties);

    return oldInherit != newInherit;
  }

  @VisibleForTesting
  void invalidateExternalReferences(@NonNull Content content, @NonNull Map<String, Object> oldProperties,
                                    @NonNull Map<String, Object> newProperties) {
    // Get all external IDs which may have changed in Studio due to the
    // change of the sequence property.
    Set<String> allReferences = new HashSet<>();

    if (assetReadSettingsHelper.hasReferencesList(newProperties)) {
      allReferences.addAll(assetReadSettingsHelper.getCommerceReferences(newProperties));
    }

    if (assetReadSettingsHelper.hasReferencesList(oldProperties)) {
      allReferences.addAll(assetReadSettingsHelper.getCommerceReferences(oldProperties));
    }

    Optional<CommerceConnection> commerceConnection = commerceConnectionSupplier.findConnection(content);

    if (!commerceConnection.isPresent()) {
      LOG.debug("Commerce connection not available, will not invalidate references.");
      return;
    }

    commerceCacheInvalidationSource.invalidateReferences(newHashSet(allReferences));
  }

  @NonNull
  @SuppressWarnings("unchecked")
  Set<Content> resolveAllSpinnerPictures(@NonNull Map<String, Object> oldProperties,
                                         @NonNull Map<String, Object> newProperties) {
    // First show if the sequence property has changed, if it changed
    // use its pictures.
    if (newProperties.containsKey(SEQUENCE_SPINNER_PROPERTY)) {
      Object newPictures = newProperties.get(SEQUENCE_SPINNER_PROPERTY);
      return new HashSet<>((Collection) newPictures);
    }

    // If sequence property has not changed, get the existing pictures
    // directly from the old content.
    Object oldPictures = oldProperties.get(SEQUENCE_SPINNER_PROPERTY);
    if (oldPictures instanceof Collection) {
      return new HashSet<>((Collection) oldPictures);
    }

    return emptySet();
  }

  private void rewriteOriginalExternalReferences(@NonNull Map<String, Object> oldProperties,
                                                 @NonNull Map<String, Object> newProperties) {
    Set<Content> allPictures = resolveAllSpinnerPictures(oldProperties, newProperties);
    Set<String> allReferences = buildOriginalCommerceReferenceList(allPictures);

    Struct localSettings = evaluateNewSettingsFoundation(oldProperties, newProperties);
    writeNewStruct(newProperties, allReferences, localSettings);
  }

  @NonNull
  Struct evaluateNewSettingsFoundation(@NonNull Map<String, Object> oldProperties,
                                       @NonNull Map<String, Object> newProperties) {
    Object newLocalSettings = newProperties.get(NAME_LOCAL_SETTINGS);
    Object oldLocalSettings = oldProperties.get(NAME_LOCAL_SETTINGS);

    if (newLocalSettings != null) {
      return (Struct) newLocalSettings;
    }

    if (oldLocalSettings != null) {
      return (Struct) oldLocalSettings;
    }

    return emptyStruct();
  }

  private void writeNewStruct(@NonNull Map<String, Object> newProperties, @NonNull Set<String> allReferences,
                              @NonNull Struct localSettings) {
    boolean isInherited = assetReadSettingsHelper.readInheritedField(localSettingsToMap(localSettings));

    Struct newLocalSettingsStruct = assetWriteSettingsHelper.createNewSettingsStructWithReferences(
            localSettingsToMap(localSettings), new ArrayList<>(allReferences), isInherited);

    newProperties.put(NAME_LOCAL_SETTINGS, newLocalSettingsStruct);
  }

  @NonNull
  private static Map<String, Object> localSettingsToMap(@NonNull Struct localSettings) {
    Map<String, Object> stringObjectHashMap = new HashMap<>();
    stringObjectHashMap.put(NAME_LOCAL_SETTINGS, localSettings);
    return stringObjectHashMap;
  }

  @NonNull
  private static Set<String> buildOriginalCommerceReferenceList(@NonNull Set<Content> pictures) {
    return pictures.stream()
            .map(CommerceReferenceHelper::getExternalReferences)
            .flatMap(List::stream)
            .collect(toSet());
  }

  @NonNull
  private Struct emptyStruct() {
    return contentRepository.getConnection().getStructService().createStructBuilder().build();
  }
}
