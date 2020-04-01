package com.coremedia.livecontext.fragment.links.transformers.resolvers;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.handlers.PreviewHandler;
import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.layout.DynamizableContainer;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.commercebeans.CategoryInSite;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.contentbeans.CMExternalPage;
import com.coremedia.livecontext.contentbeans.CMProductTeaser;
import com.coremedia.livecontext.contentbeans.LiveContextExternalProduct;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.link.LinkService;
import com.coremedia.livecontext.ecommerce.link.QueryParam;
import com.coremedia.livecontext.ecommerce.link.StorefrontRef;
import com.coremedia.livecontext.fragment.links.transformers.resolvers.seo.ExternalSeoSegmentBuilder;
import com.coremedia.objectserver.beans.ContentBean;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.link.UrlUtil.getQueryParamList;
import static com.coremedia.livecontext.fragment.links.transformers.resolvers.AbstractLiveContextLinkResolver.deabsolutizeLink;

@Component
@DefaultAnnotation(NonNull.class)
public class CommerceLinkResolver implements LiveContextLinkResolver {

  private static final Logger LOG = LoggerFactory.getLogger(CommerceLinkResolver.class);

  private final ExternalSeoSegmentBuilder seoSegmentBuilder;

  public CommerceLinkResolver(ExternalSeoSegmentBuilder seoSegmentBuilder) {
    this.seoSegmentBuilder = seoSegmentBuilder;
  }

  @Override
  public Optional<String> resolveUrl(String source, Object bean, String variant, CMNavigation navigation,
                                     HttpServletRequest request) {
    try {
      String link = buildLink(source, bean, navigation, request);
      return Optional.ofNullable(link);
    } catch (Exception e) {
      LOG.error("Error creating Salesforce intermediate link representation for '{}'.", debug(bean), e);
      return Optional.empty();
    }
  }

  @Nullable
  private String buildLink(String source, Object bean, CMNavigation navigation, HttpServletRequest request) {
    StoreContext storeContext = CurrentStoreContext.find().orElse(null);
    if (storeContext == null) {
      return null;
    }

    LinkService linkService = storeContext.getConnection().getLinkService().orElse(null);
    if (linkService == null) {
      return null;
    }

    return buildLink(source, bean, navigation, linkService, storeContext, request)
            .map(StorefrontRef::toLink)
            .orElse(null);
  }

  @SuppressWarnings({"IfStatementWithTooManyBranches", "OverlyComplexMethod"})
  private Optional<StorefrontRef> buildLink(String source, Object bean, CMNavigation navigation,
                                            LinkService linkService, StoreContext storeContext,
                                            HttpServletRequest request) {
    List<QueryParam> linkParameters = getQueryParamList(source);

    if (bean instanceof CMProductTeaser) {
      CMProductTeaser productTeaser = (CMProductTeaser) bean;
      Product product = productTeaser.getProduct();

      if (product == null) {
        LOG.debug("Cannot generate link for product teaser '{}'; product is not set.", productTeaser.getContentId());
        return Optional.empty();
      }

      return Optional.of(linkService.getProductLink(product, null, linkParameters, request));
    } else if (bean instanceof ProductInSite) {
      ProductInSite productInSite = (ProductInSite) bean;
      Product product = productInSite.getProduct();
      return Optional.of(linkService.getProductLink(product, null, linkParameters, request));
    } else if (bean instanceof LiveContextExternalProduct) {
      LiveContextExternalProduct externalProduct = (LiveContextExternalProduct) bean;
      Product product = externalProduct.getProduct();

      if (product == null) {
        LOG.debug("Cannot generate link for augmented product '{}'; product is not set.",
                externalProduct.getContentId());
        return Optional.empty();
      }

      return Optional.of(linkService.getProductLink(product, null, linkParameters, request));
    } else if (bean instanceof Product) {
      Product product = (Product) bean;
      return Optional.of(linkService.getProductLink(product, null, linkParameters, request));
    } else if (bean instanceof CMExternalPage) {
      CMExternalPage externalPage = (CMExternalPage) bean;
      if (externalPage.isRoot()) {
        return Optional.of(linkService.getExternalPageLink(null, null, storeContext, linkParameters, request));
      }

      String seoPath = externalPage.getExternalId();
      return Optional.of(linkService.getExternalPageLink(seoPath, null, storeContext, linkParameters, request));
    } else if (bean instanceof LiveContextNavigation) {
      LiveContextNavigation liveContextNavigation = (LiveContextNavigation) bean;
      Category category = liveContextNavigation.getCategory();
      return Optional.of(linkService.getCategoryLink(category, linkParameters, request));
    } else if (bean instanceof CategoryInSite) {
      CategoryInSite categoryInSite = (CategoryInSite) bean;
      return Optional.of(linkService.getCategoryLink(categoryInSite.getCategory(), linkParameters, request));
    } else if (bean instanceof Category) {
      Category category = (Category) bean;
      return Optional.of(linkService.getCategoryLink(category, linkParameters, request));
    } else if (bean instanceof CMNavigation) {
      CMNavigation cmNavigation = (CMNavigation) bean;
      String seoPath = seoSegmentBuilder.asSeoSegment(navigation, cmNavigation);
      return Optional.of(linkService.getContentLink(seoPath, storeContext, linkParameters, request));
    } else if (bean instanceof CMLinkable) {
      CMLinkable cmLinkable = (CMLinkable) bean;
      String seoPath = seoSegmentBuilder.asSeoSegment(navigation, cmLinkable);
      return Optional.of(linkService.getContentLink(seoPath, storeContext, linkParameters, request));
    } else if (bean instanceof ContentBeanBackedPageGridPlacement || bean instanceof DynamizableContainer) {
      String relativeLink = deabsolutizeLink(source);
      return Optional.of(linkService.getAjaxLink(relativeLink, storeContext, request));
    } else {
      return Optional.empty();
    }
  }

  @Nullable
  private static String debug(Object bean) {
    if (bean instanceof ContentBean) {
      Content content = ((ContentBean) bean).getContent();
      if (content != null) {
        return content.getPath();
      }
    }

    return bean + "";
  }

  @Override
  public boolean isApplicable(Object bean, HttpServletRequest request) {
    // Only execute when link service is available and current request
    // is not the studio preview URL request (`/preview?id=xxx`).
    return isLinkServiceAvailable() && !isStudioPreviewRequest(request);
  }

  private static boolean isLinkServiceAvailable() {
    return CurrentStoreContext.find()
            .map(StoreContext::getConnection)
            .map(CommerceConnection::getLinkService)
            .isPresent();
  }

  private static boolean isStudioPreviewRequest(HttpServletRequest request) {
    return PreviewHandler.isStudioPreviewRequest(request);
  }
}
