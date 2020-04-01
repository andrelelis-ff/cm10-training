package com.coremedia.blueprint.taxonomies.cycleprevention;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface TaxonomyCycleValidator {

  /**
   * Checks if the given taxonomy has a cyclic dependency.
   *
   * @param tax The taxonomy to check a cycle for.
   * @param contentType type of the taxonomy
   * @return True, if the taxonomy contains a cyclic child relation,
   *        false if no cycle is detected or given content is not a taxonomy
   */
  boolean isCyclic(@NonNull Content tax, ContentType contentType);
}
