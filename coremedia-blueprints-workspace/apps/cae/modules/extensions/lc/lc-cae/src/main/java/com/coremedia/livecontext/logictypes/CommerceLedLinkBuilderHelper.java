package com.coremedia.livecontext.logictypes;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;
import com.coremedia.livecontext.fragment.links.transformers.resolvers.seo.SeoSegmentBuilder;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Optional;

/**
 * Link Builder helper for CMChannels that are supposed to link to the commerce system.
 */
public class CommerceLedLinkBuilderHelper {

  private static final String DEFAULT_CM_CONTENT_URL_KEYWORD = "cm";
  private static final String LIVECONTEXT_POLICY_COMMERCE_PAGE_LINKS = "livecontext.policy.commerce-page-links";
  private static final String LIVECONTEXT_POLICY_COMMERCE_MICROSITE_LINKS = "livecontext.policy.commerce-microsite-links";

  private String contentURLKeyword = DEFAULT_CM_CONTENT_URL_KEYWORD;

  private SettingsService settingsService;

  private SeoSegmentBuilder seoSegmentBuilder;

  public boolean isCommerceLedChannel(CMObject cmObject) {
    return (cmObject instanceof CMChannel) && (!(cmObject instanceof CMExternalChannel)) && isCommerceLed((CMChannel) cmObject);
  }

  /**
   * Return true if the given channel link shall be rendered as a WCS/Commerce link.
   *
   * @param channel given channel
   * @return true if the given channel link shall be rendered as Commerce link.
   */
  private boolean isCommerceLed(@NonNull CMChannel channel) {
    //noinspection ConstantConditions
    return getBooleanSetting(LIVECONTEXT_POLICY_COMMERCE_PAGE_LINKS, channel).orElse(false)
            || getBooleanSetting(LIVECONTEXT_POLICY_COMMERCE_MICROSITE_LINKS, channel).orElse(false);
  }

  @NonNull
  private Optional<Boolean> getBooleanSetting(@NonNull String name, @NonNull Object bean) {
    return settingsService.getSetting(name, Boolean.class, bean);
  }

  public String getSeoSegmentForChannel(CMChannel channel) {
    return seoSegmentBuilder.asSeoSegment(channel, channel);
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  public String getContentURLKeyword() {
    return contentURLKeyword;
  }

  public void setContentURLKeyword(String contentURLKeyword) {
    this.contentURLKeyword = contentURLKeyword;
  }

  @Required
  public void setSeoSegmentBuilder(SeoSegmentBuilder seoSegmentBuilder) {
    this.seoSegmentBuilder = seoSegmentBuilder;
  }
}
