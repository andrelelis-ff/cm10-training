package com.coremedia.livecontext.p13n.preview;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.personalization.contentbeans.CMUserProfile;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProfile;
import com.coremedia.personalization.preview.TestContextExtractor;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

/**
 * Extracts commerce usersegments from cmUserProfile and enriches the p13n ContextCollection and the StoreContext.
 */
public class CommerceSegmentTestContextExtractor implements TestContextExtractor {
  private static final Logger LOG = LoggerFactory.getLogger(CommerceSegmentTestContextExtractor.class);

  private ContentBeanFactory contentBeanFactory;

  static final String PROPERTIES_PREFIX = "properties";
  static String COMMERCE_CONTEXT = "commerce";
  static String USER_SEGMENTS_PROPERTY = "usersegments";

  private static String SEGMENTS_PROPERTY_PATH = CMUserProfile.PROFILE_EXTENSIONS + "[" + PROPERTIES_PREFIX + "][" + COMMERCE_CONTEXT + "][" + USER_SEGMENTS_PROPERTY + "]";

  @Override
  public void extractTestContextsFromContent(Content content, ContextCollection contextCollection) {
    if (content == null || contextCollection == null) {
      LOG.debug("supplied content or contextCollection are null; cannot extract any contexts");
      return;
    }

    ContentBean cmUserProfileBean = contentBeanFactory.createBeanFor(content, ContentBean.class);
    if (!(cmUserProfileBean instanceof CMUserProfile)) {
      LOG.debug("cannot extract context from contentbean of type {}", cmUserProfileBean.getClass().toString());
      return;
    }

    Object userSegments = getProperty((CMUserProfile) cmUserProfileBean, SEGMENTS_PROPERTY_PATH);

    if (userSegments instanceof List) {
      List userSegmentList = (List) userSegments;
      if (!userSegmentList.isEmpty()) {
        PropertyProfile propertyProfile = new PropertyProfile();
        propertyProfile.setProperty(USER_SEGMENTS_PROPERTY, StringUtils.join(userSegmentList, ","));
        contextCollection.setContext(COMMERCE_CONTEXT, propertyProfile);

        addUserSegmentsToStoreContext(userSegmentList);
      }
    }
  }

  private void addUserSegmentsToStoreContext(@NonNull List<String> userSegmentList) {
    StoreContext storeContext = CurrentStoreContext.find().orElse(null);

    if (storeContext == null) {
      LOG.debug("Store context is null; cannot add user segments to store context.");
      return;
    }

    String segmentIdsStr = assembleSegmentIdsString(userSegmentList);

    StoreContext updatedStoreContext = storeContext.getConnection()
            .getStoreContextProvider()
            .buildContext(storeContext)
            .withUserSegments(segmentIdsStr)
            .build();
    CurrentStoreContext.set(updatedStoreContext);

    StoreContextHelper.setStoreContextToRequest(updatedStoreContext);
  }

  @NonNull
  private String assembleSegmentIdsString(@NonNull List<String> userSegmentList) {
    return userSegmentList.stream()
            .map(CommerceSegmentTestContextExtractor::parseSegmentId)
            .flatMap(Optional::stream)
            .collect(joining(","));
  }

  @NonNull
  private static Optional<String> parseSegmentId(@Nullable String userSegment) {
    return CommerceIdParserHelper.parseCommerceId(userSegment)
            .flatMap(CommerceId::getExternalId);
  }

  private static Object getProperty(CMUserProfile userProfile, String propertyPath) {
    try {
      return PropertyAccessorFactory.forBeanPropertyAccess(userProfile).getPropertyValue(propertyPath);
    } catch (InvalidPropertyException | PropertyAccessException ex) {
      return null;
    }
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }
}
