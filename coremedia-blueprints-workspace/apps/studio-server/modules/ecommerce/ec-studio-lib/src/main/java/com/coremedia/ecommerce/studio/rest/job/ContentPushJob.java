package com.coremedia.ecommerce.studio.rest.job;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.push.PushService;
import com.coremedia.rest.cap.jobs.JobContext;
import com.coremedia.rest.cap.jobs.JobExecutionException;
import com.coremedia.rest.invalidations.SimpleInvalidationSource;
import com.coremedia.rest.linking.LinkResolver;
import com.coremedia.rest.linking.Linker;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.scheduling.TaskScheduler;

import java.util.Optional;


public class ContentPushJob extends AbstractPushedContentJob{

  ContentPushJob(@NonNull LinkResolver linkResolver, @NonNull CommerceConnectionSupplier commerceConnectionSupplier, SimpleInvalidationSource pushStateInvalidationSource, Linker linker, CommerceConnectionInitializer commerceConnectionInitializer, SitesService sitesService, TaskScheduler taskScheduler) {
    super(linkResolver, commerceConnectionSupplier, pushStateInvalidationSource, linker, commerceConnectionInitializer, sitesService, taskScheduler);
  }

  @Nullable
  @Override
  public Object call(@NonNull JobContext jobContext) throws JobExecutionException {
    ProgressRunnable progressRunnable = new ProgressRunnable(jobContext, taskScheduler);
    try {
      progressRunnable.schedule();
      doUpload(getEntityUri(), getSiteId());
      jobContext.notifyProgress(1);
      return "Content successfully pushed";
    } finally {
      progressRunnable.cancel();
    }
  }

  private void doUpload(String entityUri, String siteId){
    initCommerceConnection(siteId);
    Object entity = getEntityFromUri(entityUri);
    Optional<StoreContext> storeContextOptional = getStoreContextFromEntity(entity);
    String id = getIdFromEntity(entity);
    if (id != null && storeContextOptional.isPresent()) {
      Optional<PushService> pushServiceOpt = storeContextOptional.map(StoreContext::getConnection).flatMap(CommerceConnection::getPushService);
      if (pushServiceOpt.isPresent()) {
        pushServiceOpt.get().push(id, storeContextOptional.get());
        // invalidate push state of recently pushed content
        pushStateInvalidationSource.addInvalidations(getUrisToBeInvalidated(entity));
      }
    }
  }
}
