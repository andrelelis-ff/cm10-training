package com.coremedia.blueprint.common.services.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractValidator<T> implements Validator<T> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractValidator.class);

  // --- Validator --------------------------------------------------

  /**
   * @param source The objects to be filtered
   * @return the filtered objects
   */
  @Override
  public <R extends T> List<R> filterList(List<R> source) {
    List<R> linkables = internallyFilterList(source);
    return new ArrayList<>(linkables);
  }

  /**
   * @param source A single object to be tested
   * @return true if valid, false otherwise
   */
  @Override
  public boolean validate(T source) {
    List<? extends T> results = filterList(Collections.singletonList(source));
    return !results.isEmpty();
  }

  // --- abstract ---------------------------------------------------

  protected abstract Predicate<T> createPredicate();

  protected void addCustomDependencies(List<? extends T> result) {
    LOG.debug("The default implementation is not adding any dependencies");
  }

  // --- internal ---------------------------------------------------

  /**
   * internal method which will be called by any implementing validation service
   *
   * @param allItems the items to be filtered
   * @return the filtered objects or null
   */
  private <R extends T> List<R> internallyFilterList(List<R> allItems) {
    //fist collect all valid items
    LOG.debug("Before selecting the list contained {} items ({})", allItems.size(), allItems);

    List<R> validItems = new ArrayList<>();
    Predicate<T> predicate = createPredicate();
    for (R item : allItems) {
      if (supports(item.getClass())) {
        try {
          if (predicate.test(item)) {
            validItems.add(item);
          }
        } catch (Exception e) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("caught exception while validating item '{}'", item, e);
          }
          else {
            LOG.info("caught exception while validating item '{}': {}", item, e.getMessage());
          }
        }
      } else {
        validItems.add(item);
      }
    }
    LOG.debug("Afterwards {} items ({})", validItems.size(), validItems);
    addCustomDependencies(allItems);
    return validItems;
  }
}
