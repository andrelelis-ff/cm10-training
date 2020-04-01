package com.coremedia.livecontext.fragment.links.transformers.resolvers.seo;

import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMObject;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface SeoSegmentBuilder {
  @NonNull
  String asSeoSegment(CMNavigation navigation, CMObject target);
}
