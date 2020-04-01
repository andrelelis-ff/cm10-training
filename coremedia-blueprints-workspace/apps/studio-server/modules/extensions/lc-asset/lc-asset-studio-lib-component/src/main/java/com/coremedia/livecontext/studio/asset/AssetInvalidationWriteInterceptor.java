package com.coremedia.livecontext.studio.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.ecommerce.studio.rest.cache.CommerceCacheInvalidationSource;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * {@link com.coremedia.rest.cap.intercept.ContentWriteInterceptor}
 * which delegates the invalidation of CommerceRemote Beans to the write post processor
 * if commerce reference list of the given asset or the image data and the transformation meta data are changed.
 * Only the difference between the old and new list is invalidated
 * The difference is only accessible before the write operation.
 */
public class AssetInvalidationWriteInterceptor extends ContentWriteInterceptorBase {

  private static final Logger LOG = LoggerFactory.getLogger(AssetInvalidationWriteInterceptor.class);

  @VisibleForTesting
  static final String STRUCT_PROPERTY_NAME = "localSettings";

  private final CommerceConnectionSupplier commerceConnectionSupplier;
  private final CommerceCacheInvalidationSource commerceCacheInvalidationSource;

  AssetInvalidationWriteInterceptor(CommerceConnectionSupplier commerceConnectionSupplier,
                                    CommerceCacheInvalidationSource commerceCacheInvalidationSource) {
    this.commerceConnectionSupplier = commerceConnectionSupplier;
    this.commerceCacheInvalidationSource = commerceCacheInvalidationSource;
  }

  @Override
  public void intercept(ContentWriteRequest request) {
    Content content = request.getEntity();
    Map<String, Object> properties = request.getProperties();

    if (content != null && properties != null) {
      invalidate(content, properties);
    }
  }

  private void invalidate(@NonNull Content content, @NonNull Map<String, Object> properties) {
    if (!properties.containsKey(STRUCT_PROPERTY_NAME)) {
      return;
    }

    Struct localSettings = (Struct) properties.get(STRUCT_PROPERTY_NAME);

    Set<String> references = getInvalidReferences(content, localSettings);

    Optional<CommerceConnection> commerceConnection = commerceConnectionSupplier.findConnection(content);

    if (!commerceConnection.isPresent()) {
      LOG.debug("Commerce connection not available, will not invalidate references.");
      return;
    }

    //we delegate the invaliations to the write post processor
    //as the write interceptor has too old sequence number
    commerceCacheInvalidationSource.invalidateReferences(references);
  }

  @NonNull
  private static Set<String> getInvalidReferences(Content content, Struct localSettings) {
    //the list of references to the catalog objects might have been changed
    //let's calculate the diff between the old and new lists
    List<String> newIds = CommerceReferenceHelper.getExternalReferences(localSettings);
    List<String> oldIds = CommerceReferenceHelper.getExternalReferences(content);

    Collection<String> invalidationsCollection = CollectionUtils.disjunction(newIds, oldIds);
    Set<String> invalidations = newHashSet(invalidationsCollection);

    //if the references aren't changed...
    if (invalidations.isEmpty()) {
      //... let's check if other properties of the local settings have changed
      if (!localSettings.equals(content.getStruct(STRUCT_PROPERTY_NAME))) {
        //if so all references have to be invalidated
        invalidations.addAll(oldIds);
      }
    }

    return invalidations;
  }
}
