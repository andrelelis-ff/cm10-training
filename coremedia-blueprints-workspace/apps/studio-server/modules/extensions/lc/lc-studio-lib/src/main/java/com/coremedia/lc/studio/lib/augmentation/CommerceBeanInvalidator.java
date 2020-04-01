package com.coremedia.lc.studio.lib.augmentation;

import com.coremedia.cap.util.ContentStringPropertyValueChangeEvent;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.ecommerce.studio.rest.cache.CommerceCacheInvalidationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper.parseCommerceId;
import static java.util.Collections.singleton;

@Component
class CommerceBeanInvalidator implements ApplicationListener<ContentStringPropertyValueChangeEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(CommerceBeanInvalidator.class);

  // Naming convention only, not bound to particular doctypes.
  private static final String EXTERNAL_ID = "externalId";

  private final CommerceCacheInvalidationSource commerceInvalidationSource;

  CommerceBeanInvalidator(CommerceCacheInvalidationSource commerceInvalidationSource) {
    this.commerceInvalidationSource = commerceInvalidationSource;
  }

  @Override
  public void onApplicationEvent(ContentStringPropertyValueChangeEvent event) {
    if (isApplicable(event)) {
      String propertyValue = event.getValue();
      String commerceBeanUri = parseCommerceId(propertyValue)
              .flatMap(commerceInvalidationSource::toCommerceBeanUri).orElse(null);
      if (commerceBeanUri != null) {
        commerceInvalidationSource.addInvalidations(singleton(commerceBeanUri));
      } else {
        LOG.debug("Unable to create invalidation for commerce bean reference '{}'", propertyValue);
      }
    }
  }

  private static boolean isApplicable(ContentStringPropertyValueChangeEvent event) {
    CapPropertyDescriptor observedProperty = event.getObservedProperty();
    return observedProperty!=null && EXTERNAL_ID.equals(observedProperty.getName());
  }
}
