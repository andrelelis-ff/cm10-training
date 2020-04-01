package com.coremedia.blueprint.coderesources;

/**
 * Represents the code resources of a page for a certain type, CSS or
 * JavaScript.
 * <p>
 * The code resources of a page are aggregated from code properties of the
 * page's channel and a theme which may be inherited from a parent channel.
 */
public interface CodeResources {
  /**
   * Get a CodeResourcesModel for the given html mode (head, body or ie).
   *
   * @param htmlMode one of the MODE_ constants in CodeResourcesModel
   */
  CodeResourcesModel getModel(String htmlMode);
}
