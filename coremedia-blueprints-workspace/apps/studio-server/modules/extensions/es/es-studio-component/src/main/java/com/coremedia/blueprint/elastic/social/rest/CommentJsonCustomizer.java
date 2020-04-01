package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.cap.content.Content;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.core.api.models.UnresolvableReferenceException;
import com.coremedia.elastic.core.cms.ContentWithSite;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.rest.api.JsonCustomizer;
import com.coremedia.rest.cap.content.ContentRepositoryResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static com.coremedia.elastic.social.rest.api.JsonProperties.PREVIEW_URL;
import static com.coremedia.elastic.social.rest.api.JsonProperties.SUBJECT;
import static java.text.MessageFormat.format;

@Named
@Order(0)
public class CommentJsonCustomizer implements JsonCustomizer<Comment> {
  private static final Logger LOG = LoggerFactory.getLogger(CommentJsonCustomizer.class);

  private static final String CURATED_CONTENTS = "curatedContents";

  @Inject
  ContentRepositoryResource contentRepositoryResource;

  @Override
  public void customize(Comment comment, @PersonalData Map<String, Object> serializedObject) {
    try {
      Object target = comment.getTarget();
      if (target instanceof ContentWithSite) {
        Content content = ((ContentWithSite) target).getContent();
        // Order matters: determine Preview URL first!
        // Read-rights problems might break later steps.
        addPreviewUrl(serializedObject, content);
        addTitle(serializedObject, content);
      } else {
        LOG.debug("cannot customize target '{}'", target);
      }
      addCuratedContents(comment, serializedObject);
    } catch (IllegalArgumentException e) {
      LOG.warn("An exception '{}' occurred resolving the target reference for the comment with id {}", e, comment.getId());
    } catch (UnresolvableReferenceException e) {
      LOG.debug("Could not resolve target reference for the comment with id {}: {}", comment.getId(), e);
    } catch (RuntimeException e) {
      LOG.warn("An exception '{}' occurred resolving the target reference for the comment with id {}", e, comment.getId());
    }
    // add customization for special views like to preview URL (if necessary)
  }

  private void addPreviewUrl(@PersonalData Map<String, Object> serializedObject, Content content) {
    String url = format(contentRepositoryResource.getPreviewControllerUrlPattern(), content.getId());
    serializedObject.put(PREVIEW_URL, url);
  }

  private static void addTitle(@PersonalData Map<String, Object> serializedObject, Content content) {
    String title = content.getString("title");
    if (StringUtils.isNotBlank(title)) {
      serializedObject.put(SUBJECT, title);
    }
  }

  private static void addCuratedContents(Comment comment, @PersonalData Map<String, Object> serializedObject) {
    @SuppressWarnings("PersonalData") // the list of created curated contents from a comment is not personal data
    List<?> curatedContents = comment.getProperty(CuratedTransferResource.COMMENT_PROPERTY_CURATED_CONTENTS, List.class);
    if (curatedContents != null && !curatedContents.isEmpty()) {
      serializedObject.put(CURATED_CONTENTS, curatedContents);
    }
  }
}
