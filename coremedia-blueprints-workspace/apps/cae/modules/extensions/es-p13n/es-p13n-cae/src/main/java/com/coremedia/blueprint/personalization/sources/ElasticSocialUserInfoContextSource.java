package com.coremedia.blueprint.personalization.sources;

import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.ratings.LikeService;
import com.coremedia.elastic.social.api.ratings.RatingService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProvider;
import com.coremedia.personalization.context.collector.AbstractContextSource;
import org.springframework.beans.factory.annotation.Required;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A {@link com.coremedia.personalization.context.collector.ContextSource} that exposes properties
 * of a {@link CommunityUser}. Properties are enriched with number of comments, number of ratings and
 * number of likes.
 */
public class ElasticSocialUserInfoContextSource extends AbstractContextSource {

  // the name under which this context will be available
  private String contextName;

  private static final String NUMBER_OF_COMMENTS = "numberOfComments";
  private static final String USER_LOGGED_IN = "userLoggedIn";
  private static final String NUMBER_OF_RATINGS = "numberOfRatings";
  private static final String NUMBER_OF_LIKES = "numberOfLikes";

  private static final List<String> VIRTUAL_PROPERTIES = Arrays.asList(NUMBER_OF_COMMENTS, NUMBER_OF_RATINGS, NUMBER_OF_LIKES, USER_LOGGED_IN);

  @Inject
  private CommentService commentService;

  @Inject
  private RatingService ratingService;

  @Inject
  private LikeService likeService;

  @Override
  public void preHandle(final HttpServletRequest request, final HttpServletResponse response,
                        final ContextCollection contextCollection) {
    final CommunityUser communityUser = UserHelper.getLoggedInUser();
    if (communityUser != null) {
      contextCollection.setContext(contextName, new UserPropertiesPropertyProvider(communityUser));
    }
  }

  private long getNumberOfComments(CommunityUser user) {
    return commentService.getNumberOfApprovedComments(user);
  }

  private long getNumberOfRatings(CommunityUser user) {
    return ratingService.getNumberOfRatingsFromUser(user);
  }

  private long getNumberOfLikes(CommunityUser user) {
    return likeService.getNumberOfLikesFromUser(user);
  }

  /**
   * Sets the name of the context that is managed by this source. This is a required
   * property.
   *
   * @param contextName name of the context that is managed by this source. Must
   *                    not be <code>null</code>.
   */
  @Required
  public void setContextName(final String contextName) {
    if (contextName == null) {
      throw new IllegalArgumentException("supplied contextName must not be null");
    }
    this.contextName = contextName;
  }

  /**
   * PropertyProvider implementation backed by the properties of a community user.
   * In contrast to {@link com.coremedia.personalization.context.MapPropertyMaintainer}, this class can deal with null values in user properties.
   */
  private final class UserPropertiesPropertyProvider implements PropertyProvider {

    private final CommunityUser communityUser;
    private final @PersonalData Map<String,Object> communityUserProperties;
    private final List<String> propertyNames = new ArrayList<>(VIRTUAL_PROPERTIES);

    private UserPropertiesPropertyProvider(CommunityUser communityUser) {
      this.communityUser = communityUser;
      communityUserProperties = communityUser.getProperties();
      propertyNames.addAll(communityUserProperties.keySet());
    }

    @Override
    public @PersonalData Object getProperty(String key) {
      if(VIRTUAL_PROPERTIES.contains(key)) {
        if(NUMBER_OF_COMMENTS.equals(key)) {
          return getNumberOfComments(communityUser);
        } else if(NUMBER_OF_LIKES.equals(key)) {
          return getNumberOfLikes(communityUser);
        } else if(NUMBER_OF_RATINGS.equals(key)) {
          return getNumberOfRatings(communityUser);
        }
        return true; // default is USER_LOGGED_IN
      }
      return communityUserProperties.get(key);
    }

    @Override
    public <T> @PersonalData T getProperty(String key, T defaultValue) {
      final @PersonalData Object property = getProperty(key);
      //noinspection unchecked
      return (defaultValue != null && defaultValue.getClass().isInstance(property) )? (@PersonalData T) property : defaultValue;
    }

    @Override
    public Collection<String> getPropertyNames() {
      return Collections.unmodifiableCollection(propertyNames);
    }
  }
}
