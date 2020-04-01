package com.coremedia.blueprint.osm;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cae.aspect.AspectAggregatorAware;

/**
 * @deprecated since 1907.1; Currently unused. If needed again, reimplement it as extension.
 */
@Deprecated
public interface OpenStreetMapAspect extends AspectAggregatorAware, Aspect<CMTeasable> {

  boolean isEnabled();
}
