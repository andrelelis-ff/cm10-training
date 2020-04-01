package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.rest.api.JsonCustomizer;
import com.coremedia.elastic.social.rest.api.JsonProperties;
import org.springframework.core.annotation.Order;

import javax.inject.Named;
import java.util.Map;

/**
 * We are satisfied with showing 'No preview for this contribution' for
 * users in the Studio moderation. This bean may be removed if a preview
 * for user information is desired (templates required!).
 */
@Named
@Order(0)
public class CommunityUserJsonCustomizer implements JsonCustomizer<CommunityUser> {
  @Override
  public void customize(CommunityUser communityUser, @PersonalData Map<String, Object> serializedObject) {  // NOSONAR unused parameters
    serializedObject.put(JsonProperties.PREVIEW_URL, null);
  }
}