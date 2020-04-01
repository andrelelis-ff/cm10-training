package com.coremedia.blueprint.studio.rest.taxonomies;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation class for server side taxonomy settings.
 */
public class TaxonomySettingsRepresentation {
  private List<String> adminGroups = new ArrayList<>();

  public List<String> getAdminGroups() {
    return adminGroups;
  }
}
