package com.coremedia.ecommerce.studio.rest.job;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceBean;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.jobs.Job;
import com.coremedia.rest.invalidations.SimpleInvalidationSource;
import com.coremedia.rest.linking.LinkResolver;
import com.coremedia.rest.linking.LinkResolverUtil;
import com.coremedia.rest.linking.Linker;
import com.google.common.collect.Streams;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.scheduling.TaskScheduler;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractPushedContentJob implements Job {

  public static final String PUSH_STATE_RESOURCE_BASE_URI = "livecontext/pushState/";

  private final LinkResolver linkResolver;
  protected final CommerceConnectionSupplier commerceConnectionSupplier;
  final SimpleInvalidationSource pushStateInvalidationSource;
  protected final Linker linker;
  protected final CommerceConnectionInitializer commerceConnectionInitializer;
  protected final SitesService sitesService;
  final TaskScheduler taskScheduler;

  private String entityUri;
  private String siteId;

  AbstractPushedContentJob(@NonNull LinkResolver linkResolver,
                           @NonNull CommerceConnectionSupplier commerceConnectionSupplier,
                           @NonNull SimpleInvalidationSource pushStateInvalidationSource, @NonNull Linker linker,
                           @NonNull CommerceConnectionInitializer commerceConnectionInitializer,
                           @NonNull SitesService sitesService, @NonNull TaskScheduler taskScheduler) {
    this.linkResolver = linkResolver;
    this.commerceConnectionSupplier = commerceConnectionSupplier;
    this.pushStateInvalidationSource = pushStateInvalidationSource;
    this.linker = linker;
    this.commerceConnectionInitializer = commerceConnectionInitializer;
    this.sitesService = sitesService;
    this.taskScheduler = taskScheduler;
  }

  public void setEntityUri(String entityUri) {
    this.entityUri = entityUri;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public String getEntityUri() {
    return entityUri;
  }

  public String getSiteId() {
    return siteId;
  }

  static String getIdFromEntity(Object entity) {
    String id = null;
    if (entity instanceof CommerceBean) {
      id = CommerceIdFormatterHelper.format(((CommerceBean) entity).getId());
    } else if (entity instanceof Content) {
      id = ((Content) entity).getId();
    }
    return id;
  }

  void initCommerceConnection(@NonNull String siteId) {
    sitesService.findSite(siteId)
            .flatMap(commerceConnectionInitializer::findConnectionForSite)
            .map(CommerceConnection::getStoreContext)
            .ifPresent(CurrentStoreContext::set);
  }

  Optional<StoreContext> getStoreContextFromEntity(Object entity) {
    if (entity instanceof Content) {
      return commerceConnectionSupplier.findConnection((Content) entity)
              .map(CommerceConnection::getStoreContext);
    } else if (entity instanceof AbstractCommerceBean) {
      return Optional.of(((AbstractCommerceBean) entity).getContext());
    }
    return Optional.empty();
  }

  Object getEntityFromUri(String entityUri) {
    return LinkResolverUtil.resolveLink(entityUri.substring(4), linkResolver);//remove leading "api/"
  }

  Set<String> getUrisToBeInvalidated(Object bean) {
    Stream<Object> stream = Stream.of(bean);
    if (bean instanceof Product && !(bean instanceof ProductVariant)) {
      stream = Streams.concat(stream, ((Product) bean).getVariants().stream());
    }
    return stream.map(item -> getInvalidationUriForSingleEntity(item))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
  }

  private String getInvalidationUriForSingleEntity(@NonNull Object entity) {
    String link = linker.link(entity).toString();

    if (entity instanceof Content) {
      return PUSH_STATE_RESOURCE_BASE_URI + link;
    } else if (entity instanceof CommerceBean) {
      return link.replace("livecontext/", PUSH_STATE_RESOURCE_BASE_URI);
    }
    return null;
  }
}
