package com.coremedia.blueprint.sfmc.preview.cae.p13n;

import com.coremedia.blueprint.personalization.contentbeans.CMUserProfile;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProfile;
import com.coremedia.personalization.preview.TestContextExtractor;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.List;
import java.util.Optional;

public class JourneyTestContextExtractor implements TestContextExtractor {
  private static final Logger LOG = LoggerFactory.getLogger(JourneyTestContextExtractor.class);
  private static final String CONTEXT_NAME = "sfmc";
  private static final String CONTEXT_JOURNEY_PROPERTY = "journeys";
  private static final String JOURNEY_PROPERTY_PATH = CMUserProfile.PROFILE_EXTENSIONS + "[properties][" + CONTEXT_NAME + "][" + CONTEXT_JOURNEY_PROPERTY + "]";
  private ContentBeanFactory contentBeanFactory;

  JourneyTestContextExtractor(@NonNull ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Override
  public void extractTestContextsFromContent(Content content, ContextCollection contextCollection) {
    if (content == null || contextCollection == null) {
      LOG.debug("supplied content or contextCollection are null; cannot extract any contexts");
      return;
    }

    CMUserProfile cmUserProfileBean = contentBeanFactory.createBeanFor(content, CMUserProfile.class);
    if (cmUserProfileBean == null) {
      LOG.debug("cannot extract context from contentbean of type {}", content);
      return;
    }

    Optional<Object> journeysOptional = getJourneys(cmUserProfileBean);

    if (!journeysOptional.isPresent()) {
      return;
    }

    Object journeys = journeysOptional.get();
    if (!(journeys instanceof List)) {
      return;
    }
    List userSegmentList = (List) journeys;
    if (userSegmentList.isEmpty()) {
      return;
    }
    PropertyProfile propertyProfile = new PropertyProfile();
    propertyProfile.setProperty(CONTEXT_JOURNEY_PROPERTY, StringUtils.join(userSegmentList, ","));
    contextCollection.setContext(CONTEXT_NAME, propertyProfile);
  }


  private Optional<Object> getJourneys(@NonNull CMUserProfile userProfile) {
    try {
      return Optional.ofNullable(PropertyAccessorFactory.forBeanPropertyAccess(userProfile).getPropertyValue(JOURNEY_PROPERTY_PATH));
    } catch (InvalidPropertyException | PropertyAccessException ex) {
      return Optional.empty();
    }
  }
}
