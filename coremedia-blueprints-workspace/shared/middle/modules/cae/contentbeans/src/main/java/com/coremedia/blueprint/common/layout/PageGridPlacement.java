package com.coremedia.blueprint.common.layout;

import com.coremedia.blueprint.common.navigation.HasViewTypeName;
import com.coremedia.blueprint.common.navigation.Linkable;

import java.util.List;
import java.util.Map;

/**
 * A PageGridPlacement is a part of a whole PageGrid representing for example the sidebar or main column
 *
 * @cm.template.api
 */
public interface PageGridPlacement extends HasViewTypeName, Container<Linkable> {
  /**
   * Retrieves the items of this PageGridPlacement
   */
  List<? extends Linkable> getItems();

  /**
   * <p>
   * Returns the logical name of this placement.
   * </p>
   * <p>
   * "main" is magic for the current content.
   * </p>
   *
   * @cm.template.api
   */
  String getName();

  /**
   * Return the "virtual" property name of this placement (used for Studio preview integration).
   */
  String getPropertyName();

  /**
   * Returns the absolute position in the row.
   * <p>
   * Count starts with 1.
   */
  int getCol();

  /**
   * <p>
   * Returns the colspan of this placement.
   * </p>
   * <p>
   * The colspan concept is motivated by the HTML table model,
   * the Blueprint templates map this value to CSS styles, though.
   * </p>
   *
   * @cm.template.api
   */
  int getColspan();

  /**
   * Returns the relative (percentage) width of this placement.
   *
   * @cm.template.api
   */
  int getWidth();

  /**
   * Returns the number of the row of this placement.
   */
  int getNumCols();

  /**
   * Returns if the Placement is editable.
   */
  boolean isEditable();

  /**
   * Returns additional properties stored in the pagegrid config
   */
  Map<String, Object> getAdditionalProperties();

}

