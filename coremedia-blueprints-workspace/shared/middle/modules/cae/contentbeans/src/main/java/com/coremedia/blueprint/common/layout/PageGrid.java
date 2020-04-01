package com.coremedia.blueprint.common.layout;

import com.coremedia.cap.content.Content;

import java.util.List;

/**
 * A PageGrid consists of placements arranged like an HTML table
 *
 * @cm.template.api
 */
public interface PageGrid {

  /**
   * Returns the pagegrid's rows
   *
   * @cm.template.api
   */
  List<PageGridRow> getRows();

  /**
   * Returns the number of columns of the pagegrid
   */
  int getNumcols();

  /**
   * @return name of the css class of the pagegrid
   * @cm.template.api
   */
  String getCssClassName();

  List<?> getMainItems();

  /**
   * Returns the placement which section document has the given name.
   *
   * @param name The name of the placement.
   * @cm.template.api
   */
  PageGridPlacement getPlacementForName(String name);

  /**
   * Returns the layout settings document that defines the structure of the page grid
   */
  Content getLayout();

}
