package com.coremedia.blueprint.common.layout;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

/**
 * A PageGridRow contains the placements of a row,
 * without row spanning placements from upper rows.
 *
 * @cm.template.api
 */
@DefaultAnnotation(NonNull.class)
public interface PageGridRow {
  /**
   * Returns the placements of this row.
   * @return the placements of this row.
   */
  List<PageGridPlacement> getPlacements();

  /**
   * Returns the placements of this row that are editable.
   * @return the placements of this row that are editable.
   */
  List<PageGridPlacement> getEditablePlacements();

  /**
   * Checks if one of the placements of this row has at least one item
   * @return true if one of the placements of this row has at least one item
   */
  boolean getHasItems();
}
