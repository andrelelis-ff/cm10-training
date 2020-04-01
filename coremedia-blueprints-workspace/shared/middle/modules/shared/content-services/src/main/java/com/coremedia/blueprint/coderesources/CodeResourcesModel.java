package com.coremedia.blueprint.coderesources;

import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;

/**
 * Code resources for a page.
 * <p>
 * The code resources of a page are aggregated from code properties of the
 * page's channel and a theme which may be inherited from a parent channel.
 * <p>
 * A particular CodeResourcesModel instance determines
 * <ul>
 *   <li>the code type, CSS or JavaScript</li>
 *   <li>the respective subset of the code resources to be included in the html head, body or ie specific</li>
 * </ul>
 */
public interface CodeResourcesModel {
  String MODE_HEAD = "head";
  String MODE_BODY = "body";
  String MODE_IE = "ie";

  String TYPE_CSS = "css";
  String TYPE_JS = "js";


  // --- for link building ------------------------------------------

  /**
   * @return one of the TYPE_* values
   */
  String getCodeType();

  /**
   * Where to include this code in HTML.
   *
   * @return one of the MODE_* values
   */
  String getHtmlMode();

  /**
   * @return a unique hash calculated for all resources
   */
  String getETag();

  /**
   * @return the channel that carries the theme
   */
  Content getChannelWithTheme();

  /**
   * @return the channel that carries the code
   */
  Content getChannelWithCode();


  // --- for rendering ----------------------------------------------

  /**
   * The returned code resources are ordered as follows:
   * <ol>
   *   <li>external resources</li>
   *   <li>non-IE-excludes resources</li>
   *   <li>IE-excludes</li>
   * </ol>
   * The list elements are mostly CMAbstractCode Content objects.  One item
   * may be a list of Content objects, which means that these resources should
   * be rendered as merged resource.
   * <p>
   * Background: We need to identify the merged resource by a link.
   * (S. CodeResourceHandler) The limitation to one merged resource per
   * CodeResourcesModel enables us to identify it 1:1 by the model.  It would
   * be nicer to provide the merged resource as a dedicated feature, but the
   * order of this list matters, so we cannot tear it out of this context.
   *
   * @return a list of resources.
   */
  @NonNull
  List<?> getLinkTargetList();
}
