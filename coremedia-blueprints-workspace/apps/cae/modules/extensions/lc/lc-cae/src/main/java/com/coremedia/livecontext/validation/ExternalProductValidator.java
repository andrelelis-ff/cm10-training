package com.coremedia.livecontext.validation;

import com.coremedia.blueprint.common.services.validation.AbstractValidator;
import com.coremedia.livecontext.contentbeans.CMExternalProduct;
import com.coremedia.livecontext.contentbeans.LiveContextExternalProduct;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.function.Predicate;

/**
 * {@link com.coremedia.livecontext.contentbeans.CMExternalProduct augmented Product} may link to a product that
 * does not existent on the remote commerce system anymore or does only exist in a workspace which is not currently selected.
 * This {@link com.coremedia.blueprint.common.services.validation.Validator validator} can be used to
 * {@link com.coremedia.blueprint.common.services.validation.ValidationService#filterList(java.util.List) filter out}
 * those augmented products silently, so that the rendering does not break.
 */
public class ExternalProductValidator extends AbstractValidator<LiveContextExternalProduct> {
  private static final Logger LOG = LoggerFactory.getLogger(ExternalProductValidator.class);

  @Override
  protected Predicate<LiveContextExternalProduct> createPredicate() {
    return new ExternalReferencePredicate();
  }

  private static class ExternalReferencePredicate implements Predicate<LiveContextExternalProduct> {
    @Override
    public boolean test(@Nullable LiveContextExternalProduct externalProduct) {
      if (externalProduct != null) {
        String externalId = externalProduct.getExternalId();
        if (!StringUtils.hasText(externalId)) {
          LOG.info("external id property of {} is empty", externalProduct);
          return false;
        }
        return hasValidReference(externalProduct);
      }
      return true;
    }
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return CMExternalProduct.class.isAssignableFrom(clazz);
  }

  private static boolean hasValidReference(@NonNull LiveContextExternalProduct externalProduct) {
    String externalId = externalProduct.getExternalId();
    try {
      Product product = externalProduct.getProduct();
      if (product == null) {
        return false;
      }
      // Product may have been initialized lazily. Try to load product in order to see if anything goes wrong.
      product.load();
      LOG.debug("E-Commerce product for {} with external id '{}' is '{}'", externalProduct, externalId, product);
      return true;
    } catch (CommerceException e) {
      LOG.debug("Caught exception while validating external id '{}' of {}.", externalId, externalProduct, e);
      return false;
    }
  }
}
