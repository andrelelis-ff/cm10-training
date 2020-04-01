package com.coremedia.livecontext.context;

import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

@DefaultAnnotation(NonNull.class)
public interface ResolveContextStrategy {

  Optional<LiveContextNavigation> resolveContext(Site site, CommerceBean commerceBean);
}
