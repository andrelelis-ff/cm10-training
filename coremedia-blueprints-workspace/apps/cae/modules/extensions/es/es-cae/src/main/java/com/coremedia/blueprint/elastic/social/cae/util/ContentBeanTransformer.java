package com.coremedia.blueprint.elastic.social.cae.util;


import com.coremedia.blueprint.base.elastic.social.common.ContributionTargetTransformer;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.elastic.core.cms.ContentWithSite;
import com.coremedia.objectserver.beans.ContentBean;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

@Named
class ContentBeanTransformer implements ContributionTargetTransformer<ContentBean, ContentWithSite> {
  @Inject
  private SitesService sitesService;

  @NonNull
  private Content getContent(@NonNull ContentBean target) {
    return target.getContent();
  }

  @Nullable
  private Site getSiteForContent(@NonNull Content content) {
    return sitesService.getContentSiteAspect(content).getSite();
  }

  @NonNull
  @Override
  public ContentWithSite transform(@NonNull ContentBean target) {
    final Content content = getContent(target);
    return new ContentWithSite(content, getSiteForContent(content));
  }

  @Nullable
  @Override
  public Site getSite(@NonNull ContentBean target) {
    return getSiteForContent(target.getContent());
  }

  @Override
  @NonNull
  public Class<ContentBean> getType() {
    return ContentBean.class;
  }
}