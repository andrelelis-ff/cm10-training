package com.coremedia.blueprint.localization;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class ContentBundleResolver implements BundleResolver {
  private static final String LOCALIZATIONS = "localizations";

  /**
   * Returns the "localizations" struct of the given content.
   * <p>
   * The content is supposed to be a CMResourceBundle.
   */
  @Override
  @Nullable
  public Struct resolveBundle(@NonNull Content bundle) {
    return bundle.getStruct(LOCALIZATIONS);
  }
}
