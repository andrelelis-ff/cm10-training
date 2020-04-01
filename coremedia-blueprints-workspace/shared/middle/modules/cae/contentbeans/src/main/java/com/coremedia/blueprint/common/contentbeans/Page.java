package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cap.common.Blob;

import java.util.List;

/**
 * A Page is a {@link AbstractPage} that represents a complete page including its own CSS, JavaScript
 * and favicon.
 *
 * @cm.template.api
 */
public interface Page extends AbstractPage {
  /**
   * Returns the css contents for this page.
   *
   * @return the css contents for this page
   * @cm.template.api
   */
  List<?> getCss();

  /**
   * Returns the ie specific css contents for this page.
   *
   * @return the css contents for this page
   * @cm.template.api
   */
  List<?> getInternetExplorerCss();

  /**
   * Returns the JavaScript contents for this page.
   *
   * @return the JavaScript contents for this page
   * @cm.template.api
   */
  List<?> getJavaScript();

  /**
   * Returns the JavaScript contents for this page that are to be included in
   * the HTML head.
   *
   * @return the JavaScript contents for the head of this page
   * @cm.template.api
   */
  List<?> getHeadJavaScript();

  /**
   * Returns the ie specific JavaScript contents for this page.
   *
   * @return the ie specific JavaScript contents for this page
   * @cm.template.api
   */
  List<?> getInternetExplorerJavaScript();

  /**
   * Returns a favicon for this page. May be null.
   *
   * @cm.template.api
   */
  Blob getFavicon();

  /**
   * Return the first navigation context within the navigation hierarchy which is an instance of CMContext
   *
   * @cm.template.api
   */
  CMContext getContext();
}
