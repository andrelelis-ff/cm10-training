package com.coremedia.livecontext.handler.util;

import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Locale;
import java.util.Optional;

@DefaultAnnotation(NonNull.class)
public interface LiveContextSiteResolver extends SiteResolver {

  Optional<Site> findSiteFor(FragmentParameters fragmentParameters);

  Optional<Site> findSiteFor(String storeId, Locale locale);
}
