package com.coremedia.blueprint.localization;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface BundleResolver {
  @Nullable Struct resolveBundle(@NonNull Content bundle);
}
