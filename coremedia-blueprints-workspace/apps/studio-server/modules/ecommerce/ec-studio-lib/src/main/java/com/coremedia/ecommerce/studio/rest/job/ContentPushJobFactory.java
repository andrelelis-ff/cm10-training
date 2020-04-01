package com.coremedia.ecommerce.studio.rest.job;

import com.coremedia.rest.cap.jobs.Job;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.inject.Named;

@Named
public class ContentPushJobFactory extends AbstractPushContentJobFactory{

  @Override
  public boolean accepts(@NonNull String jobType) {
    return "pushContent".equals(jobType);
  }

  @NonNull
  @Override
  public Job createJob() {
    return new ContentPushJob(linkResolver, commerceConnectionSupplier, pushStateInvalidationSource, linker, commerceConnectionInitializer, sitesService, taskScheduler);
  }
}
