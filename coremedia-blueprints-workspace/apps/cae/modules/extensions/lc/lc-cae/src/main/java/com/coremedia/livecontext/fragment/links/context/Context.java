package com.coremedia.livecontext.fragment.links.context;

import java.util.Collection;

/**
 * A collection of named context values
 */
public interface Context {

  /**
   * @return The names of all available contexts
   */
  Collection<String> getContextNames();

  /**
   * Provides a context value
   * @param name The context name
   * @return The context value or null if no such value exists
   */
  Object get(String name);

  /**
   * Set a context value
   * @param name The context name
   * @param value The context value
   */
  void put(String name, Object value);

  /**
   * Remove a context value
   * @param name
   */
  void remove(String name);
}
