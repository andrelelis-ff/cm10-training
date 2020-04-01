package com.coremedia.blueprint.common.services.validation;

import java.util.List;

/**
 * The interface for all validation services. A validation service can be used to check if a content item is valid.
 *
 * Whenever validation is used, it is important to know that validation might break caching dependencies.
 * If this happens, a cached value might never be invalidated.
 *
 * For example a class might look like this:
 *
 * <pre>
 *   public class MyClass(){
 *     private List<String> foo;
 *     private ValidationService<String> validationService;
 *
 *     public List<String> getFoo() {
 *       return validationService.filterList(getFooUnfiltered());
 *     }
 *     public List<String> getFooUnfiltered() {
 *       return foo;
 *     }
 *   }
 * </pre>
 *
 * For this class, caching should NOT be configured to cache getFoo(). Instead, configure it to cache getFooUnfiltered().
 *
 */
public interface ValidationService<S> {
  /**
   * Filters the given list. Make sure that you don't use this in a cached method, because filtering might break
   * caching dependencies.
   *
   * @param source   The objects to be filtered
   * @return the filtered objects or null
   */
  List<? extends S> filterList(List<? extends S> source);

  /**
   * @param source   A single object to be tested
   * @return true if valid, false otherwise
   */
  boolean validate(S source);
}