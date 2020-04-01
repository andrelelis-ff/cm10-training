package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.layout.PageGridService;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * A LiveContextNavigation which is backed by a CMExternalChannel content
 * in the CMS repository.
 */
public class LiveContextExternalChannelImpl extends CMExternalChannelBase implements LiveContextExternalChannel {

  private static final Logger LOG = LoggerFactory.getLogger(LiveContextExternalChannelImpl.class);

  private LiveContextNavigationFactory liveContextNavigationFactory;
  private Site site;
  private PageGridService pdpPageGridService;
  private CommerceConnectionSupplier commerceConnectionSupplier;
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Override
  public Category getCategory() {
    Optional<CommerceConnection> connectionForContent = commerceConnectionSupplier.findConnection(this.getContent());

    if (!connectionForContent.isPresent()) {
      return null;
    }

    CommerceConnection connection = connectionForContent.get();

    Content content = getContent();
    String externalId = getExternalId();

    StoreContext storeContext = connection.getStoreContext();

    Optional<CommerceId> commerceIdOptional = CommerceIdParserHelper.parseCommerceId(externalId);
    if (!commerceIdOptional.isPresent()) {
      logHintAboutCategoryInWorkspace(content, externalId);
      return null;
    }

    Category category = connection.getCatalogService()
            .findCategoryById(commerceIdOptional.get(), storeContext);

    if (category == null) {
      logHintAboutCategoryInWorkspace(content, externalId);
    }

    return category;
  }

  private static void logHintAboutCategoryInWorkspace(Content content, String externalId) {
    LOG.debug("Content #{}: No category found for externalId:{} - maybe the category only exists in a workspace?",
            content, externalId);
  }

  @NonNull
  @Override
  public Site getSite() {
    if (site == null) {
      site = getSitesService().getContentSiteAspect(getContent()).findSite()
              .orElseThrow(() -> new IllegalStateException(
                      "A " + LiveContextExternalChannelImpl.class.getName() + " must belong to a site but content["
                              + getContentId() + "] does not. "));
    }

    return site;
  }

  @Override
  @NonNull
  public String getExternalId() {
    String externalId = getContent().getString(EXTERNAL_ID);
    return externalId == null ? "" : externalId.trim();
  }

  @Override
  protected List<Linkable> getExternalChildren(Site site) {
    if (isCommerceChildrenSelected()) {
      Optional<CommerceConnection> commerceConnection = commerceConnectionInitializer.findConnectionForSite(site);
      if (commerceConnection.isPresent()) {
        CommerceConnection connection = commerceConnection.get();
        StoreContext storeContext = connection.getStoreContext();
        CatalogService catalogService = connection.getCatalogService();

        return getCommerceChildrenIds().stream()
                .map(CommerceIdParserHelper::parseCommerceIdOrThrow)
                .map(commerceId -> catalogService.findCategoryById(commerceId, storeContext))
                .filter(Objects::nonNull)
                .map(subCategory -> liveContextNavigationFactory.createNavigation(subCategory, site))
                .collect(toList());
      }
    }

    // in all other cases (especially in automatic mode) we ask the treeRelation...
    return new ArrayList<>(treeRelation.getChildrenOf(this));
  }

  @Override
  public boolean isCatalogRoot() {
    Category category = getCategory();
    return category != null && category.isRoot();
  }

  @Override
  public PageGrid getPdpPagegrid() {
    return pdpPageGridService.getContentBackedPageGrid(this);
  }

  @Required
  public void setLiveContextNavigationFactory(LiveContextNavigationFactory liveContextNavigationFactory) {
    this.liveContextNavigationFactory = liveContextNavigationFactory;
  }

  @Required
  public void setPdpPageGridService(PageGridService pdpPageGridService) {
    this.pdpPageGridService = pdpPageGridService;
  }

  @Required
  public void setCommerceConnectionSupplier(CommerceConnectionSupplier commerceConnectionSupplier) {
    this.commerceConnectionSupplier = commerceConnectionSupplier;
  }

  @Required
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(LiveContextExternalChannel.class)
            .add("contentId", getContent().getId())
            .add("externalId", getExternalId())
            .toString();
  }
}
