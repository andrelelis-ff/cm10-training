package com.coremedia.livecontext.elastic.social.rest;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

@Named
public class LiveContextSocialModule extends SimpleModule {
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")

  private Collection<JsonSerializer<?>> jsonSerializers;

  @Inject
  public LiveContextSocialModule(Collection<JsonSerializer<?>> jsonSerializers) {
    super("LiveContextSocial", new Version(1, 0, 0, null, null, null));
    this.jsonSerializers = jsonSerializers;
  }

  @Override
  public void setupModule(final SetupContext context) {
    for (final JsonSerializer<?> jsonSerializer : jsonSerializers) {
      if (jsonSerializer.getClass().getPackage().equals(LiveContextSocialModule.class.getPackage()) ) {
        addSerializer(jsonSerializer);
      }
    }
    super.setupModule(context);
  }
}
