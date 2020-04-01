package com.coremedia.blueprint.common.layout;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.navigation.Linkable;

import java.util.List;

/**
 * Represents one page of a paginated collection
 * <p>
 * Do not mistake the term "page" within this interface with the Blueprint's
 * Page concept.
 *
 * @since 1901
 * @cm.template.api
 */
public interface Pagination {
  /**
   * Returns the number of this page
   * <p>
   * Counting starts with 0.
   *
   * @cm.template.api
   */
  int getPageNum();

  /**
   * Returns the total number of pages of the pagination
   *
   * @cm.template.api
   */
  long getNumberOfPages();

  /**
   * Return the number of items per page
   *
   * @cm.template.api
   */
  int getItemsPerPage();

  /**
   * Returns the items of this page
   *
   * @cm.template.api
   */
  List<Linkable> getItems();

  /**
   * Target for link building
   * <p>
   * Templates build links on Pagination objects directly, but the
   * implementation needs a "persistent handle" to refer to.
   */
  CMLinkable linkable();
}
