package com.coremedia.blueprint.taxonomies.semantic;


import com.coremedia.blueprint.taxonomies.Taxonomy;

/**
 * The interface that has to be implemented by semantic strategies.
 */
public interface SemanticStrategy {

  /**
   * Returns a result instance that contains taxonomy keywords, so that a suggestion
   * of keywords is made that can be added to an existing keyword collection.
   *
   * @param capId The capId that should be used to find suggestions for (this is not the node ref!!!)
   * @return The result items that are suggested to add to the node.
   */
  Suggestions suggestions(Taxonomy<?> taxonomy, String capId);

  /**
   * Returns the service id of this service.
   *
   * @return id
   */
  String getServiceId();
}
