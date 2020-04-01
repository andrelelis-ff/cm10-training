package com.coremedia.blueprint.cae.web.i18n;

import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.user.User;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ResourceBundle;

public interface PageResourceBundleFactory {
  /**
   * Returns the ResourceBundle for the page.
   * <p>
   * Implementations may consider the developer's work in progress resource bundles.
   */
  ResourceBundle resourceBundle(Page page, @Nullable User developer);

  /**
   * Returns the ResourceBundle for the Navigation.
   * <p>
   * Implementations may consider the developer's work in progress resource bundles.
   */
  ResourceBundle resourceBundle(Navigation navigation, @Nullable User developer);
}
