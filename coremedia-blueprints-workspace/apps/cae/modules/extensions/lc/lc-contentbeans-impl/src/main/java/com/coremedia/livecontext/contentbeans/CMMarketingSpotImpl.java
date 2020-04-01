package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.cae.contentbeans.CMDynamicListImpl;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceObject;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpot;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class CMMarketingSpotImpl extends CMDynamicListImpl implements CMMarketingSpot {

  public static final String EXTERNAL_ID = "externalId";

  private LiveContextNavigationFactory liveContextNavigationFactory;

  CommerceConnectionSupplier commerceConnectionSupplier;

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.livecontext.contentbeans.CMMarketingSpot} objects
   */
  @Override
  public CMMarketingSpot getMaster() {
    return (CMMarketingSpot) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMMarketingSpot> getVariantsByLocale() {
    return getVariantsByLocale(CMMarketingSpot.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMMarketingSpot> getLocalizations() {
    return (Collection<? extends CMMarketingSpot>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMMarketingSpot>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMMarketingSpot>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMMarketingSpot>> getAspects() {
    return (List<? extends Aspect<? extends CMMarketingSpot>>) super.getAspects();
  }

  @Override
  public String getExternalId() {
    return getContent().getString(EXTERNAL_ID);
  }

  @Override
  public List<CommerceObject> getItems() {
    //since the commerce system renders marketing spots, we do not need to return the items anymore
    return List.of(getMarketingSpot());
  }

  /**
   * @return the value of the document property "teaserTitle".
   * If it is empty then fallback to the document property "title".
   * If it is still empty then fallback to the name of the marketing spot.
   */
  @Override
  public String getTeaserTitle() {
    String teaserTitle = super.getTeaserTitle();
    if (isBlank(teaserTitle)) {
      MarketingSpot marketingSpot = getMarketingSpot();
      if (marketingSpot != null && marketingSpot.getName() != null) {
        teaserTitle = marketingSpot.getName();
      }
    }
    return teaserTitle;
  }

  @Required
  public void setLiveContextNavigationFactory(LiveContextNavigationFactory liveContextNavigationFactory) {
    this.liveContextNavigationFactory = liveContextNavigationFactory;
  }

  @Required
  public void setCommerceConnectionSupplier(CommerceConnectionSupplier commerceConnectionSupplier) {
    this.commerceConnectionSupplier = commerceConnectionSupplier;
  }

  @Override
  @Nullable
  public MarketingSpot getMarketingSpot() {
    return commerceConnectionSupplier.findConnection(getContent())
            .map(con -> doGetMarketingSpot(con, getExternalId()))
            .orElse(null);
  }

  @Nullable
  private MarketingSpot doGetMarketingSpot(CommerceConnection connection, String marketingSpotId) {
    StoreContext storeContext = connection.getStoreContext();
    CommerceId commerceId = CommerceIdParserHelper.parseCommerceIdOrThrow(marketingSpotId);

    return connection.getMarketingSpotService()
            .map(spotService -> spotService.findMarketingSpotById(commerceId, storeContext))
            .orElse(null);
  }
}
